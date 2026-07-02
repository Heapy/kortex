# Ktor skill generation steps

Generated on 2026-07-19 in `/Users/yoda/dev/pet/kortex`.

## Goal

Create and maintain the compressed `plugins/kortex/skills/ktor/SKILL.md` from the official Ktor documentation by:

1. Cloning the upstream Ktor documentation repository.
2. Pinning the snapshot to a ref/SHA and a Ktor minor line.
3. Compressing the docs into a main skill plus server/client references.
4. Verifying frontmatter, links, and file structure.

This is a skill-authoring log, not a Ktor usage guide. For actual Ktor development, load `SKILL.md` and then
`references/server.md` or `references/client.md` as appropriate.

## Target Layout

```text
plugins/kortex/skills/ktor/
├── SKILL.md
├── references/
│   ├── server.md
│   └── client.md
└── generation/
    └── generation-steps.md
```

Minimum required split:

- `SKILL.md` — concise entry point, source snapshot, version policy, first moves, common dependencies, minimal server and
  client examples, mental model, pitfalls, verification checklist.
- `references/server.md` — compressed Ktor Server documentation.
- `references/client.md` — compressed Ktor Client documentation.

Keep this split unless a different layout is explicitly requested. Additional `references/*.md`, `scripts/`, or
`generation/` files are okay when they add real maintainability.

## Source Inputs Used

The Ktor documentation repository was cloned into a temporary directory:

```shell
git clone https://github.com/ktorio/ktor-documentation.git /private/tmp/ktor-documentation
```

The generated `ktor` skill currently records this snapshot:

- Repository: `https://github.com/ktorio/ktor-documentation`
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

1. **Clone or update the Ktor docs checkout.**

   ```shell
   test -d /private/tmp/ktor-documentation/.git || git clone https://github.com/ktorio/ktor-documentation.git /private/tmp/ktor-documentation
   git -C /private/tmp/ktor-documentation rev-parse HEAD
   git -C /private/tmp/ktor-documentation status --short --branch
   ```

2. **Read the existing Kortex skill conventions.**

   Use `plugins/kortex/skills/kotlin-toolchain/SKILL.md` as the versioning/layout precedent:

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

   ```shell
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

## Quality Bar

A generated documentation skill is good when:

- It can be loaded and immediately guide code changes without opening the full docs for common tasks.
- It includes enough coordinates/imports/examples to prevent common hallucinations.
- It is clear about what version/minor line it represents.
- It tells the agent when to inspect the actual project instead of relying on generic guidance.
- It splits long material into references so the main skill stays readable.
- It has a concrete verification checklist.

## Common Pitfalls

- Writing the skill outside the repo (for example into a user-local skills directory). Bundled plugin skills live under
  `plugins/kortex/skills/`.
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
- [ ] Run `git diff -- plugins/kortex/skills/ktor` before reporting.