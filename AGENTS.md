# Agent instructions

Conventions for working in this repository. Follow them when making changes.

## Module layout
- `idporten-access-log-common` — shared production classes/resources compiled against the
  Spring Boot 3 baseline using `provided` dependencies. No runtime dependencies are forced on consumers.
- `idporten-access-log-spring-boot-3-starter` / `-4-starter` — thin starters that bring their own
  Spring Boot, Tomcat, Jackson, Logback and logback-access versions.
- `idporten-access-log-shared-test` — a source-only directory (no `pom.xml`, not a module).
  Both starters add it as a test-source root via `build-helper-maven-plugin` so one set of shared
  tests compiles and runs against each starter's own dependency set.
  *Why:* the shared tests must execute against **both** runtime dependency sets (Boot 3 and Boot 4) to
  actually catch version-specific breakage; a plain test-jar would compile once and lose that coverage,
  and copying the tests would let the two copies drift.

## Dependency & version pinning (both starters must stay uniform)
*Why uniform:* the two starters differ only by major platform version, so keeping identical structure makes
diffs reviewable and prevents a fix landing in one starter but not the other.
- Declare every third-party version as a property in `<properties>`.
  *Why:* one obvious place to see/change a version, and it lets the Maven `<name>` surface them.
- Pin every third-party version in `<dependencyManagement>`, grouped in this order:
  1. BOM imports (`jackson-bom`, then `spring-boot-dependencies`)
  2. Logback family (`logback-core`, `logback-classic`, `logback-access-tomcat`)
  3. Other third-party (`logstash-logback-encoder`, `tomcat-catalina`)
  *Why dependencyManagement (not inline):* a managed version overrides versions pulled in **transitively**
  (e.g. logback-core dragged in by logback-access and Spring Boot), which an inline version on a direct
  dependency does not reliably do.
  *Why jackson-bom is imported before spring-boot-dependencies:* for imported BOMs the **first** declaration
  wins, so jackson-bom must come first to override the Jackson version managed by the Spring Boot BOM.
  (Flipping this silently downgraded Boot 4 Jackson 3.1.7 → 3.1.5 — caught by Trivy, not the build.)
- In `<dependencies>` declare only `groupId`/`artifactId`/`scope` — never an inline `<version>`
  (the reactor module `idporten-access-log-common` uses `${project.version}` and is the only exception).
  *Why:* a single source of truth for versions (the management block) avoids a dependency being pinned in
  two places that drift apart.
- Keep the two starter POMs structurally identical: same property order, same dependency order,
  same scopes. Only values (versions) and the Jackson groupId (`com.fasterxml.jackson` vs `tools.jackson`)
  may differ.

## Logback compatibility
- logback-access runs a startup version check and logs `For logback-core, expected version X but found Y`
  on a mismatch. Pin `logback-core`/`logback-classic` (`${logback.version}`) to the version expected by the
  starter's `logback-access` version. The expected version lives in the access jar at
  `ch/qos/logback/access/common/logback-access-common-dependencies.properties`.
- `logback-core`/`logback-classic` are `provided`: the pin protects this project's build/tests only and is
  not transitive to consumers. Consumers' own Spring Boot BOM controls their Logback version.
- A shared safety-net test fails the build if the mismatch warning appears. Keep it free of hard-coded
  version numbers — assert on the warning *pattern* only, so it keeps working across future version bumps.
- Reflect the compatible versions in each starter's Maven `<name>` and in the README compatibility section.

### Logback upgrade — pitfalls (read before bumping any Logback artifact)
> [!WARNING]
> Logback, logback-access, Spring Boot and the Tomcat major line are a tightly coupled set.
> Bumping one without the others frequently breaks silently (a startup `WARN`, not a build failure).
- **Never bump one in isolation.** `logback.version` and `logback-access.version` must move together.
  After changing either, re-check the access jar's expected core version
  (`ch/qos/logback/access/common/logback-access-common-dependencies.properties`) and align `logback.version` to it.
- **Patch bumps can break too.** logback-access pins an *exact* logback-core version; even a patch mismatch
  (e.g. `1.5.37` vs `1.5.38`) triggers the startup warning. Do not assume semver compatibility.
- **Dependabot updates logback-core/classic independently.** PRs that bump only `logback.version`
  (or only `logback-access.version`) are the most common source of mismatch — scrutinise them and bump the pair.
- **Spring Boot upgrades move Logback transitively.** When bumping `spring-boot.version`, the BOM may pull a
  new logback-core; our `dependencyManagement` pin overrides it, so re-verify the pin still matches logback-access
  rather than silently diverging from the BOM.
- **Tomcat major must match the access artifact.** `logback-access-tomcat` targets a specific Tomcat major
  (10.x ↔ `tomcat.version` 10.1.x, 11.x ↔ 11.0.x). Keep `tomcat.version` on the matching line.
- **Jackson major is starter-specific.** Boot 3 uses `com.fasterxml.jackson` (Jackson 2); Boot 4 uses
  `tools.jackson` (Jackson 3). Logback/logstash encoder upgrades must keep using the matching Jackson API
  (`writeStringField` vs `writeStringProperty`) — a logstash-logback-encoder bump can flip this.
- **Always validate after any Logback-related bump**: run the build (so the shared compatibility safety-net
  runs in both starters) and run `mvn -pl <starter> dependency:tree` to confirm logback-core/classic/access
  resolve to the intended, mutually-compatible versions. A green build of that safety-net is the gate.

## Tests
- Put tests that are identical across both starters in `idporten-access-log-shared-test`; never duplicate them.
  *Why:* duplicated tests drift — a fix or new case added to one starter silently misses the other.
- Tests that touch Jackson API differences (`writeStringField` vs `writeStringProperty`,
  `com.fasterxml.jackson` vs `tools.jackson`) stay per-starter.
  *Why:* the Jackson major differs between Boot 3 and Boot 4, so these cannot share one source file.
- Use Awaitility for assertions over captured log output, consistent with existing tests.
  *Why:* access-log/Logback output is produced **asynchronously** (valve + console appender on another thread),
  so a direct assert right after the request is racy. Awaitility polls until the expectation holds (or a short
  timeout), and `.during(...)` lets us assert an absence (e.g. the version-mismatch warning never appears)
  without a brittle fixed `sleep`.

## Build / validate
- Targeted: `mvn -pl idporten-access-log-spring-boot-3-starter,idporten-access-log-spring-boot-4-starter -am test`
- Verify pins unchanged with `mvn -pl <starter> dependency:tree` after POM edits.
