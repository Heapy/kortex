# heapy

Agent skills for personal workflows and Heapy package work, including projects such as `komok`, `kinetica`, and related packages.

## What it covers

- Personal engineering workflows.
- Heapy package work for `komok`, `kinetica`, and similar packages.
- Issue fixing workflows: capture the source, reproduce or localize, diagnose, implement, verify, and hand off.
- Session recaps after stepping away from a long-running session.
- Driving the codex CLI, both as an adversarial reviewer of findings and as an executor of scoped tasks.
- Auditing Claude Code project memory: verifying what is still true, and pruning or relocating what is not.

## Files

- `skills/fix-issues/SKILL.md` - repository issue triage, implementation, and verification workflow.
- `skills/amnesia/SKILL.md` - session recap: start time, last user message, and what changed since.
- `skills/call-codex/SKILL.md` - invoking the codex CLI as reviewer or executor: sandbox grants, flags, prompt shape, and integration traps.
- `skills/clean-claude-memory/SKILL.md` - reviewing project memory file by file: keep, rewrite, delete, or move, then rebuild `MEMORY.md`.

## Usage

Ask the agent to use a skill explicitly when needed:

```text
Use the fix-issues skill to resolve the failing CI check
Use the amnesia skill to recap this session and tell me what I missed
Use the call-codex skill to challenge these review findings
Use the clean-claude-memory skill to review this project's memory and drop what is stale
```
