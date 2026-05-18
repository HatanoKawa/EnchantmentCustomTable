# Minecraft Version Migration Plan

Updated: 2026-05-17
Baseline branch: `dev`
Current latest maintained branch: `dev_1.21.11`

## Progress

- `dev_1.21.9`: completed and pushed on 2026-05-17. Validation: `compileJava`, `test`, `runGameTestServer` (8 required tests), `build`.
- `dev_1.21.10`: completed and pushed on 2026-05-17. Validation: `compileJava`, `test`, `runGameTestServer` (8 required tests), `build`.
- `dev_1.21.11`: completed and pushed on 2026-05-17. Validation: `compileJava`, `test`, `runGameTestServer` (8 required tests), `build`. Phase 1 complete.

## Current Version Data

Sources checked on 2026-05-17:

- Mojang Java version manifest: `https://piston-meta.mojang.com/mc/game/version_manifest_v2.json`
- NeoForge Maven metadata: `https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml`
- Parchment Maven metadata/index: `https://maven.parchmentmc.org/org/parchmentmc/data/`
- NeoForge 26.1 release note: `https://neoforged.net/news/26.1release/`

Observed current upstream state:

- Latest Minecraft Java release: `26.1.2`
- Latest Minecraft Java snapshot: `26.2-snapshot-7`
- Latest NeoForge artifact: `26.1.2.59-beta`
- Current repo baseline target: Minecraft `1.21.8`, NeoForge `21.8.23`, Parchment `2025.07.20`

Snapshots are intentionally excluded from the migration target list unless explicitly requested.

## Migration Split

The migration is split into two phases because Minecraft/NeoForge moved from the `1.21.x` line to the `26.x` line. The second phase should be treated as a larger compatibility boundary.

### Phase 1: Complete the 1.21 line

Goal: migrate from `dev_1.21.8` through `dev_1.21.11`.

Planned branch chain:

1. `dev_1.21.9`
2. `dev_1.21.10`
3. `dev_1.21.11`

Candidate Gradle version data:

| Target branch | Minecraft | NeoForge candidate | NeoForge range | Parchment Minecraft | Parchment release |
| --- | --- | --- | --- | --- | --- |
| `dev_1.21.9` | `1.21.9` | `21.9.16-beta` | `[21.9.0-beta,)` | `1.21.9` | `2025.10.05` |
| `dev_1.21.10` | `1.21.10` | `21.10.64` | `[21.10.0-beta,)` | `1.21.10` | `2025.10.12` |
| `dev_1.21.11` | `1.21.11` | `21.11.42` | `[21.11.0-beta,)` | `1.21.11` | `2025.12.20` |

Expected workflow:

1. Start from clean `dev_1.21.8`.
2. Create `dev_1.21.9` from `dev_1.21.8`.
3. Update Gradle version properties for the target version.
4. Resolve compile/API issues.
5. Run validation before moving forward:
   - `./gradlew compileJava`
   - `./gradlew test`
   - `./gradlew runGameTestServer`
   - `./gradlew build`
6. Push the completed branch.
7. Repeat by creating the next target branch from the previously completed target branch.

Phase 1 expected risk:

- Medium. It remains inside the `1.21.x` family, but NeoForge APIs may still shift between `21.8`, `21.9`, `21.10`, and `21.11`.
- Watch especially for client networking APIs, GameTest APIs, registry lookup APIs, GUI rendering APIs, and Gradle/ModDevGradle compatibility.

### Phase 2: Enter the 26.x line

Goal: migrate from `dev_1.21.11` to the latest stable `26.1.2` line.

Planned branch chain:

1. `dev_26.1`
2. `dev_26.1.1`
3. `dev_26.1.2`

Candidate Gradle version data:

| Target branch | Minecraft | NeoForge candidate | NeoForge range | Parchment status |
| --- | --- | --- | --- | --- |
| `dev_26.1` | `26.1` | `26.1.0.19-beta` | `[26.1.0.0-beta,)` | No `parchment-26.1` artifact found on 2026-05-17 |
| `dev_26.1.1` | `26.1.1` | `26.1.1.15-beta` | `[26.1.1.0-beta,)` | No `parchment-26.1.1` artifact confirmed on 2026-05-17 |
| `dev_26.1.2` | `26.1.2` | `26.1.2.59-beta` | `[26.1.2.0-beta,)` | No `parchment-26.1.2` artifact found on 2026-05-17 |

Phase 2 expected risk:

- High. This crosses the Minecraft version naming boundary and the NeoForge four-part versioning boundary.
- Parchment mappings may be unavailable for 26.x at the time of migration; the migration may need to remove, delay, or replace Parchment usage for 26.x branches.
- The NeoForge 26.1 release note mentions upstream mapping/toolchain changes around official parameter names. Expect source-level API name churn and Gradle configuration changes.
- Validate each branch independently and keep commits small enough to isolate failures.

## Required Rechecks Before Execution

Before starting either phase:

- Re-fetch Mojang version manifest and NeoForge Maven metadata.
- Re-fetch Parchment metadata for the exact target version.
- Confirm whether the target NeoForge version is still the latest preferred release for that Minecraft line.
- Confirm whether the current Gradle wrapper and ModDevGradle plugin support the target version.
- Confirm whether branch naming should remain `dev_<minecraft_version>`.

Before Phase 2 specifically:

- Re-read NeoForge 26.1 migration/release notes.
- Confirm whether Parchment has published 26.x artifacts.
- Decide whether 26.x should be implemented in a temporary feature branch first before creating the full chain.
- Run a small compile-only proof on `dev_26.1` before committing to the rest of the 26.x chain.

## Suggested Commit Strategy

- Keep `.ai` organization commits separate from code migration commits.
- Use one migration commit per target version branch after validation passes.
- If a target version needs a broad compatibility refactor, commit that refactor separately before continuing to the next target branch.
- Avoid force-pushing version branches.

## Open Questions

- Should `mod_version` stay `1.2.0` across these compatibility branches, or should the migration advance it before release?
- Should Phase 2 keep Parchment disabled until 26.x mappings are available?
- Should `dev_26.1` be created as a proof branch first, then promoted into the chain after compile/test viability is known?
