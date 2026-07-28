# Codex CLI: 84 GB/day of SQLite disk writes, and how to stop them

A field report from a macOS workstation where Codex CLI was responsible for 38% of all disk
writes on the machine. Includes the measurements, the root cause, the fix, and the numbers
after the fix.

Environment: MacBook Pro `Mac16,5` (M4 Max, 36 GB RAM), macOS 26.5.2 (25F84), internal
`APPLE SSD AP1024Z` (1 TB). Codex CLI with a `logs_2.sqlite` local log database.

## The symptom

`kernel_task` had written ~2 TB over 27 days of uptime, which is what surfaced the problem in
Activity Monitor. That number is a red herring — `kernel_task` aggregates all kernel-side
writes (swap, APFS metadata, the memory compressor, unified log), so it reports a symptom
rather than a cause.

The real totals for that 27d 23h uptime, read from the block device layer:

| Metric | Value |
| --- | --- |
| Written to `disk0` | 7.06 TB (6573 GiB) |
| Read from `disk0` | 11.24 TB |
| Write rate | ~252 GB/day |
| Swapouts | 70,400,166 × 16 KB ≈ 1.15 TB |

There is no authoritative baseline for what a workstation *should* write, so treat this as a
judgement call rather than a threshold: 252 GB/day was high enough to be worth attributing to
a specific source.

## Finding the source

macOS keeps a daily write profile that nobody looks at. It is a sampled stack profile — each
sample corresponds to a fixed number of bytes written, so summing samples per process gives a
per-process byte attribution:

```sh
ls /Library/Logs/DiagnosticReports/disk\ writes_*.diag
```

The header states the scale (`Steps: 21399 (32 samples lost, 10.49 MB/step)`). Summing
`Num samples` per process and multiplying by the step size:

```sh
awk '
/^Powerstats for:/ { name=$0; sub(/^Powerstats for:[ \t]+/,"",name); sub(/ \(.*/,"",name); next }
/^Num samples:/ { s[name]+=$3 }
END { for (n in s) printf "%d\t%s\n", s[n], n }' "$REPORT" \
  | sort -rn | head -25 \
  | awk -F'\t' '{printf "%8.1f GiB  %s\n", $1*10.49/1024, $2}'
```

Result for a single day (224 GB total):

| Process | Written that day |
| --- | --- |
| **codex** | **84.3 GB** |
| firefox | 32.1 GB |
| Virtualization.VirtualMachine | 23.2 GB |
| java | 12.1 GB |
| jetbrains-toolbox | 6.6 GB |
| mds_stores | 6.1 GB |
| idea | 6.0 GB |
| launchd | 1.1 GB |

Codex alone accounted for **38% of all disk writes on the machine**, while its entire
`~/.codex` directory was only 3.2 GB.

## Root cause

The writes come from `~/.codex/logs_2.sqlite`, the local log database. Three factors multiply
together.

**1. Every SSE event is logged at TRACE level.** Codex persists each streamed event from the
model — including full tool-call payloads — as an individual row:

```sh
sqlite3 ~/.codex/logs_2.sqlite \
  "SELECT level, COUNT(*) FROM logs GROUP BY level ORDER BY 2 DESC;"
```

```
TRACE|100292      # 61%
DEBUG|32124
INFO|31581
WARN|1520
ERROR|23
```

The noisiest target was `codex_api::sse::responses` at 49,709 rows.

**2. The insert rate is enormous, and rotation hides it.** The `AUTOINCREMENT` counter tells
the real story — only 165,540 rows were live, but the counter had reached 261 million:

```sh
sqlite3 ~/.codex/logs_2.sqlite "SELECT COUNT(*), MIN(rowid), MAX(rowid) FROM logs;"
sqlite3 ~/.codex/logs_2.sqlite \
  "SELECT datetime(MIN(ts),'unixepoch','localtime'), datetime(MAX(ts),'unixepoch','localtime'),
          MAX(ts)-MIN(ts), COUNT(*), SUM(estimated_bytes) FROM logs;"
```

