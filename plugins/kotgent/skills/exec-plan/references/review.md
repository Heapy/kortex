# Review and finding contract

## Establish the reviewed result

A worker's finish changes its task to `in_review` and opens/increments its task review. Record the exact
worker SHA and its base. Read its diff, test results, repository instructions and changed contracts.
Choose relevant specialist reviewers for the actual risks; use available repository review agents or
independent reviewer contexts with explicit scopes. Do not turn every task into every possible audit.
Reviewers inspect evidence and propose findings; they do not edit the worker branch.

Have a separate verifier challenge each proposed finding against the actual code, reachability, guards
and decisive checks. Independence concerns the reviewing context, not a fabricated Kotgent identity:
subagents in the orchestrator's pane legitimately share its session attribution. The implementation
worker is not its own independent reviewer/verifier. If an essential reviewer/provider is unavailable,
report the missing gate; do not invent a successful independent opinion.

Consolidate duplicates before recording a batch. Preserve legitimate distinct failure conditions, avoid
style-only findings, and associate every finding with the current task/iteration. On resume, inspect
already persisted findings and verifier blocks first. An old event is not permission to duplicate them.

## Persist complete evidence

Write finding JSON to a temporary file and call `kotgent plan finding REF add < finding.json`. Omit IDs
and runtime fields. The daemon generates the finding ID, revision, author and current iteration.
A complete finding has this shape (example values, not a finding to copy):

```json
{
  "taskId": "t_returnedByDaemon",
  "location": "src/Example.kt:42",
  "condition": "A response captured at revision 2 arrives after revision 3.",
  "impact": "The stored revision 3 value is replaced by stale content.",
  "danger": "high",
  "likelihood": "medium",
  "options": [{
    "fix": "Compare the response revision before publishing it.",
    "outcome": "A late response cannot replace newer data.",
    "cost": "low",
    "fit": "high"
  }],
  "recommended": 0
}
```

Scores are low/medium/high; `recommended` and decision options are zero-based. Location is optional but,
when present, must be file:positive-line. Each option needs its outcome, cost and fit. Keep one finding
including its history within 32 KiB.

Persist the independent verifier's own scores with
`kotgent plan finding REF verify FINDING --rev CURRENT_REV < verifier.json`:

```json
{
  "danger": "high", "likelihood": "medium",
  "options": [{"cost": "low", "fit": "high"}],
  "verdict": "confirmed", "reason": "The delayed-response fixture reaches the unguarded write."
}
```

There must be one verifier option score per finding option. The verdict is confirmed or rejected, and
the reason explains why. Rejected findings remain visible and need a decision; never erase them to make
a batch green. `amend` takes full corrected finding JSON and the expected revision, journals old details,
and clears verification/decision. Reverify an amended finding before decisions or feedback. Notes also
advance revisions: always use the latest observed finding revision for the next write. On 409, reread
and reconcile rather than blindly retrying an old body.

## Apply the current review's mode

Read `execution.reviews` for the current task; use its `mode`, not `plan.mode` or `nextMode`.

- **Supervised:** record `plan task REF TASK status awaiting_decision`, share the Plan link and wait.
  The operator chooses fix now (and option), fix later, or won't fix and uses Send to worker. Agents must
  never omit their identity to impersonate that operator. Operator investigation can amend a finding;
  watch current state and reverify changes before expecting a decision. Continue other ready tasks.
- **Autonomous:** send the full verified current batch with
  `plan task REF TASK feedback --findings f_one,f_two`. The assigned worker records decisions with notes,
  acts, notes each disposition, finishes and waits. The orchestrator does not decide for the worker.
- **No findings:** record the independently reviewed SHA, scope and checks in task activity, then enter
  the serial merge gate. A lack of persisted findings alone is not evidence that review happened.

For a batch with only accepted deferred/rejected dispositions and no remaining corrective work, verify
that every finding is verified and decided, record the disposition/backlog work, and proceed to merging.
Otherwise review the next worker result independently. After the third review still needs corrective
work, stop the assigned waiting worker so session-end reconciliation records blocked, preserve its
worktree and findings, and ask for human direction. Do not send another automatic fix loop, recreate the
task or spawn a new child merely to reset the iteration counter. A genuine rebase also produces a new
review; report the counter and why the limit was reached.

For final integrated review, include relevant specialists, Codex through `heapy:call-codex`, and a
separate critical-only pass. Feed verified issues to the final verification/fixer child under these same
rules. Findings fixed on one base need rechecking if the final rebase changes their reachable behavior.
