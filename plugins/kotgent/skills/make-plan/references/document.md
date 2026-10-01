# Plan document contract

A minimal authored document has `taskRef` and `title`. A useful implementation plan normally adds
sections, decisions and tasks, using only the fields relevant to the work:

```json
{
  "taskRef": "local:42",
  "title": "Export the selected rows",
  "sections": [
    {"kind": "overview", "body": "Export the selected rows as CSV."},
    {"kind": "technical", "body": "Preserve ordering and quote embedded delimiters."}
  ],
  "decisions": [{"title": "Export scope", "body": "Only the selected rows are included."}],
  "tasks": [{
    "ordinal": 1,
    "title": "Implement and verify CSV export",
    "files": [{"path": "src/Export.kt", "action": "create"}],
    "steps": [{"text": "Verify commas, quotes, newlines and empty cells."}],
    "dependsOn": [],
    "agent": "any"
  }]
}
```

Section kinds are `overview`, `context`, `solution`, `technical`, `post`. File actions are `create`,
`modify`, `delete`; agent choices are `claude`, `codex`, `any`. Bodies and steps use Markdown; raw HTML is
dropped and links allow HTTP(S) only. Limits count UTF-8 bytes: document 512 KiB, block body 32 KiB,
thread message 8 KiB.

Omit IDs for new blocks. The daemon generates `s_`, `d_`, `t_`, `st_` IDs; supplying an invented new ID is
rejected. Preserve every existing ID on updates. Omitting an existing block deletes it and closes its
threads. To create dependencies, first put new tasks without dependencies, then put the returned plan
with `dependsOn` naming its generated task IDs. Dependencies must remain acyclic and within the plan.

The plan's `rev` changes with every visible mutation, including viewed marks and discussion. A block's
`rev` changes only when its authored content changes. Always fetch before rewriting and pass the current
plan revision as `--base-rev`. Runtime fields (`status`, worker assignments, step completion, mode,
concurrency, branch) are managed by execution operations and preserved by `put`; do not use them to
simulate approval or progress.
