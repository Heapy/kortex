---
name: message-claude
description: Use when an agent or script outside a Claude Code session needs to post a message into one — codex reporting back to the session that launched it, a hook or long job notifying a running session, one machine-local process telling another what landed. Triggers on "send a message to Claude", "notify the Claude session", "report back to Claude", "post to Claude's inbox", "tell the parent session", "wake the Claude session when this finishes". Covers the inbox socket protocol and its newline-delimited payload, how to find the target session and which of its two tokens to send, when a message is delivered rather than held or refused, and the codex-specific traps — a scrubbed environment, registry-based discovery, and misleading socket probes.
---

# Message Claude

When `ListAgents` and `SendMessage` are available inside Claude Code, use them. They are the
supported in-session route. Do not use the raw inbox to bypass a permission rule that removed or
denied either tool.

The rest of this skill is for an external caller — Codex, a hook, or a script — that has no Claude
messaging tools and must use the inbox transport directly.

Every Claude Code session with cross-session messaging on binds a private inbox endpoint. Any
process running as the same OS user can open it and post text, subject to platform auth and the
receiving session's inbound controls. The text arrives in that session as a turn, the way a typed
prompt does, tagged with where it came from.

This is the outside-in direction. `SendMessage` is the inside-out one; an external agent has no
Claude tools, only the inbox transport.

## Prerequisites

| Requirement | Detail |
|---|---|
| Base feature | Claude Code 2.1.224+ on macOS and Linux including WSL 2; 2.1.234+ on native Windows |
| Child auth token | `CLAUDE_CODE_MESSAGING_TOKEN` requires 2.1.228+ |
| Transport | Unix domain socket on macOS and Linux, named pipe on native Windows |
| Same user | The Unix socket is `0600`. Another OS user cannot reach it |
| Same filesystem | A container and its host cannot see each other's session files. Two processes in the same container can |
| Not bare | A session started with `--bare` binds no socket and cannot be messaged |

The executable recipes below support macOS, Linux, and WSL 2. Native Windows uses a named pipe,
requires auth, and needs a pipe client rather than `nc -U` or Python `AF_UNIX`; do not use these
Unix recipes there or claim a send succeeded.

Providers without the feature: Amazon Bedrock, Claude Platform on AWS, Google Cloud Agent
Platform, Microsoft Foundry. Values of `CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC`,
`DISABLE_TELEMETRY`, `DO_NOT_TRACK`, and `DISABLE_GROWTHBOOK` that disable feature-flag
evaluation also turn messaging off.

---

## Step 1 — find the target

Two paths. Prefer the second: it works whether or not the caller is a child, and it survives an
environment that strips variables.

| Situation | Address comes from |
|---|---|
| A hook or Bash command run directly by the session | `$CLAUDE_CODE_MESSAGING_SOCKET` and `$CLAUDE_CODE_MESSAGING_TOKEN` |
| Anything else, including `codex exec` launched by the session | the session registry on disk |

Each live session writes `<config-dir>/sessions/<pid>.json`, where the config directory is
`${CLAUDE_CONFIG_DIR:-$HOME/.claude}`:

```sh
claude_dir="${CLAUDE_CONFIG_DIR:-$HOME/.claude}"
for f in "$claude_dir"/sessions/*.json; do
  test -e "$f" || continue
  jq -r '[.name, .status, .cwd, .messagingSocketPath] | @tsv' "$f"
done
```

If Claude was launched with a custom `CLAUDE_CONFIG_DIR` and the external caller did not inherit
it, pass that absolute directory to the caller or replace the default in the recipe. Do not search
the whole home directory for session registries.

The fields that matter: `name` (what the session answers to), `messagingSocketPath`, `cwd` to
tell same-named sessions apart, `status`, and `pid`. A session whose process is gone leaves a
stale file — connect and fail rather than trusting the file.

A user can also read the address themselves: `/status` shows it in the `Peer address` row,
prefixed `uds:`.

### The two tokens

Alongside the socket each session holds two 16-byte hex tokens. They are not interchangeable.

| Token | Where | Who it identifies |
|---|---|---|
| `childToken` | env `CLAUDE_CODE_MESSAGING_TOKEN` | a process the session itself spawned |
| `peerToken` | `<config-dir>/sessions/<pid>.<sha256 of the recorded socket path>.key`, JSON field `peerToken` | another session, or anything speaking as a peer |

Hash the socket path exactly as `messagingSocketPath` records it:

```sh
sock=/tmp/cc-socks/12345.sock
printf '%s' "$sock" | shasum -a 256
```

Do not resolve the path first. On macOS, resolving `/tmp` to `/private/tmp` changes the hash and
looks for a key file that does not exist.

