# heapy

Agent skills for personal workflows and Heapy package work, including projects such as `komok`, `kinetica`, and related packages.

## What it covers

- Personal engineering workflows.
- Heapy package work for `komok`, `kinetica`, and similar packages.
- Issue fixing workflows: capture the source, reproduce or localize, diagnose, implement, verify, and hand off.
- Session recaps after stepping away from a long-running session.
- Driving the codex CLI, both as an adversarial reviewer of findings and as an executor of scoped tasks.
- Posting a message into a running Claude Code session from outside it: the inbox socket, its payload, and its delivery rules.
- Auditing Claude Code project memory: verifying what is still true, and pruning or relocating what is not.
- Cutting low-value comments and agent guidance down to what actually carries non-obvious intent.

## Files

- `skills/fix-issues/SKILL.md` - repository issue triage, implementation, and verification workflow.
- `skills/amnesia/SKILL.md` - session recap: start time, last user message, and what changed since.
- `skills/call-codex/SKILL.md` - invoking the codex CLI as reviewer or executor: sandbox grants, flags, prompt shape, and integration traps.
- `skills/message-claude/SKILL.md` - posting into a Claude Code session's inbox socket from an external agent or script: finding the session, the two tokens, and the codex sandbox and environment traps.
- `skills/clean-claude-memory/SKILL.md` - reviewing project memory file by file: keep, rewrite, delete, or move, then rebuild `MEMORY.md`.
- `skills/clean-comments-and-guidance/SKILL.md` - what to delete and what to keep across comments, KDoc, `CLAUDE.md`, and `AGENTS.md`, without changing behavior.

## Usage

Ask the agent to use a skill explicitly when needed:

```text
Use the fix-issues skill to resolve the failing CI check
Use the amnesia skill to recap this session and tell me what I missed
Use the call-codex skill to challenge these review findings
Use the message-claude skill to report this result back to the Claude session that started me
Use the clean-claude-memory skill to review this project's memory and drop what is stale
Use the clean-comments-and-guidance skill to prune the comments in this module
```
