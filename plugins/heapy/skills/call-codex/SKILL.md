---
name: call-codex
description: Use when the codex CLI should be driven from the agent — as a reviewer giving a second, independent opinion ("ask codex", "call codex", "codex review", "get a second opinion", "challenge this finding"), or as an executor doing a scoped task in the repository ("have codex do this", "delegate this to codex", "let codex implement it"). Covers which flags codex actually understands, how much access each mode needs, how to write a prompt that produces verdicts instead of agreement, and the integration traps that waste a run.
---

# Call Codex

## Two modes

|                     | Reviewer                      | Executor                                 |
|---------------------|-------------------------------|------------------------------------------|
| Job                 | break a claim                 | do a scoped task                         |
| Sandbox             | `read-only`, always           | `workspace-write` plus what the task needs |
| The output that matters | the disagreement          | the diff                                 |
| Verified by         | reading the code              | reading the diff and running the tests   |

One binary, one set of traps, two different grants and two different prompts. Picking the
wrong grant is the usual failure: a reviewer with write access starts fixing instead of
judging, and an executor without network dies on the first dependency fetch.

## Prerequisites

Check `which codex`. Report and stop if it is absent.

`~/.codex/config.toml` supplies defaults (`model`, `model_reasoning_effort`, `sandbox_mode`,
`sandbox_workspace_write.*`). Pass everything the run depends on explicitly anyway: behavior
should not change because a default moved.

---

# Mode 1 — Reviewer

A second reader for code that is already understood. Not a search tool, not a replacement for
reading the code. It earns its cost in one situation: a claim exists — a review finding, a
diagnosis, a design decision — and an independent model should try to break it.

Codex analyzes; the calling agent implements. Never let this mode write.

## When to use

- A review produced findings and their reliability matters before acting on them.
- A diagnosis is plausible but unproven and a wrong fix would be expensive.
- Two explanations of the same failure both fit and the difference changes the fix.
- The user explicitly asks for codex.

## When not to use

- To find code or navigate a repository. Grep is faster and free.
- To confirm something already verified. A run costs minutes and real tokens.
- To decide anything the user has already decided.

## Invocation

Write the prompt to a file, then run in the background — a `max` effort review takes
10-15 minutes.

```sh
codex exec -m gpt-5.6-sol \
  --sandbox read-only \
  -c model_reasoning_effort="max" \
  -c stream_idle_timeout_ms=900000 \
  -o /abs/path/codex-answer.md \
  "$(cat /abs/path/prompt.md)" < /dev/null
```

`read-only` really is read-only. Codex can run terminating read-only commands (`git show`,
`rg`, `strings`), but it cannot start a server, a tmux socket, or a daemon. State that limit
in the prompt and ask for an exact manual reproduction plan instead — it produces good ones.

## Prompt shape

A prompt that only describes the task produces agreement. Four parts produce verdicts.

- **Role and stance.** Say the job is to break confidence in the claim, and that the
  skepticism points at the *claim*, not at the code.
- **Grounding.** Every verdict must quote code, spec text, or a command that was run.
  A verdict without quoted evidence is not a verdict. Inferences must be labeled.
- **A verdict vocabulary.** `CONFIRMED` / `REFUTED` / `PARTIAL` / `UNPROVEN`, one per
  claim, plus: when the stated *mechanism* is wrong but the *conclusion* survives (or the
  reverse), say so explicitly. That distinction is usually the most useful output.
- **Output format.** Name the sections and their order, and say "no preamble". Ask for
  what was MISSED and for a fix ranking that includes what NOT to fix and why.

For anything the code cannot settle — device behavior, runtime timing, a real TUI — ask
for the minimal live reproduction: the exact commands, what to observe, and **what result
would refute the claim**. A claim no observation can falsify is worthless; say that in the
prompt and codex will flag them.

Point codex at file paths, not file contents. It has the repository.

## Reading the result

