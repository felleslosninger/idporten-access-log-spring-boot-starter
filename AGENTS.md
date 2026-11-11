# Agent instructions

Conventions for working in this repository. Follow them when making changes.

## Module layout
- `idporten-access-log-spring-boot-3-starter` / `-4-starter` — the only real Maven modules and the only
  published artifacts. Each is a thin starter that brings its own Spring Boot, Tomcat, Jackson, Logback and
  logback-access versions, and is **self-contained** (the shared code/resources below are compiled/copied
  into each starter jar).
- `idporten-access-log-common` — a **source-only holder** (no `pom.xml`, NOT a module, never published).
  It holds everything shared by both starters: `src/main/java` (framework-free constants/fields plus the
  framework-coupled-but-identical `AccessLogsProperties` and `StaticResourcesFilter`), `src/main/resources`
  (the `logback-access*.xml`), and `src/test/java` (the shared tests). Both starters pull it in via
  `build-helper-maven-plugin` using the parent property `${shared.sources.basedir}`:
  `add-source` (main java), `add-resource` (resources) and `add-test-source` (tests).
  *Why source-only instead of a published/compiled module:* the shared classes must compile against **each**
  starter's own Spring Boot / Tomcat / Jackson / logback versions, which a single compiled module cannot do;
  and since consumers only use the starters, there is no value in publishing a separate shared artifact.
  Keeping it as compiled-per-starter source gives both DRY and self-contained starter jars with the fewest
  possible modules (just the two starters).
  *Why the three build-helper executions can't be merged:* `add-source` and `add-test-source` both read the
  `<sources>` parameter but need different directories (main vs test), so they must stay separate; the shared
  base path is factored into `${shared.sources.basedir}` (parent POM) so it is written once.
  *Why shared tests live here:* they must execute against **both** runtime dependency sets (Boot 3 and 4) to
  catch version-specific breakage; a plain test-jar would compile once and lose that coverage, and copying
  the tests would let the two copies drift.
  *When to split a shared class out (per starter):* only when its API genuinely diverges between the two
  platform lines (e.g. the Jackson 2 vs 3 `AccesslogProvider`/decorators, which are intentionally kept
  per-starter). Because the shared sources are compiled against both dependency sets, such divergence shows
  up as a compile failure in one starter — that is the signal to move that one class into each starter's own
  `src/main/java`. See the class comment in `StaticResourcesFilter` for the exact procedure.

## Dependency & version pinning (both starters must stay uniform)
*Why uniform:* the two starters differ only by major platform version, so keeping identical structure makes
diffs reviewable and prevents a fix landing in one starter but not the other.
- Declare every third-party version as a property in `<properties>`.
  *Why:* one obvious place to see/change a version, and it lets the Maven `<name>` surface them.
- Pin every third-party version in `<dependencyManagement>`, grouped in this order:
  1. BOM imports (`jackson-bom`, then `spring-boot-dependencies`)
  2. Logback family (`logback-access-tomcat`; `logback-core`/`logback-classic` only in the Boot 3 starter, see
     "Logback compatibility")
  3. Other third-party (`logstash-logback-encoder`, `tomcat-catalina`)
  *Why dependencyManagement (not inline):* a managed version overrides versions pulled in **transitively**
  (e.g. logback-core dragged in by logback-access and Spring Boot), which an inline version on a direct
  dependency does not reliably do.
  *Why jackson-bom is imported before spring-boot-dependencies:* for imported BOMs the **first** declaration
  wins, so jackson-bom must come first to override the Jackson version managed by the Spring Boot BOM.
  (Flipping this silently downgraded Boot 4 Jackson 3.1.7 → 3.1.5 — caught by Trivy, not the build.)
- In `<dependencies>` declare only `groupId`/`artifactId`/`scope` — never an inline `<version>`.
  *Why:* a single source of truth for versions (the management block) avoids a dependency being pinned in
  two places that drift apart.
- Keep the two starter POMs structurally identical: same property order, same dependency order,
  same scopes. Only values (versions), the Jackson groupId (`com.fasterxml.jackson` vs `tools.jackson`) and the
  Boot 3-only logback-core/classic pin (see "Logback compatibility") may differ.

