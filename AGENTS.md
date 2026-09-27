# Repository Guidelines

## Maintenance Scope

This branch maintains Minecraft **1.21.x**: 1.21.1 through 1.21.11, on both NeoForge and Fabric. The default NeoForge and Fabric targets and Stonecutter VCS version are **1.21.11**. Java 21 is required for game code; `common` remains Java 21 for portable business rules. The mod version remains in `gradle.properties`.

The rolling `dev` maintains the latest family; `maint/1.21.x` and the legacy `maint/1.20.x`, `maint/1.19.x`, `maint/1.18.x` are active maintenance lines. **All current maintenance lines accept all change types, including features, refactors, fixes, and adaptations.** Consult the enclosing workspace's `AGENTS/branches.md` when available; record every active branch's applicability and verification for shared changes. Do not automatically downgrade legacy support or merge entire newer-family branches into older families.

Within this family, retain necessary Stonecutter conditions and Fabric source layers. Small API changes may use conditions; large mutually exclusive implementations belong in source layers. Pure business rules should remain free of Minecraft and loader imports. This branch does not build other families; historical GUI reports retain their original scope and dates.

## Project Structure

- `src/common/java`: pure Java rules and configuration; `common/` runs their fast JVM tests.
- `src/common-minecraft/java`: loader-neutral Minecraft sessions, services, inventory and automation interfaces.
- `src/main/java`: NeoForge implementation, processed by the `versions/` Stonecutter matrix. `neoforge/` builds the active VCS implementation directly.
- `src/test/java`: common tests. `fabric/src/test/java`: tests with actual vanilla slots/components. `fabric/src/test-versioned/`: legacy registry bootstrap.
- `fabric/src/main/java`: shared Fabric implementation. Version source layers: legacy, legacy_pair_render, legacy_render, legacy_optional_tag, modern_legacy_input, modern, and identifier.
- `fabric/build.gradle`: the family's shared Fabric build script. `fabric_versions/<mc>/gradle.properties` declares each target. Do not use `fabric/versions/` as an active matrix.
- Resources: `src/main/resources`, `src/main/templates`, `src/generated/resources`, `fabric/src/main/resources`, and `src/versioned/resources`. `recipe_1_21_2_plus` names a resource format also used by 26.x; do not delete resources merely because their names mention an older version.

## Build, Test, and Development

Use the wrapper from this checkout, with explicit project paths. Keep the configured 4 GiB heap and one worker for full matrix builds. The committed `.java-version` selects Java 21; use a temporary JAVA_HOME if needed, without changing the global JDK.

- `./gradlew :common:test` or `:verifyCommon`: fast common rules.
- `./gradlew :1.21.1:build :1.21.11:build`: NeoForge family boundary builds.
- `./gradlew :fabric_1_21_1:test :fabric_1_21_11:test`: Fabric slot/component regressions. Fabric build tasks include these tests; logs stay in each project's `build/test-run/`.
- `./gradlew :neoforge:build :fabric:build`: default projects, both targeting 1.21.11.
- `./gradlew :verifyRepresentative`: common checks, family boundaries and selected API breakpoints.
- `./gradlew :verifyNeoForgeAll`, `:verifyFabricAll`, `:verifyAll`, or `:verifyCi`: corresponding complete family matrices.
- `./gradlew :buildReleaseArtifacts`: validate and collect only this family's 22 publishable jars into `build/release-artifacts/`; exclude duplicate default projects. Releases spanning families must collect each active branch separately and record commit/hash provenance.
- `./gradlew :1.21.11:runClient` or `:fabric_1_21_11:runClient`: actual client GUI testing.
- Corresponding `runServer` tasks launch dedicated servers; NeoForge `runData` regenerates data and `runGameTestServer` runs registered game tests.

NeoForge artifacts live in `versions/<mc>/build/libs/`, Fabric artifacts in `fabric_versions/<mc>/build/libs/`. `./gradlew clean build` addresses all included projects; prefer `buildReleaseArtifacts` for publishable output selection.