Report to the user in this order: where codex **disagrees** with the current conclusion,
what it says was missed, and its ranking. Agreement is the least informative part.

Then check the disagreements against the code yourself before changing anything, and stop.
Codex findings are input for a decision, not a work order.

---

# Mode 2 — Executor

Codex does the work: writes files, runs the build, resolves dependencies. Worth delegating a
task that is well-specified, verifiable by a command, and separable from what the calling
agent is doing. Not worth it for anything that needs a conversation to pin down — exec mode
cannot have one.

## Invocation

```sh
codex exec -m gpt-5.6-sol \
  --sandbox workspace-write \
  -c sandbox_workspace_write.network_access=true \
  -c tools.web_search=true \
  -c model_reasoning_effort="high" \
  -c stream_idle_timeout_ms=900000 \
  -C /abs/path/to/repo \
  --add-dir /abs/path/to/second/checkout \
  -o /abs/path/codex-answer.md \
  "$(cat /abs/path/task.md)" < /dev/null
```

What each grant buys:

- `--sandbox workspace-write` — writes under the working root only; the rest of the disk stays
  readable. `--add-dir` adds another writable root for this run;
  `sandbox_workspace_write.writable_roots` in `config.toml` is where build caches belong
  (`~/.gradle`, `~/.konan`, Kotlin caches) — a build fails without them and the error names a
  path, not a permission.
- `-c sandbox_workspace_write.network_access=true` — dependency resolution, `git fetch`, curl.
  Without it the failure looks like a broken repository, not a blocked socket.
- `-c tools.web_search=true` — the native web-search tool. `codex exec` has no `--search`
  flag; only the interactive CLI does.
- `--dangerously-bypass-approvals-and-sandbox` — no sandbox at all. Only when the whole run is
  already isolated (container, throwaway VM). Never on a working machine.

**exec mode cannot ask.** There are no approvals in `codex exec` — no `-a/--ask-for-approval`,
and an escalation request fails with *"permissions approval is not supported in exec mode"*.
The model does not stop to ask; it works around the wall or reports failure. Every permission
the task needs must be on the command line. When the needed access cannot be predicted, run
the interactive `codex -a on-request` and let the user answer.

## Containment

Give the executor its own branch or `git worktree` — it costs nothing and makes `git diff` and
`git checkout .` the entire rollback story. Never point one at a tree with uncommitted work
that matters: once its edits mix with yours, they cannot be told apart.

## Prompt shape

Execution prompts fail in the opposite direction from review prompts: not agreement, but
scope creep. Pin the edges.

- **Scope.** Which files or directories it may touch, and what to leave alone.
- **Definition of done, as a command.** `./gradlew :module:test`, `kotlin build`, a script
  that must exit zero. A prose goal produces a prose report.
- **Behavior when blocked.** Stop and report; do not invent a workaround, do not widen the
  scope, do not disable a failing test to make the command pass.
- **Conventions.** Spell out the ones this task depends on. Codex picks up `AGENTS.md` on
  its own and nothing else (rule 2).
- **Hands off git.** No commits, no branch switching, no `git add` — leave the working tree
  dirty so the diff is the deliverable. If a commit is wanted, dictate exactly what goes in it.
- **Final report.** What changed and why, what was deliberately not done, what remains
  unverified.

## Reading the result

The final message is a claim; `git diff` is the evidence. Read the diff first, then the
report, then run the done-criteria command yourself. "Codex says it passed" and "it passes"
are two different facts, and only the second one is reportable to the user.

---

# Integration rules (both modes)

These cost time to rediscover. Respect them.

1. **Always redirect stdin: `< /dev/null`.** `codex exec` reads stdin to append a
   `<stdin>` block even when the prompt is a positional argument. Under a background
   launch the inherited pipe never closes and codex blocks forever on "Reading additional
   input from stdin…".