## Logback compatibility
- logback-access runs a startup version check and logs a `WARN` on a mismatch. The wording comes from
  **logback-core** (i.e. from whichever core version is actually on the classpath) and has already changed once:
  core 1.5.x logs `For logback-core, expected version X but found Y`; core 1.6.x logs
  `Depender [logback-access-common] was expecting version X for dependency [logback-core] but found version Y`
  followed by `See also https://logback.qos.ch/codes.html#versionMismatch`. The expected version lives in the
  access jar at `ch/qos/logback/access/common/logback-access-common-dependencies.properties`.
- Consumers' own Spring Boot BOM controls their Logback version. A pin in our `dependencyManagement` is **not**
  inherited by consumers, and an imported BOM can't override what `spring-boot-starter-parent` manages, so a pin
  only makes our build/tests run with a version consumers don't get.
  *Therefore:* pick the `logback-access` release whose expected core version equals the Spring Boot BOM's
  `logback.version`, and leave `logback-core`/`logback-classic` unpinned (Boot 4 starter). Then the safety-net test
  runs with exactly what consumers get.
  *Exception (Boot 3 starter):* no `logback-access` release matches Boot 3.5's logback (1.5.34), so it keeps the
  `${logback.version}` pin and the README tells consumers to set the same `logback.version`. Drop the pin as soon as
  a Boot 3.5.x / logback-access pair lines up again.
- `logback-core`/`logback-classic` are `provided`: the app owns them.
- A shared safety-net test (`LogbackVersionCompatibilityTest`) fails the build on a mismatch. It does **not**
  match the warning text (that wording is owned by logback-core and changes between versions, so a text match goes
  blind exactly when the mismatching core brings new wording). Instead it reads the expected logback-core version
  from the access jar and the actual one from the core jar and asserts equality, plus fails on any version-related
  `WARN`/`ERROR` status the valve records at startup. Keep it free of hard-coded version numbers so it keeps
  working across future version bumps.
- Reflect the compatible versions in each starter's Maven `<name>` and in the README compatibility section.

### Logback upgrade — pitfalls (read before bumping any Logback artifact)
> [!WARNING]
> Logback, logback-access, Spring Boot and the Tomcat major line are a tightly coupled set.
> Bumping one without the others frequently breaks silently (a startup `WARN`, not a build failure).
- **Never bump one in isolation.** `logback-access.version` must move together with the logback version in use
  (the Spring Boot BOM's in the Boot 4 starter, `logback.version` in the Boot 3 starter). After changing either,
  re-check the access jar's expected core version
  (`ch/qos/logback/access/common/logback-access-common-dependencies.properties`). A newer `logback-access` is not
  better if it expects a core version Spring Boot doesn't ship.
- **Patch bumps can break too.** logback-access pins an *exact* logback-core version; even a patch mismatch
  (e.g. `1.5.37` vs `1.5.38`) triggers the startup warning. Do not assume semver compatibility.
- **Dependabot updates logback-core/classic independently.** PRs that bump only `logback.version`
  (or only `logback-access.version`) are the most common source of mismatch — scrutinise them and bump the pair.
- **Spring Boot upgrades move Logback transitively.** When bumping `spring-boot.version`, the BOM may pull a
  new logback-core. In the Boot 4 starter the safety-net test then fails until `logback-access.version` is moved
  to the matching release; that failure is intended, since consumers on that Boot version would get the warning.
- **Tomcat major must match the access artifact.** `logback-access-tomcat` targets a specific Tomcat major
  (10.x ↔ `tomcat.version` 10.1.x, 11.x ↔ 11.0.x). Keep `tomcat.version` on the matching line.
- **Jackson major is starter-specific.** Boot 3 uses `com.fasterxml.jackson` (Jackson 2); Boot 4 uses
  `tools.jackson` (Jackson 3). Logback/logstash encoder upgrades must keep using the matching Jackson API
  (`writeStringField` vs `writeStringProperty`) — a logstash-logback-encoder bump can flip this.
- **Always validate after any Logback-related bump**: run the build (so the shared compatibility safety-net
  runs in both starters) and run `mvn -pl <starter> dependency:tree` to confirm logback-core/classic/access
  resolve to the intended, mutually-compatible versions. A green build of that safety-net is the gate.

## Tests
- Put tests that are identical across both starters in `idporten-access-log-common/src/test/java`;
  never duplicate them.
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
