# Repository Guidelines

## Project Structure & Module Organization

This is a Java 21 NeoForge mod for Minecraft 1.21.1. Main code lives in `src/main/java/com/river_quinn/enchantment_custom_table`, organized by responsibility: `block`, `block/entity`, `client/gui`, `world/inventory`, `network`, `renderer`, `init`, and `utils`. Resources live in `src/main/resources`, including mod assets under `assets/enchantment_custom_table`, data files under `data/enchantment_custom_table`, documentation images under `doc`, and metadata templates under `src/main/templates`. Generated resources are written to `src/generated/resources` by the data run config and are included in the main resource set.

## Build, Test, and Development Commands

Use the Gradle wrapper from the repository root:

- `./gradlew build` compiles the mod, processes resources, and creates build artifacts.
- `./gradlew runClient` launches a local Minecraft client for manual testing.
- `./gradlew runServer` launches a local dedicated server with `--nogui`.
- `./gradlew runData` regenerates data into `src/generated/resources`.
- `./gradlew test` runs JVM tests if a `src/test` tree is added.
- `./gradlew runGameTestServer` runs registered NeoForge game tests; add tests before relying on this target.

## Coding Style & Naming Conventions

Use UTF-8 and Java 21. Follow the existing Java style: 4-space indentation, braces on the same line, descriptive class names, and package names matching `com.river_quinn.enchantment_custom_table`. Registry holder classes use the `Mod*` pattern, such as `ModBlocks` and `ModMenus`. Keep mod ids, resource paths, translation keys, and JSON filenames lowercase with underscores, for example `enchantment_conversion_table`.

## Testing Guidelines

There are currently no checked-in automated tests. Prefer focused tests for shared logic such as enchantment conversion and book handling. Put JVM tests in `src/test/java` with names ending in `Test`. Put NeoForge game tests under the mod namespace `enchantment_custom_table` so the configured run tasks can discover them. Always run `./gradlew build` after code or resource changes, and use `./gradlew runClient` for GUI, menu, renderer, and in-game behavior checks.

## Commit & Pull Request Guidelines

Recent history uses short conventional-style subjects such as `fix: ...`, `feat: ...`, `doc: ...`, and `refactor: ...`. Keep commits scoped and describe the player-visible bug or behavior changed. Pull requests should include a clear summary, testing performed, linked issues when applicable, and screenshots or short recordings for GUI, texture, recipe, or renderer changes.

## Configuration & Assets

Keep version, mod id, author, and dependency ranges in `gradle.properties`. Do not hardcode values that are already expanded into `neoforge.mods.toml` from `src/main/templates`. When adding blocks or items, update the matching registration class, model, blockstate, loot table, recipe, texture, and `en_us.json` / `zh_cn.json` translations together.