Over the 10.5 days the live rows spanned, **110 million rows were inserted** — about
**122 inserts/second, around the clock**. Rotation discarded 98.5% of them almost immediately.
Because the file size stays flat while rows are recycled, `du` shows nothing and the growth is
invisible to normal inspection.

**3. Write amplification stacks on top.** Roughly 10.5 GB/day of logical log data became
84.3 GB/day of physical writes, about **8×**:

- The `logs` table carries **four indexes**, three of them composite — every insert updates
  four B-trees.
- **11 concurrent Codex processes** (ages 1 to 9.5 days) held the same database open in WAL
  mode. With a reader almost always active, checkpoints could rarely acquire the lock they
  need, so the WAL grew to **75 MB** against a default 4 MB threshold.
- When a checkpoint did land, it rewrote pages into a 604 MB file, and APFS copy-on-write
  allocated fresh blocks for each rewritten page.
- `auto_vacuum` is set to `incremental`, but nothing ever calls `incremental_vacuum`, so freed
  pages accumulated: **84,275 of 147,589 pages (57%) sat on the freelist**, and the file held
  604 MB for 166 MB of live data.

Check the concurrent holders with:

```sh
lsof ~/.codex/logs_2.sqlite | tail -n +2 | awk '{print $2}' | sort -u
```

## Upstream status

This is a known, open bug — not a local misconfiguration.