---

## Step 2 — send

The protocol is newline-delimited JSON, one object per line. Send the auth line first when it is
required or when a token is available, then the message line.

```
{"type":"auth","token":"<token>"}
{"type":"user","message":{"role":"user","content":"<text>"}}
```

**Build the whole payload before connecting.** A connection that has not delivered one complete
line within 15 seconds is closed with nothing delivered. Do not open the socket and then go
compute the message.

With the environment present:

```sh
msg='migration finished; tenant_id is the new column'
{ printf '{"type":"auth","token":"%s"}\n' "$CLAUDE_CODE_MESSAGING_TOKEN"
  jq -cn --arg m "$msg" '{type:"user",message:{role:"user",content:$m}}'
} | nc -U "$CLAUDE_CODE_MESSAGING_SOCKET"
```

Without `jq`, and without depending on the environment:

```sh
python3 - "$SOCK" "$TOKEN" "$MSG" <<'PY'
import json, socket, sys
sock, token, msg = sys.argv[1], sys.argv[2], sys.argv[3]
payload = ""
if token:
    payload += json.dumps({"type": "auth", "token": token}) + "\n"
payload += json.dumps({"type": "user", "message": {"role": "user", "content": msg}}) + "\n"
s = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
s.connect(sock)
s.sendall(payload.encode())
s.shutdown(socket.SHUT_WR)
s.close()
PY
```

Never build the JSON with `printf` and a raw string. Message text carries quotes, newlines, and
backslashes; hand-built JSON breaks on the first one and the line is dropped as unparseable.

### Is the auth line required

| Platform | Auth line |
|---|---|
| macOS, Linux, WSL 2 | Optional. The `0600` socket already proves the OS user |
| Native Windows | Required. A first line that is not a valid auth line closes the connection and delivers nothing |

Optional is not the same as pointless — see the next section.

---

## Delivered, held, or refused

Every arriving message is checked against the receiving session's `crossSessionInbound`
setting, and lands in one of three states:

| Outcome | What happens |
|---|---|
| Delivered | Claude reads it between tool calls, or it starts a new turn if the session is idle |
| Held | Set aside with a dialog. It reaches Claude only on approval, or when a later setting change accepts it |
| Refused | Dropped silently |

When no `crossSessionInbound` value applies, the default decides from permission classes — and
this is where the token earns its place. A message Claude Code can verify came from the
session's **own child** is delivered under the default. One it cannot verify asserts no
permission class, so a session running in `bypassPermissions` holds it for the user.

How that verification works is platform-shaped:

- **Linux, including WSL 2** — process evidence works even after the sending process exits.
- **macOS** — process evidence works only while the sender is still running. Once `nc` or the
  python one-liner has exited, the `childToken` in the auth line is the only remaining proof.
- **Containers where Claude Code is PID 1, and native Windows** — no process evidence at all.
  The token is the only proof.

So: a short-lived sender on macOS should always send the `childToken` when it has one. It costs
one line and it is the difference between delivered and held.

For an unattended `claude -p` worker that should take messages with nobody watching, start it
with `crossSessionInbound` set to `accept` in its `--settings` value. A `-p` session cannot show
an approval dialog; a held message there expires after `dialogExpiry`, five minutes by default.

---

## Codex-specific traps

Two things stand between `codex exec` and the socket. Neither announces itself clearly.

Measured with `codex exec --sandbox read-only` on macOS, against a script run outside codex as
the control. One of the two suspected walls is real.

| | Outside codex | Inside `codex exec --sandbox read-only` |
|---|---|---|
| Read `~/.claude/sessions/*.json` | 12 sessions | 12 sessions |
| `connect()` to a session socket | reachable | reachable |
| Send a message end to end | delivered | delivered |
| `CLAUDE_CODE_MESSAGING_SOCKET` | set | **unset** |
| `CLAUDE_CODE_MESSAGING_TOKEN` | present | **absent** |

**The sandbox was not the obstacle in this measured setup.** That `read-only` run read the session
registry, opened the socket, and delivered the payload without an extra grant. Do not generalize
that result to every Codex installation or permission profile: Unix-socket access can be denied by
policy. If `connect()` fails with a permission error, stop and report the exact socket path. The
caller must authorize that socket root in the active profile; the `codex sandbox` helper exposes
`--allow-unix-socket <path>` for explicit sandbox runs.

**The environment is.** `CLAUDE_CODE_MESSAGING_SOCKET` and `CLAUDE_CODE_MESSAGING_TOKEN` do not
survive into a codex run, and `-c shell_environment_policy.inherit=all` does not bring them
back. Any recipe that reads those variables inside codex is dead on arrival.

