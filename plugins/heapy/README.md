# heapy

Agent skills for personal workflows and Heapy package work, including projects such as `komok`, `kinetica`, and related packages.

## What it covers

- Personal engineering workflows.
- Heapy package work for `komok`, `kinetica`, and similar packages.
- Issue fixing workflows: capture the source, reproduce or localize, diagnose, implement, verify, and hand off.
- Session recaps after stepping away from a long-running session.
- Driving the codex CLI, both as an adversarial reviewer of findings and as an executor of scoped tasks.

## Files

- `skills/fix-issues/SKILL.md` - repository issue triage, implementation, and verification workflow.
- `skills/amnesia/SKILL.md` - session recap: start time, last user message, and what changed since.
- `skills/call-codex/SKILL.md` - invoking the codex CLI as reviewer or executor: sandbox grants, flags, prompt shape, and integration traps.

## Usage

Ask the agent to use a skill explicitly when needed:

```text
Use the fix-issues skill to resolve the failing CI check
Use the amnesia skill to recap this session and tell me what I missed
Use the call-codex skill to challenge these review findings
```
