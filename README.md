# kortex

Agent Skills packaged as extensions/plugins for **Junie**, **Codex**, and **Claude Code**.

Installing the plugins is non-destructive: Junie, Claude Code, and Codex install into isolated extension/plugin caches
instead of copying into your personal skills directories.

## Plugins

- `kortex` - Kotlin development skills for Kotlin Toolchain, `.main.kts` scripts, Kotlin/JVM, Multiplatform, and related build workflows.
- `heapy` - Personal skills and Heapy package skills for projects such as `komok`, `kinetica`, and related workflows.
- `kotgent` - Repository-aware backlog creation, synchronization, review, claiming, and execution through the `kotgent` CLI.

## Install

**Codex**

```
codex plugin marketplace add Heapy/kortex
codex plugin add kortex@kortex
codex plugin add heapy@kortex
codex plugin add kotgent@kortex
```

**Claude Code**

```
/plugin marketplace add Heapy/kortex
/plugin install kortex@kortex
/plugin install heapy@kortex
/plugin install kotgent@kortex
```

**Junie**

```
/extensions marketplace add Heapy/kortex
/extensions install kortex
/extensions install heapy
/extensions install kotgent
```

## Usage

**Codex**

```
$kortex:kotlin-toolchain convert current project to Kotlin Toolchain
$kortex:main-kts create an executable Kotlin script that fetches a URL and prints the response
$kortex:modern-kotlin rewrite this service using context parameters and guard conditions
$kortex:ktor add a JSON REST endpoint to this Ktor server and cover it with a test
$heapy:fix-issues fix issue #123 and verify the change
$heapy:amnesia recap this session and tell me what I missed
$heapy:call-codex challenge these review findings with codex
$kotgent:create-tasks create focused backlog tasks from this implementation plan
$kotgent:sync-tasks compare the active backlog with this repository
$kotgent:take-task take the next eligible task and work it through review
```

**Claude Code**

```
/kortex:kotlin-toolchain create a setup for a Kotlin Native CLI application
/kortex:main-kts create an executable Kotlin script that parses a CSV and prints a summary
/kortex:modern-kotlin which experimental flags do I need for collection literals in 2.4
/kortex:ktor set up JWT authentication for this Ktor server
/heapy:fix-issues resolve the failing CI check
/heapy:amnesia recap this session and tell me what I missed
/heapy:call-codex challenge these review findings with codex
/kotgent:create-tasks create focused backlog tasks from this implementation plan
/kotgent:review-tasks audit the todo backlog against the current repository
/kotgent:work-task implement the task linked to this session and submit it for review
```

**Junie**

```
Use the kotlin-toolchain skill to create a setup for a Kotlin Native CLI application
Use the main-kts skill to create an executable Kotlin script that calls an HTTP API
Use the modern-kotlin skill to modernize this code for Kotlin 2.4
Use the ktor skill to build a REST API with an HTTP client integration
Use the fix-issues skill to reproduce and fix this bug
Use the amnesia skill to recap this session and tell me what I missed
Use the call-codex skill to get a second opinion on these findings
Use the create-tasks skill to create focused backlog tasks from this implementation plan
Use the sync-tasks skill to compare the active backlog with this repository
Use the take-task skill to take the next eligible task and work it through review
```