On this Mac, run full verification with:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
  ./gradlew --no-daemon --no-configuration-cache --max-workers=1 \
  -Dorg.gradle.jvmargs=-Xmx4g :verifyAll :buildReleaseArtifacts
```

When needed for NeoForge, add the locally cached manifest override:
`-PneoForge.neoFormRuntime.launcherManifestUrl=file:///Users/river_quinn/.gradle/caches/neoformruntime/artifacts/minecraft_launcher_manifest.json`.

GUI tooling exports actual runClient launch arguments; see `tools/gui-validation/README.md`. Default target is 1.21.11; explicitly choose other supported targets. Use a same-version dedicated test world and preserve original config/options. Historical reports and evidence are not instructions to reintroduce removed targets.

## Coding Style & Naming Conventions

Use UTF-8. This maintenance family uses Java 21 for Minecraft code; the pure Java common module retains Java 21. Follow the existing Java style: 4-space indentation, braces on the same line, descriptive class names, and package names matching `com.river_quinn.enchantment_custom_table`. Registry holder classes use the `Mod*` pattern, such as `ModBlocks` and `ModMenus`. Keep mod ids, resource paths, translation keys, and JSON filenames lowercase with underscores, for example `enchantment_conversion_table`.

## Testing Guidelines

Prefer focused tests for shared logic such as enchantment conversion, book handling, config behavior, and table session behavior. Put JVM tests in `src/test/java` with names ending in `Test`. Put NeoForge game tests under the mod namespace `enchantment_custom_table` so the configured run tasks can discover them.

For shared logic changes, start with `./gradlew :common:test`. For cross-platform behavior changes, also build the representative Fabric projects and at least one NeoForge Stonecutter project. Always use `runClient` for GUI, menu, renderer, and in-game behavior checks.

## Commit & Pull Request Guidelines

Recent history uses short conventional-style subjects such as `fix: ...`, `feat: ...`, `doc: ...`, and `refactor: ...`. Keep commits scoped and describe the player-visible bug or behavior changed. Pull requests should include a clear summary, testing performed, linked issues when applicable, and screenshots or short recordings for GUI, texture, recipe, or renderer changes.

## Configuration & Assets

Keep version, mod id, author, and dependency ranges in `gradle.properties`. Do not hardcode values that are already expanded into `neoforge.mods.toml` from `src/main/templates`. When adding blocks or items, update the matching registration class, model, blockstate, loot table, recipe, texture, and `en_us.json` / `zh_cn.json` translations together.

Fabric self-manages its config file at the Fabric config dir path `enchantment_custom_table.json`. Shared JSON defaults, parsing, and deprecated-field warning detection live in `src/common/java/com/river_quinn/enchantment_custom_table/core/config/JsonTableConfigCodec.java`; Fabric-only file IO and logging live in `fabric/src/main/java/com/river_quinn/enchantment_custom_table/fabric/config/FabricTableConfig.java`.


Payment configuration uses the schema-2 `paymentOptions` list, with 36 emeralds / 4 emerald blocks / 1 nether star as alternatives. Validation, migration and immutable snapshots live in `core/config`; resolve item IDs and default stack limits only after Minecraft registration. Preserve original files with `.payment-v1.bak` before migrating old costs. TOML adapters and their common-project tests live in `src/main/java/com/river_quinn/enchantment_custom_table/config/TomlPaymentConfig.java` and `src/test-config/java`.

Server rules are authoritative. Keep local snapshots separate from connection-scoped client snapshots, sync all four gameplay flags with payments, and clear the remote snapshot on disconnect. Do not persist received rules to the client file. Forge/NeoForge retains COMMON configuration and replaces only this mod's file watcher after loading to preserve malformed edits; Fabric reloads on server/world start. Config changes require JVM regression tests plus real client/server checks with intentionally different local and server prices. See both README configuration sections before changing migration or validation semantics.
