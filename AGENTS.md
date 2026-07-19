# Repository Guidelines

## Project Structure & Module Organization

This branch is a Java 17 Minecraft `1.19.2` port branch. It targets two loaders:

- Forge `1.19.2`
- Fabric `1.19.2`

The branch intentionally does not retain the mainline `1.21.1` through `26.1.2` version matrix. Do not reintroduce Stonecutter, NeoForge, `versions/`, or `fabric_versions/` unless a future backward-port matrix is explicitly planned.

Shared code is still split by dependency level:

- `src/common/java`: pure Java logic with no Minecraft or loader imports.
- `src/common-minecraft/java`: Minecraft-dependent common logic with no Forge/Fabric imports.
- `src/main/java`: Forge platform implementation.
- `fabric/src/main/java`: Fabric platform implementation.
- `src/test/java`: shared JVM tests for common rules, config, and intent behavior.

Gradle subprojects:

- `common/`: pure JVM Gradle subproject for fast shared tests.
- `forge/`: Forge `1.19.2` build and run configuration.
- `fabric/`: Fabric `1.19.2` build and run configuration.

Resources live in `src/main/resources`, including assets under `assets/enchantment_custom_table` and data files under `data/enchantment_custom_table`. For Minecraft `1.19.2`, use resource pack format 9 and legacy data-pack directory names such as `recipes/`, `loot_tables/`, and `tags/items/`. Custom generated-slot and floating-book sprites must be explicitly registered with the loader's pre-atlas-definition stitching API.

## Build, Test, and Development Commands

Use the Gradle wrapper from the repository root. This branch targets Java 17 bytecode; the local Gradle runtime may run on Java 21 while Gradle toolchains compile with Java 17.

- `./gradlew :common:test` runs fast shared JVM tests.
- `./gradlew :forge:build` builds the Forge `1.19.2` jar.
- `./gradlew :fabric:build` builds the Fabric `1.19.2` jar.
- `./gradlew :forge:runClient` launches a Forge client.
- `./gradlew :fabric:runClient` launches a Fabric client.
- `./gradlew :forge:runServer` launches a Forge dedicated server.
- `./gradlew :fabric:runServer` launches a Fabric dedicated server.
- `./gradlew :forge:runData` regenerates Forge data into `src/generated/resources`.
- `./gradlew :verifyCommon` runs shared JVM tests.
- `./gradlew :verifyForge` builds Forge.
- `./gradlew :verifyFabric` builds Fabric.
- `./gradlew :verifyRepresentative`, `./gradlew :verifyAll`, and `./gradlew :verifyCi` run common tests plus both platform builds.
- `./gradlew buildReleaseArtifacts` builds both publishable jars and collects them under `build/release-artifacts/`.

Expected release jar names:

- `enchantment_custom_table-<mod_version>-forge-mc1.19.2.jar`
- `enchantment_custom_table-<mod_version>-fabric-mc1.19.2.jar`

## Coding Style & Naming Conventions

Use UTF-8. Java 17 is the target for this branch. Follow the existing Java style: 4-space indentation, braces on the same line, descriptive class names, and package names matching `com.river_quinn.enchantment_custom_table`.

Registry holder classes use the `Mod*` pattern, such as `ModBlocks`, `ModItems`, and `ModMenus`. Keep mod ids, resource paths, translation keys, and JSON filenames lowercase with underscores.

## Testing Guidelines

Prefer focused JVM tests for shared logic such as enchantment conversion, book handling, config behavior, and table session behavior. Put JVM tests in `src/test/java` with names ending in `Test`.

For shared logic changes, start with `./gradlew :common:test`. For loader behavior changes, build both `:forge:build` and `:fabric:build`. Use `runClient` for GUI, menu, renderer, config, automation, and in-game behavior checks.

## Commit & Pull Request Guidelines

Recent history uses short conventional-style subjects such as `fix: ...`, `feat: ...`, `doc: ...`, and `refactor: ...`. Keep commits scoped to one migration milestone or one player-visible behavior change.

For this port branch, prefer milestone commits:

- build graph collapse
- resource migration
- shared enchantment API downgrade
- Forge platform port
- Fabric platform port
- verification and release task cleanup

## Configuration & Assets

Keep version, mod id, author, loader dependency ranges, Forge version, Fabric Loader version, and Fabric API version in `gradle.properties`. Do not hardcode values that are already expanded into `mods.toml` or `fabric.mod.json`.

Forge metadata is generated from `src/main/templates/META-INF/mods.toml`. Fabric metadata lives in `fabric/src/main/resources/fabric.mod.json`.

Fabric self-manages its config file at the Fabric config dir path `enchantment_custom_table.json`. Shared JSON defaults and parsing live in `src/common/java/com/river_quinn/enchantment_custom_table/core/config/JsonTableConfigCodec.java`; Fabric-only file IO and logging live in `fabric/src/main/java/com/river_quinn/enchantment_custom_table/fabric/config/FabricTableConfig.java`.
