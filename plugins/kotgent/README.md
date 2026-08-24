# kotgent

Agent skills for turning repository work into a disciplined kotgent backlog and carrying a task safely
from selection through human review. Tracker operations use only the `kotgent` CLI; repository evidence
and provenance come from `git`. The plugin includes no MCP server, SDK, or helper scripts.

## Requirements

- `git` and `kotgent` available on `PATH`, with the kotgent daemon reachable.
- A Git worktree. Existing-backlog workflows require a valid `.kotgent.json` and registered project;
  `create-tasks` can create or adopt them through the current session context.
- Mutating workflows run from the live kotgent pane by default. A caller outside the pane must supply an
  exact session ID explicitly; skills never treat `KOTGENT_SESSION_ID` as identity or guess from names,
  paths, recency, or `kotgent list`.

## Skills

- `create-tasks` creates only request-derived tasks, with session and Git provenance in each body.
- `sync-tasks` compares `todo` and `in_progress` tasks with repository guidance, plans, code, tests,
  TODOs, and relevant history without changing either side.
- `review-tasks` audits `todo` tasks for stale, duplicate, absorbed, contradictory, or incorrect work.
- `take-task` claims an eligible task only from a free session, then checks for concurrent work.
- `work-task` implements the linked task, reports meaningful progress or blockers, verifies the result,
  and moves the task to `review` without closing or unlinking it.

`sync-tasks` and `review-tasks` are always read-only. Task links are not exclusive, so `take-task` and
`work-task` inspect linked sessions, activity, and the worktree before proceeding.

## Usage

Codex:

```text
$kotgent:create-tasks create focused tasks from this implementation plan
$kotgent:sync-tasks compare the active backlog with this repository
$kotgent:review-tasks audit the todo backlog against current repository evidence
$kotgent:take-task take the next eligible task and work it through review
$kotgent:work-task implement the task linked to this session and submit it for review
```

Claude Code uses the same names with `/kotgent:<skill>`. In Junie, ask it to use the corresponding
`create-tasks`, `sync-tasks`, `review-tasks`, `take-task`, or `work-task` skill.
