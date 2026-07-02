---
name: ktor-skill-generation
description: Use when generating or maintaining compressed framework-documentation skills like the Ktor skill from an upstream documentation repository, including source snapshotting, minor-version pinning, splitting into main/server/client references, and verification.
---

# Ktor Skill Generation

Use this skill when recreating, updating, or reviewing the generated `ktor` skill in Kortex. It documents the workflow used
to compress the official Ktor documentation into a practical Hermes skill.

This is a **skill-authoring workflow**, not a Ktor usage guide. For actual Ktor development, load `ktor` and then its
`references/server.md` or `references/client.md` as appropriate.

## Target Layout

The generated Ktor skill lives in the Kortex plugin skills directory:

```text
/home/kokoko/kortex/plugins/kortex/skills/ktor/
├── SKILL.md
└── references/
    ├── server.md
    └── client.md
```

Minimum split required by the user:

- `SKILL.md` — concise entry point, source snapshot, version policy, first moves, common dependencies, minimal server and
  client examples, mental model, pitfalls, verification checklist.
- `references/server.md` — compressed Ktor Server documentation.
- `references/client.md` — compressed Ktor Client documentation.

Keep this split unless the user explicitly asks for a different layout. Additional `references/*.md`, `scripts/`, or
`generation/` files are okay when they add real maintainability.

## Source Inputs Used

The Ktor documentation repository was cloned locally:

```bash
cd /home/kokoko
git clone https://github.com/ktorio/ktor-documentation.git
```

The generated `ktor` skill currently records this snapshot:

- Repository: `https://github.com/ktorio/ktor-documentation`
- Local checkout: `/home/kokoko/ktor-documentation`
- Ref: `main` at the Ktor `3.5.x` documentation line
- SHA: `d118418a65e03d73ad7c2d33bb68e979cebcb4e5`
- Version variables from `v.list`:
  - Ktor: `3.5.1`
  - Kotlin: `2.3.21`
  - Coroutines: `1.11.0`

Primary source files inspected:

- `v.list` for version variables.
- `ktor.tree` for the official information architecture and server/client split.
- `topics/whats-new-350.md` for Ktor 3.5 changes.
- Server topics such as `server-dependencies.topic`, `server-engines.md`, `server-routing.md`, `server-requests.md`,
  `server-responses.md`, `server-serialization.md`, `server-auth.md`, `server-sessions.md`,
  `server-testing.md`, `server-dependency-injection.md`.
- Client topics such as `client-dependencies.md`, `client-create-and-configure.md`, `client-engines.md`,
  `client-requests.md`, `client-responses.md`, `client-serialization.md`, `client-auth.md`,
  `client-response-validation.md`, `client-timeout.md`, `client-plugins.md`, `client-testing.md`.

## Versioning Policy

Version by **Ktor minor line**, matching the style of the `kotlin-toolchain` skill:

- The default `SKILL.md` should be directly useful for the current supported minor line, not just a routing index.
- Record the upstream docs repository, ref/SHA, and version variables in `SKILL.md`.
- State the supported minor line explicitly, for example `Ktor 3.5.x`.
- Do not silently mix behavior-changing guidance from another minor line.
- When updating to a future minor, either update the default skill to the new minor with a new source snapshot or add a
  separate minor-specific reference/snapshot if multiple lines need support.

## Generation Workflow

1. **Verify or clone Kortex and Ktor docs.**

   ```bash
   cd /home/kokoko
   test -d kortex/.git
   test -d ktor-documentation/.git || git clone https://github.com/ktorio/ktor-documentation.git
   git -C ktor-documentation rev-parse HEAD
   git -C ktor-documentation status --short --branch
   ```

2. **Read the existing Kortex skill conventions.**

   Use `kotlin-toolchain` as the versioning/layout precedent:

   ```text
   /home/kokoko/kortex/plugins/kortex/skills/kotlin-toolchain/SKILL.md
   ```

   Important conventions copied from it:

   - source snapshot section
   - explicit upstream SHA/ref
   - default guide remains operational, not only an index
   - minor-line/version sensitivity is called out
   - linked files are referenced from `SKILL.md`

3. **Extract docs structure from `ktor.tree`.**

   Use the TOC to decide what belongs in server vs client references.

   Server sections observed:

   - Getting started
   - Developing applications
   - creating/configuring server, engines, configuration, modules, DI, plugins
   - routing, requests, responses
   - serialization
   - static content/templates
   - authentication, sessions
   - HTTP plugins
   - WebSockets, SSE, sockets
   - monitoring/admin
   - running/debugging, testing, deployment, extending Ktor

   Client sections observed:

   - Getting started
   - supported platforms, dependencies, create/configure, engines, plugins, SSL/proxy
   - requests, responses, serialization
   - auth, cookies, content encoding, cache, text/charsets, timeout, logging
   - WebSockets, SSE
   - monitoring, custom plugins, testing