So a codex run finds its target on disk and carries no `childToken`. Read the `peerToken` out of
the target's key file instead: it is required on native Windows and costs one line elsewhere.
Whether the message then lands or waits is the receiving session's decision, not the sender's —
a session that must take messages unattended needs `crossSessionInbound` set to `accept` there.

### The recipe that works from codex on macOS and Linux

```python
import glob, hashlib, json, os, socket

CLAUDE_DIR = os.path.expanduser(os.environ.get("CLAUDE_CONFIG_DIR", "~/.claude"))
SESSIONS = os.path.join(CLAUDE_DIR, "sessions")
TARGET_NAME = "the-session-name"
TARGET_CWD = None  # set to an absolute path when names are not unique

def live_sessions():
    for f in glob.glob(os.path.join(SESSIONS, "*.json")):
        try:
            with open(f) as src:
                d = json.load(src)
        except Exception:
            continue
        if d.get("messagingSocketPath"):
            yield d

def peer_token(d):
    sock = d["messagingSocketPath"]
    name = f"{d['pid']}.{hashlib.sha256(sock.encode()).hexdigest()}.key"
    try:
        with open(os.path.join(SESSIONS, name)) as src:
            return json.load(src)["peerToken"]
    except Exception:
        return None

def send(d, text):
    token = peer_token(d)
    if not token:
        raise RuntimeError("peerToken not found for target session")
    lines = [
        json.dumps({"type": "auth", "token": token}),
        json.dumps({"type": "user", "message": {"role": "user", "content": text}}),
    ]
    s = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    s.connect(d["messagingSocketPath"])          # payload is already built
    s.sendall(("\n".join(lines) + "\n").encode())
    s.shutdown(socket.SHUT_WR)
    s.close()

targets = [
    d for d in live_sessions()
    if d.get("name") == TARGET_NAME and (TARGET_CWD is None or d.get("cwd") == TARGET_CWD)
]
if len(targets) != 1:
    raise RuntimeError(f"expected one target session, found {len(targets)}")
target = targets[0]
send(target, "codex: the migration finished, tenant_id is the new column")
```

Pick the target by `name`, and set `TARGET_CWD` when two sessions share a name. The recipe refuses
an ambiguous match instead of sending to an arbitrary session. A stale registry file outlives its
process, so let `connect()` fail rather than trusting the file. If a custom `CLAUDE_CONFIG_DIR`
did not survive into Codex, replace `CLAUDE_DIR` with its absolute path before running the recipe.

**`nc -U -z` does not work on macOS.** It returns failure against a socket that is provably
reachable, so a probe built on it reports blocked for everything and proves nothing. To test
reachability, connect for real:

```sh
nc -U /tmp/cc-socks/12345.sock < /dev/null; echo "rc=$?"
```

**`codex exec` cannot ask.** There are no approvals in exec mode. Every grant the send needs
must be on the command line, or the run reports failure instead of stopping to ask.

---

## Limits

- **Plain text only.** No structured payloads, no files, no conversation history.
- **About a million characters** serialized, for a session on this machine. Over that the send
  is refused before it leaves.
- **Bursts are throttled.** Repeated messages are rate-limited per sender, identical repeats
  inside a short window are dropped, and at most 50 accepted messages queue for Claude to read.
  Batch a report into one message rather than sending five.
- **The message has no authority.** It cannot approve a pending permission prompt, change
  `CLAUDE.md`, permissions, or any other configuration, and a slash command in the text arrives
  as plain text and never runs. Write the message as information, not as an instruction that
  assumes consent.

Say who is speaking in the first few words. The receiving Claude is told the text came from
another process rather than from its user, and it will act on that difference.

---

## Verify the path once

From a shell inside a live session, `!` prefix and all:

```sh
{ printf '{"type":"auth","token":"%s"}\n' "$CLAUDE_CODE_MESSAGING_TOKEN"
  printf '%s\n' '{"type":"user","message":{"role":"user","content":"probe"}}'
} | nc -U "$CLAUDE_CODE_MESSAGING_SOCKET"
```

`probe` appearing in that session's transcript confirms that receiver's socket, format, and child
auth path. A later failure is then in target discovery, the caller's environment, peer auth, or
the receiving session's inbound controls rather than the basic payload format.

## Reference

<https://code.claude.com/docs/en/cross-session-messaging> — the socket, the auth line, the
inbound controls, and the own-child rules.

<https://code.claude.com/docs/en/claude-directory> and
<https://code.claude.com/docs/en/env-vars> — config-directory overrides and versioned messaging
environment variables.

<https://developers.openai.com/codex/cli/reference#codex-sandbox> — explicit Unix-socket grants
for Codex sandbox runs.
