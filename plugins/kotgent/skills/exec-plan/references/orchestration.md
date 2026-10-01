# Orchestration and recovery

## Feature checkout and scheduling

Inspect the approved plan, Git status/diff, full HEAD, worktree list and default branch before mutations.
Preserve user changes. Create an isolated coordinator worktree when the current checkout is dirty or
belongs to other work. Choose an owned feature branch, reuse the plan's existing branch on resume, and
record a new one with `kotgent plan set REF branch BRANCH`. Never reset or force-checkout somebody else's
branch. If the plan explicitly spans repositories, resolve each task's repository from its instructions;
keep a feature checkout and serial merge queue per repository and verify all of them in the final phase.
Do not guess another repository from a similarly named directory.

A ready task is pending, all its `dependsOn` tasks are done, and there is a concurrency slot. Running,
in-review, awaiting-decision and merging tasks occupy slots. Sort eligible tasks by ordinal. The daemon
checks eligibility atomically; on conflict reread, do not bypass it. For each ready task:

1. `kotgent plan task REF TASK start`.
2. Create its owned branch/worktree from the current integrated feature HEAD. Save that base commit for
   review, or recover it from the branch's merge base on resume. Keep ownership and paths in the prompt.
3. Write the filled worker prompt to a temporary file outside tracked source. Launch
   `kotgent start AGENT /absolute/worktree --name 'Plan TASK' --parent PARENT --task REF --prompt-file /absolute/prompt.md`.
   Choose the plan's provider (`claude`, `codex`; `any` follows the user's preference and available tools).
4. Record the session ID returned by start, never a guessed ID, with
   `kotgent plan task REF TASK worker --worker-session CHILD --branch BRANCH --worktree /absolute/worktree`.

The worker prompt includes a bounded assignment handshake, because the child may run before step 4.
If launch or assignment fails, stop/archive only the newly created child and retain its worktree if it
contains changes. A task started before assignment can be resumed by completing this same launch step;
calling start again is idempotent. Do not create a duplicate worker while the recorded one is live.

Every Kotlin invocation, in coordinator or worker worktrees, uses
`kotgent mutex run kotlin-build -- ./kotlin …`. The mutex belongs to a live session and the wrapper
releases it after the command; do not manually acquire it and then leave an asynchronous build behind.
Run builds/tests in the repository's required order. Other shared resources need their own explicit keys.

## Read runtime truth before each action

`plan show` returns `plan`, `review`, and `execution`. Task reviews have task ID, iteration, fixed `mode`
and `nextMode`. Findings belong to an iteration. Events have monotonically increasing IDs; delivery does
not consume them. Keep one cursor per root/worker caller and plan, advanced only after handling a batch.
On interruption, read both the plan and actual Git/session state before choosing the next operation.

| Task state | Next action |
| --- | --- |
| pending | Schedule only when dependencies and a slot permit. |
| running, no worker | Finish the launch/assignment handshake; inspect any previously created worktree first. |
| running, live assigned worker | Let it work; do not launch a replacement. |
| in_review | Review the exact worker commit against its recorded base and persist verified findings. |
| awaiting_decision | Keep the current mode; supervised decisions and Send belong to the operator. |
| merging | Reconcile Git ancestry first: the merge may have succeeded before its status write. |
| done | Check cleanup; never rerun implementation due to a replayed event. |
| blocked / worker_lost | Preserve work and evidence. Resolve the blocker before retrying; stop any old worker before assigning a replacement. |

A dead orchestrator can be replaced with claim; existing assigned workers retain their original parent
metadata and can still finish/wait. Do not rewrite or fake their parent. A new replacement worker must
be a child of the current orchestrator. Preserve owned dirty work when recovering a dead worker; have a
replacement inspect it rather than resetting it. Exceeding the three-iteration review limit needs human
direction, not a new child used to hide the counter.

Handle events from a fresh document, not blindly from the old event's wording. Duplicate finish/status
notifications must not create duplicate reviews, findings, merges or children. Inspect current finding
IDs, revisions and review iteration to reconcile partially written review batches. A current event cursor
is a delivery checkpoint, never evidence that a task completed.

## Serial merge and cleanup

For a green reviewed task, require all current findings to have verification and decisions. Record
`kotgent plan task REF TASK status merging`. Freeze the reviewed worker SHA and require its worktree
clean. In the coordinator checkout, inspect the actual feature and worker tips again:

- If the worker SHA is already an ancestor of the feature tip, that exact work is already integrated.
  Verify the relevant gates and finish the status/cleanup step, without merging again.
- Otherwise require `git merge-base --is-ancestor FEATURE_SHA WORKER_SHA` before
  `git merge --ff-only WORKER_SHA` on the feature branch. Do not use an unconditional merge, reset, or
  force update when this test fails.
- If feature has advanced, send `kotgent plan task REF TASK feedback --rebase-onto FEATURE_BRANCH`.
  The assigned worker rebases, resolves ordinary conflicts in its own worktree, reruns affected checks,
  finishes and returns to review. Review the new SHA before another merge attempt. Conflicts that require
  product decisions are blockers. Never treat the old review as approval of an unreviewed replacement SHA.

After the merge is confirmed, record `plan task REF TASK status done`, then
`kotgent session done CHILD`. This stops and archives the child without closing the backlog task.
Remove its worktree with ordinary `git worktree remove` only after a clean-status check and proof that
its HEAD is reachable from the feature tip. Keep the branch for review unless deletion was requested.
If cleanup fails, report the retained path and retry safely; do not force removal. If a worker was lost
between a Git merge and the done write, the daemon may record blocked: reconcile the integrated SHA,
then use a replacement verification worker through start/finish/review/done. Do not fake a forbidden
blocked-to-done transition.

## Final phase

Use the plan's explicit final-verification task if it has one, after its implementation dependencies are
done. Otherwise, once execution has begun, append a final task using a fresh `plan show` and
`plan put REF --base-rev REV`; omit its ID and depend on all existing tasks. Preserve started tasks,
dependencies, operator edits and daemon IDs. This is verification of the authorized work, not new product
scope. If a change returns the plan to draft (for example an empty approved plan), obtain the plan review
verdict again before executing it.

Start a final verification/fixer child in its own worktree. Its first pass runs integrated gates and
reports results. On finish, have relevant specialists review the whole integrated diff, ask Codex for an
independent review with `heapy:call-codex`, and run a separate critical-only pass. Record concrete findings
and independently verify them under the same mode rules; send actionable feedback to the final fixer.
Use the same three-iteration limit. No findings is a valid review result, but record the checked scope
and commands in task activity for a restart to distinguish it from a missing review.

Before final integration, rebase the owned feature branch onto the verified local main/default-branch
tip according to repository rules. Preserve a recoverable pre-rebase commit; never force-push. If it
changes the base, request a final-worker rebase through feedback, rerun affected/full required gates,
and repeat the critical review on the resulting SHA. Merge the final worker with the same ancestry gate,
record its task done and clean it up. Recheck the complete diff and summarize statistics relative to the
actual base. Only then complete the plan and submit the backlog task for human review.