4. **Collect high-value details, not every paragraph.**

   The goal is compressed operational guidance, not a mirror of the docs. Keep:

   - required Maven artifacts
   - canonical imports and minimal examples
   - install/configure patterns
   - version-specific changes and migration traps
   - testing guidance
   - production pitfalls
   - engine/platform limitations

   Drop or aggressively summarize:

   - marketing/explanatory prose
   - duplicated examples
   - long tutorial narratives
   - platform guides that only matter for deployment details unless they affect daily coding

5. **Write the main `SKILL.md`.**

   Include:

   - frontmatter with `name: ktor` and a broad trigger description
   - what the skill is and is not
   - source snapshot
   - minor-version policy
   - first moves in a repo
   - common coordinates and artifacts
   - minimal server and client shapes
   - mental model
   - frequent pitfalls
   - verification checklist

6. **Write `references/server.md`.**

   Organize by tasks an agent performs:

   - setup and engines
   - startup styles: `embeddedServer` and `EngineMain`
   - application structure
   - configuration and DI
   - routing
   - requests and responses
   - serialization/content negotiation
   - plugins
   - auth/session details
   - static content/templates
   - WebSockets/SSE
   - testing
   - deployment checklist

7. **Write `references/client.md`.**

   Organize by tasks an agent performs:

   - setup and engines
   - client lifecycle
   - KMP pattern
   - requests and bodies
   - responses and streaming
   - serialization/content negotiation
   - validation/errors
   - timeout/retry/redirects
   - default headers/user-agent
   - auth/cookies/cache
   - logging/tracing
   - proxy/SSL
   - WebSockets/SSE
   - custom plugins
   - testing with `MockEngine`
   - engine selection notes

8. **Verify the generated files.**

   Check file presence, frontmatter, links, and git status:

   ```bash
   cd /home/kokoko/kortex
   git status --short
   python3 - <<'PY'
   from pathlib import Path
   base = Path('plugins/kortex/skills/ktor')
   files = [base/'SKILL.md', base/'references/server.md', base/'references/client.md']
   for p in files:
       txt = p.read_text()
       print(f'{p}: {len(txt)} bytes, {txt.count(chr(10)) + 1} lines')
   main = files[0].read_text()
   assert main.startswith('---\n') and '\n---\n' in main
   assert 'name: ktor' in main and 'description:' in main
   assert '3.5.x' in main
   assert 'references/server.md' in main
   assert 'references/client.md' in main
   print('OK')
   PY
   ```

## Implementation Notes from the Original Run

The original generation used these concrete actions:

1. Load `hermes-agent` because this is Hermes skill authoring.
2. Inspect `kotlin-toolchain` in `/home/kokoko/kortex/plugins/kortex/skills/kotlin-toolchain/SKILL.md` to copy the
   source-snapshot/minor-version style.
3. Inspect the Ktor docs checkout with file search and direct reads.
4. Use `v.list` to capture `ktor_version`, `kotlin_version`, and `coroutines_version`.
5. Use `ktor.tree` to confirm there are official top-level `Ktor Server` and `Ktor Client` sections.
6. Summarize representative topic headings/artifacts from server and client docs.
7. Create files with direct file writes under the Kortex plugin path, not user-local `~/.hermes/skills`, because this is
   an in-repository bundled plugin skill.
8. Verify with a small Python assertion script and `git status --short`.

## Quality Bar

A generated documentation skill is good when:

- It can be loaded and immediately guide code changes without opening the full docs for common tasks.
- It includes enough coordinates/imports/examples to prevent common hallucinations.
- It is clear about what version/minor line it represents.
- It tells the agent when to inspect the actual project instead of relying on generic guidance.
- It splits long material into references so the main skill stays readable.
- It has a concrete verification checklist.

## Common Pitfalls

- Creating the skill with `skill_manage(action='create')` when the user wants it inside the Kortex repo. Use file writes
  to `/home/kokoko/kortex/plugins/kortex/skills/...` for in-repo plugin skills.
- Forgetting source SHA/ref/version variables, which makes the compressed docs unverifiable later.
- Making `SKILL.md` only an index. The default guide should still contain practical first-response guidance.
- Overfitting to one tutorial and missing major docs areas from `ktor.tree`.
- Copying too much upstream prose instead of compressing to decision-making guidance.
- Mixing server and client imports/artifacts.
- Omitting `.main.kts`/KMP caveats for Ktor client dependencies.

## Maintenance Checklist

When updating the Ktor skill:

- [ ] Pull or clone the desired Ktor docs snapshot.
- [ ] Record new docs SHA and version variables.
- [ ] Compare `ktor.tree` against the previous structure for new/removed topics.
- [ ] Read the `whats-new-<minor>.md` file for version-specific additions and migrations.
- [ ] Update `SKILL.md`, `references/server.md`, and `references/client.md` consistently.
- [ ] Preserve minor-line versioning language.
- [ ] Verify frontmatter and links.
- [ ] Run `git diff -- plugins/kortex/skills/ktor plugins/kortex/skills/ktor-skill-generation` before reporting.
