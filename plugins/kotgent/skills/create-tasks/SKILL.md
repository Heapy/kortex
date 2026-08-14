---
name: create-tasks
description: Create focused kotgent backlog tasks from a user request, specification, issue list, or implementation plan, with repository and session provenance. Use when the user asks to create, add, file, split, or seed tasks in kotgent; do not use for comparing or reviewing an existing backlog.
---

# Create Tasks

Create only tasks justified by the request. Use the `kotgent` CLI for tracker operations and `git` for
repository identity and provenance. Do not use an MCP server, SDK, direct daemon request, or helper script.

## Honor the command contract

- Treat successful `task` and `project` stdout as one JSON value. Treat failure stderr as one JSON object.
- Treat exit `2` as a command-construction bug: stderr may be plain usage text. Stop and report the exact
  command and stderr; do not retry it unchanged.
- Never run `project init`, `project restore`, or `project delete` in this workflow.
- Never delete already-created tasks to roll back a partial batch.
- Require a concrete current kotgent session ID from the invocation context or the user. A provider
  conversation ID is not necessarily a kotgent session ID. Never choose a session by name, cwd, recency,
  or `kotgent list` output.

## Preflight

Complete every check before creating the first task:

1. Run `git --version` and `kotgent --version`. Stop if either command is unavailable.
2. Resolve the repository root with `git rev-parse --show-toplevel`. Stop outside a Git worktree.
3. Read `<root>/.kotgent.json`. Stop if it is missing, invalid, or lacks a UUID-shaped `id`; do not create
   or repair it.
4. Run `kotgent project list` and require exactly one live row whose `id` equals the file's ID. If absent,
   run `kotgent project list --archived` only to distinguish an archived project from an unknown one.
   Stop in either case. Do not initialize, restore, switch projects, or retry.
5. Validate the supplied session with `kotgent task list --session <session-id>`. Stop on any error. If the
   returned backlog is non-empty, require every row's `project` to equal the repository project UUID.
6. Resolve the full commit with `git rev-parse HEAD`; stop if HEAD does not exist or is not a full SHA.
   Resolve the branch with `git symbolic-ref --quiet --short HEAD`; use the literal `detached` only when
   that command reports detached HEAD.
7. Select the exact active host label: `Codex`, `Claude Code`, or `Junie`.

## Shape the batch

- Derive the smallest task set that fully represents the request. Do not create cleanup, documentation,
  testing, refactoring, or follow-up tasks unless the request itself requires them.
- Give each task a specific action-oriented title and a body containing the relevant scope, constraints,
  and acceptance evidence from the request.
- Add a dependency only when the request explicitly states or necessarily defines that one task cannot
  begin before another finishes. Do not infer dependencies merely from list order or implementation taste.
- Plan the entire batch before mutating the tracker, including dependency direction: in
  `task dep add A --on B`, task A depends on task B.

## Add provenance and create sequentially

Append this visible footer to every body, after any task description:

```text
---
Created by: <Codex|Claude Code|Junie>
Kotgent session: <session-id>
Git branch: <branch or detached>
Git HEAD: <full SHA>
```

Do not put the footer in an HTML comment or omit it from an otherwise empty body.

Create one task at a time with concrete values:

```text
kotgent task add <title> --body <body-with-footer> --project <project-uuid> --session <session-id>
```

After each success, parse and record the returned `ref`. Create dependencies only after every required
task exists, using returned refs rather than constructing them:

```text
kotgent task dep add <dependent-ref> --on <prerequisite-ref> --session <session-id>
```

Stop at the first failed add or dependency command. Preserve every successful task and dependency.
Report the successful refs in creation order, the exact failed command, exit code, and complete stderr.
Never issue compensating deletes.

Finish with the created refs, titles, dependency edges, project UUID, session ID, branch, and HEAD. Say
explicitly when the batch is partial.