2. **Codex reads `AGENTS.md`, and nothing else is yours to hand it.** It auto-loads
   `AGENTS.md` from the repository. Do not paste `CLAUDE.md` into the prompt and do not
   point codex at it by path — neither the project one nor the global
   `~/.claude/CLAUDE.md`. Whether a repository carries guidance for codex is the user's
   decision, expressed by the presence of `AGENTS.md`; a repository without one is meant to
   run without one. Task-specific conventions belong in the prompt, written out.

   `@file` is inert in `codex exec` — it is literal text, not an import.

3. **Read the answer from `-o`, never from stdout.** Stdout carries hook lines, a `codex`
   marker, `tokens used` with a count, and the final answer **printed twice**. The `-o`
   file holds exactly the final message (no trailing newline).

4. **Budget the run.** Fourteen findings at `max` cost ≈240k tokens and ~14 minutes. A
   trivial question at `low` costs seconds. Match the effort to the question — and for an
   executor, to the size of the diff, not to the difficulty of the phrasing.

5. **Verify what it claims.** Codex is a second opinion, not an oracle. When it refutes a
   finding, confirm the refutation in the code before acting on it — and when it confirms
   one, that is not proof either. Its value is the disagreement, which is where to look.

## Flags that matter

| Flag | Meaning |
|---|---|
| `-m, --model <MODEL>` | model id, e.g. `gpt-5.6-sol` |
| `-c <key=value>` | any config override; value parsed as TOML, falls back to a literal string |
| `-c model_reasoning_effort=` | `low` … `xhigh`, `max`. `low` answers in seconds, `max` reasons for minutes |
| `-c stream_idle_timeout_ms=` | raise for long reasoning; a deep review can sit silent for minutes |
| `-c sandbox_workspace_write.network_access=` | network inside `workspace-write`; also `writable_roots`, `exclude_slash_tmp` |
| `-c tools.web_search=` | live web search in `exec` (no `--search` flag there) |
| `-s, --sandbox <MODE>` | `read-only`, `workspace-write`, `danger-full-access` |
| `--dangerously-bypass-approvals-and-sandbox` | no sandbox, no prompts; externally isolated environments only |
| `-o, --output-last-message <FILE>` | writes ONLY the final answer to a file — the one reliable way to read the result |
| `-C, --cd <DIR>` | working root; `--add-dir` adds another writable dir, `--skip-git-repo-check` allows running outside git |
| `--json` | events as JSONL, for machine consumption |
| `--output-schema <FILE>` | JSON Schema the final response must satisfy |
| `-i, --image <FILE>` | attach screenshots to the prompt |
| `--ephemeral` | do not persist the session; `--ignore-user-config` ignores `config.toml`; `--ignore-rules` drops execpolicy `.rules` |
| `-p, --profile <NAME>` | layer `$CODEX_HOME/<name>.config.toml` on the base config |

## Subcommands

- `codex exec resume <SESSION_ID|--last> "follow-up"` — continue the same conversation
  instead of re-explaining the context. Use it for "you said X, but line N says Y", and for
  "the build you handed back fails at line N — fix that, nothing else".
- `codex exec review [--uncommitted | --base <BRANCH> | --commit <SHA>] [--title <T>]` —
  the built-in review, when a plain diff review is wanted and no custom stance is needed.

## When this file is wrong

Everything above was checked against `codex-cli 0.146.0`. Flags, config keys, and sandbox
behavior move between releases; `codex --version` is the first thing to compare when
something does not line up.

A mismatch is a finding, not an obstacle. Do not quietly route around it.

- Confirm it first: `codex exec --help`, or a probe that costs no model run —
  `codex sandbox -c sandbox_mode=<mode> -- /bin/sh -c '<command>'` settles sandbox and
  network questions in seconds.
- If the correction is local and certain, edit this file in the same session and say what
  changed.
- Otherwise open an issue on `Heapy/kortex` with the version, the exact command, what was
  expected, and what happened.
- Either way, tell the user. A trap rediscovered in silence gets rediscovered again.