- [openai/codex#35092](https://github.com/openai/codex/issues/35092) — *Codex CLI 0.145.0
  still persists per-SSE TRACE events to SQLite, causing high-frequency disk writes.* The
  primary issue.
- [openai/codex#28224](https://github.com/openai/codex/issues/28224) — the workaround thread.
- [openai/codex#29237](https://github.com/openai/codex/issues/29237) — CLI crashes with
  SIGTRAP once `logs_2.sqlite` exceeds ~200 MB (panic in a background task under WAL
  checkpoint contention; `panic = "abort"` turns it into SIGTRAP).
- [openai/codex#32431](https://github.com/openai/codex/issues/32431) — on macOS the OS
  `disk writes` resource monitor can kill the process outright over this.

**`RUST_LOG` does not help.** The SQLite sink has its own `default_filter()` that defaults to
TRACE and never consults the environment variable. Confirmed against the shipped binary — the
string appears exactly once, as part of an error message belonging to the console subscriber:

```sh
strings -a /Applications/Codex.app/Contents/Resources/codex | grep -c RUST_LOG   # => 1
```

## The fix

A `BEFORE INSERT` trigger that drops TRACE rows, followed by a checkpoint and a vacuum. It
lives **inside the database**, so it applies to every Codex process immediately — no sessions
need to be restarted.

```sh
sqlite3 "$HOME/.codex/logs_2.sqlite" <<'SQL'
CREATE TRIGGER IF NOT EXISTS codex_block_trace_logs
BEFORE INSERT ON logs
WHEN UPPER(NEW.level) = 'TRACE'
BEGIN
  SELECT RAISE(IGNORE);
END;
SQL
```

Then reclaim the space. `incremental_vacuum` is the right tool here — a plain `VACUUM` needs an
exclusive lock and will not succeed while Codex processes hold the database open:

```sh
sqlite3 "$HOME/.codex/logs_2.sqlite" "PRAGMA wal_checkpoint(TRUNCATE);"
sqlite3 "$HOME/.codex/logs_2.sqlite" "PRAGMA incremental_vacuum;"
sqlite3 "$HOME/.codex/logs_2.sqlite" "PRAGMA wal_checkpoint(TRUNCATE);"
```

The second checkpoint is required: in WAL mode the main file is not truncated until the
freed pages are checkpointed back. `incremental_vacuum` took 5.5 seconds for 84,267 pages.

Verify:

```sh
sqlite3 ~/.codex/logs_2.sqlite "SELECT name FROM sqlite_master WHERE type='trigger';"
sqlite3 ~/.codex/logs_2.sqlite "PRAGMA integrity_check;"
```

## Numbers after the fix

| Metric | Before | After |
| --- | --- | --- |
| `logs_2.sqlite` | 604.5 MB | **246.9 MB** |
| WAL | 75.1 MB | **0**, then steady at 4.4 MB |
| Freelist pages | 84,275 (57% of file) | 8 |
| Page count | 147,589 | 63,196 |
| TRACE inserts | ~61% of the stream | **0** |
| Insert rate | ~122/sec | 43.7/sec (DEBUG + INFO) |

432 MB reclaimed on disk. Nothing was dropped: the row count stands at 165,878 — up from the
165,540 measured during diagnosis, the difference being DEBUG and INFO rows that arrived in
between — and `PRAGMA integrity_check` returns `ok`. The last TRACE row is timestamped at the
exact second the trigger was installed; none have been written since.

The insert rate dropped by 64% (122 → 43.7 rows/sec), which lines up exactly with TRACE having
been 61% of the stream. The remaining rows are DEBUG and INFO, which the workaround
deliberately keeps so the built-in feedback reporter still has diagnostics. To cut roughly 19%
more, widen the trigger condition to `UPPER(NEW.level) IN ('TRACE','DEBUG')`.

### On measuring the byte-level effect

The four metrics above are direct and unambiguous. Whole-machine write throughput is harder to
attribute, and worth being explicit about rather than overclaiming.

A five-minute sample taken right after the fix showed 719 MB written across the whole system —
about 8.4 GiB/hour, against a 9.8 GiB/hour average over the preceding 671-hour uptime:

```sh
getw() { ioreg -rw0 -c IOBlockStorageDriver | tr ',' '\n' \
         | grep '"Bytes (Write)"' | grep -oE '[0-9]+' | sort -rn | head -1; }
W0=$(getw); sleep 300; W1=$(getw); echo $(( (W1-W0)/1048576 )) MB
```

Note `sort -rn | head -1` rather than `tail -1`: there is more than one `IOBlockStorageDriver`
instance and their order is not stable between calls, so picking the wrong one silently yields
a delta of zero.

That comparison is weak evidence on its own — a five-minute window against a 28-day average,
taken at night while the other heavy writers (Firefox, a VM, JVMs) were mostly idle. The clean
measurement is the next daily `disk writes` report: compare the `codex` line in tomorrow's
`/Library/Logs/DiagnosticReports/disk writes_*.diag` against the 84.3 GB baseline, using the
awk one-liner from the top of this document. Expect a reduction rather than elimination, since
DEBUG and INFO still carry the same per-insert amplification through four indexes and the WAL.

## Caveats

- **The trigger lives inside the database.** A future `logs_2` → `logs_3` schema migration will
  silently drop it. Re-check after Codex updates with the verify command above.
- **Feedback reports lose TRACE detail.** If you file a bug upstream and are asked for trace
  logs, drop the trigger, reproduce, then reinstall it.
- Rollback is a one-liner:

  ```sh
  sqlite3 ~/.codex/logs_2.sqlite "DROP TRIGGER codex_block_trace_logs;"
  ```

## Was the SSD actually at risk?

Worth stating plainly, because the panic-inducing number (2 TB from `kernel_task`) turned out
not to matter much. SMART data for the drive, read with DriveDx — `smartctl` cannot reach the
internal SSD on Apple Silicon, since it sits behind an `AppleANS3CGv2` controller rather than a
standard NVMe interface:

| Indicator | Value |
| --- | --- |
| Life Percentage Used | **3%** |
| Data Units Written (lifetime) | 52.5 TB |
| Available Spare | 100% |
| Media and Data Integrity Errors | 0 |
| Power On Hours | 1,932 |

52.5 TB consuming 3% implies a total endurance budget on the order of 1.7 PB — far above the
~600 TBW typical of consumer 1 TB TLC drives. Even at the pre-fix rate the drive had years of
headroom.

So the fix is worth applying for I/O contention, thermals, and battery life rather than for
drive longevity. The genuinely urgent item was the 604 MB database sitting three times past the
~200 MB threshold where [#29237](https://github.com/openai/codex/issues/29237) reports SIGTRAP
crashes.
