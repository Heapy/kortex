---
name: take-task
description: Safely claim a specific or next eligible kotgent task from a free session, detect blocked or concurrent work, and immediately continue through the work-task workflow. Use when the user asks to take, claim, pick up, start, or work the next kotgent task, optionally naming a task ref.
---

# Take Task

Claim work only from a free kotgent session. Use the `kotgent` CLI for tracker operations and `git` for
repository state. Do not use an MCP server, SDK, direct daemon request, or helper script.

## Require exact identity

Require a concrete current kotgent session ID from the invocation context or user. Never infer it from
provider conversation IDs, names, cwd, recency, or `kotgent list`. Require `git` and `kotgent` on `PATH`
and a Git worktree, but make the following the first `kotgent task` command:

```text
kotgent task show --session <session-id>
```

Interpret it strictly:

- Exit `0` means the session already holds a task. Report that task and stop; do not claim or call `next`.
- Continue only on exit `1`, empty stdout, no `status` field, and the exact JSON error text
  `session '<session-id>' is not linked to a task — name one: kotgent task <command> <ref> --session <session-id>`.
- Treat every other response as a real failure and stop. Exit `2` is a command-construction bug whose
  stderr may be plain usage text.

Never use `task unlink` to make a session appear free.

## Resolve a candidate

When no ref was supplied:

1. Resolve the Git root, read its `.kotgent.json`, and require the exact project UUID in
   `kotgent project list`. If it is absent or archived, stop; never initialize, restore, switch, or retry.
2. Run:

   ```text
   kotgent task next --project <project-uuid> --session <session-id>
   ```

3. Treat exit `3` with `{"task":null}` as normal absence of work and stop successfully. Treat any other
   nonzero exit as failure. Record the returned ref; do not call `next` again.

When a ref was supplied:

1. Run `kotgent task show <ref> --session <session-id>` before claiming.
2. Require `task.state == "todo"` and `task.blocked == false`. Stop for blocked work and for
   `in_progress`, `review`, or `done`; never auto-claim those states.
3. Require the task's project UUID in `kotgent project list`. Stop if it is unknown or archived.
4. Run `kotgent task claim <ref> --session <session-id>` exactly once.

Task links are non-exclusive. A successful claim is not proof of ownership.

## Detect parallel work after claiming

Immediately run `kotgent task show <ref> --session <session-id>` again and inspect the refreshed task,
complete activity feed, and every linked session. Also inspect `git status --short`, the current branch or
detached state, full HEAD, and diffs relevant to the task.

Treat these as parallel-work signals unless the user already established a division of work:

- another non-archived, live linked session;
- comments or links from another session indicating active implementation;
- uncommitted changes overlapping the task that this session did not create;
- recent commits or task transitions showing someone else is already executing the same scope.

On any signal, leave one concrete coordination comment naming the sessions, paths, activity, or commits:

```text
kotgent task comment <ref> -m <coordination-evidence-and-request> --session <session-id>
```

Then stop with the task left `in_progress` and linked. Do not unlink it, claim another task, or race the
other worker.

If no conflict exists, immediately continue with the `work-task` workflow from this plugin, carrying the
ref, session ID, refreshed detail, and Git snapshot forward. Do not stop after claiming and do not claim
the task again. Never run `task done`, and never run `task next` after review.
