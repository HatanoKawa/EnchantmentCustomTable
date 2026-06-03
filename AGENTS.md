# Repository Guidelines

## Project Structure & Module Organization

This is a Java 21 multi-loader Minecraft mod, with Java 25 required for Minecraft `26.x` targets. NeoForge is still the primary platform and is managed through Stonecutter for Minecraft `1.21.1` through `26.1.2`. Fabric support currently covers Minecraft `1.21.1` through `1.21.11` plus `26.1.2`; Fabric `26.1.2` uses the Fabric 26.1+ non-remap / official-names build model.

Main shared code is split by dependency level:

- `src/common/java`: pure Java logic with no Minecraft or loader imports.
- `src/common-minecraft/java`: Minecraft-dependent common logic with no NeoForge/Fabric imports.
- `src/main/java`: NeoForge platform implementation used by the root Stonecutter NeoForge matrix and the `:neoforge` project.
- `src/test/java`: shared JVM tests for common table rules and session behavior.

Platform and version projects live in separate directories:

- `common/`: pure JVM Gradle subproject for fast common tests.
- `neoforge/`: latest NeoForge platform subproject, currently targeting `26.1.2`.
- `fabric/`: Fabric platform source, resources, and shared Fabric build scripts. `fabric/build.gradle` is used by remapped `1.21.x` projects, while `fabric/build-26.gradle` is used by official-names `26.1.2`. Version-specific Fabric source layers live under `fabric/src/versioned/*`, including `official26`.
- `fabric_versions/`: Fabric version project directories. Each supported version has a `gradle.properties` file and is wired through `settings.gradle`.
- `versions/`: Stonecutter version metadata for the NeoForge root matrix.

Do not use `fabric/versions/` as an active version matrix location. If that directory appears locally and only contains empty version folders, it is migration residue rather than tracked project structure.

Resources live in `src/main/resources`, including mod assets under `assets/enchantment_custom_table`, data files under `data/enchantment_custom_table`, documentation images under `doc`, and metadata templates under `src/main/templates`. Fabric-specific metadata lives in `fabric/src/main/resources`. Generated resources are written to `src/generated/resources` by the data run config and are included in the main resource set.

## Build, Test, and Development Commands

Use the Gradle wrapper from the repository root. Prefer explicit project paths now that the workspace contains multiple platform/version projects.

- `./gradlew :common:test` runs fast shared JVM tests.
- `./gradlew :1.21.1:build`, `./gradlew :1.21.11:build`, or `./gradlew :26.1.2:build` builds a NeoForge Stonecutter version project.
- `./gradlew :neoforge:build` builds the latest NeoForge platform subproject.
- `./gradlew :fabric_1_21_1:build` through `./gradlew :fabric_1_21_11:build` build supported remapped Fabric `1.21.x` version projects.
- `./gradlew :fabric_26_1_2:build` builds the official-names Fabric `26.1.2` project and requires Java 25.
- `./gradlew :fabric:build` builds the default Fabric project, currently using the `1.21.1` defaults.
- `./gradlew :1.21.1:runClient`, `./gradlew :fabric_1_21_1:runClient`, or `./gradlew :fabric_26_1_2:runClient` launches a local client for manual testing on a specific platform/version.
- `./gradlew :1.21.1:runServer`, `./gradlew :fabric_1_21_1:runServer`, or `./gradlew :fabric_26_1_2:runServer` launches a local dedicated server for a specific platform/version.
- `./gradlew :1.21.1:runData` regenerates NeoForge data into `src/generated/resources`.
- `./gradlew :1.21.1:runGameTestServer` runs registered NeoForge game tests.
- `./gradlew :verifyCommon` runs shared JVM tests.
- `./gradlew :verifyRepresentative` runs common tests plus representative first/latest/26.x builds for both platforms.
- `./gradlew :verifyNeoForgeAll`, `./gradlew :verifyFabricAll`, or `./gradlew :verifyAll` run the full corresponding build matrices.
- `./gradlew :verifyCi` mirrors the complete verification set expected by GitHub Actions.

For latest NeoForge `26.x` builds on this local machine, the launcher manifest may need to be supplied from the Gradle cache:

```sh
./gradlew -PneoForge.neoFormRuntime.launcherManifestUrl=file:///Users/river_quinn/.gradle/caches/neoformruntime/artifacts/minecraft_launcher_manifest.json :26.1.2:build
```

Fabric `26.1.2` uses `net.fabricmc.fabric-loom` `1.17.x`, official Minecraft names, no `mappings` declaration, normal Gradle dependency configurations, and Java 25. On this local machine, use the Homebrew Java 25 path when your shell default is lower:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home ./gradlew --no-daemon --no-configuration-cache --max-workers=1 -Dorg.gradle.jvmargs=-Xmx4g :fabric_26_1_2:build
```

When building the full Fabric matrix in one command, avoid Gradle/Loom remap OOM by running with one worker and a larger heap:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home ./gradlew --no-daemon --no-configuration-cache --max-workers=1 -Dorg.gradle.jvmargs=-Xmx4g :fabric_1_21_1:build :fabric_1_21_2:build :fabric_1_21_3:build :fabric_1_21_4:build :fabric_1_21_5:build :fabric_1_21_6:build :fabric_1_21_7:build :fabric_1_21_8:build :fabric_1_21_9:build :fabric_1_21_10:build :fabric_1_21_11:build :fabric_26_1_2:build
```

## Coding Style & Naming Conventions

Use UTF-8. Java 21 is the baseline for `1.21.x`, and Java 25 is required for `26.x` targets. Follow the existing Java style: 4-space indentation, braces on the same line, descriptive class names, and package names matching `com.river_quinn.enchantment_custom_table`. Registry holder classes use the `Mod*` pattern, such as `ModBlocks` and `ModMenus`. Keep mod ids, resource paths, translation keys, and JSON filenames lowercase with underscores, for example `enchantment_conversion_table`.

## Testing Guidelines

Prefer focused tests for shared logic such as enchantment conversion, book handling, config behavior, and table session behavior. Put JVM tests in `src/test/java` with names ending in `Test`. Put NeoForge game tests under the mod namespace `enchantment_custom_table` so the configured run tasks can discover them.

For shared logic changes, start with `./gradlew :common:test`. For cross-platform behavior changes, also build the representative Fabric projects and at least one NeoForge Stonecutter project. Always use `runClient` for GUI, menu, renderer, and in-game behavior checks.

## Commit & Pull Request Guidelines

Recent history uses short conventional-style subjects such as `fix: ...`, `feat: ...`, `doc: ...`, and `refactor: ...`. Keep commits scoped and describe the player-visible bug or behavior changed. Pull requests should include a clear summary, testing performed, linked issues when applicable, and screenshots or short recordings for GUI, texture, recipe, or renderer changes.

## Configuration & Assets

Keep version, mod id, author, and dependency ranges in `gradle.properties`. Do not hardcode values that are already expanded into `neoforge.mods.toml` from `src/main/templates`. When adding blocks or items, update the matching registration class, model, blockstate, loot table, recipe, texture, and `en_us.json` / `zh_cn.json` translations together.

Fabric self-manages its config file at the Fabric config dir path `enchantment_custom_table.json`. Shared JSON defaults, parsing, and deprecated-field warning detection live in `src/common/java/com/river_quinn/enchantment_custom_table/core/config/JsonTableConfigCodec.java`; Fabric-only file IO and logging live in `fabric/src/main/java/com/river_quinn/enchantment_custom_table/fabric/config/FabricTableConfig.java`.
