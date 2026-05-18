# EnchantmentCustomTable Multi-Version Migration Plan

Created: 2026-05-14
Repository: /Users/river_quinn/workspace/learn/EnchantmentCustomTable

## Goal

Propagate the latest `dev` changes into every maintained `dev_1.21.x` branch, preserving each branch's Minecraft/NeoForge version-specific adaptations.

The source changes include:

- Core menu and enchantment logic hardening.
- Extracted shared enchantment table rules.
- JVM tests and NeoForge GameTest coverage.
- Build artifact naming with the Minecraft version suffix.
- Enchantment Conversion Table localized search.
- Latest `dev` GUI texture and slot/search-box positioning adjustments.
- Release version bump to `mod_version=1.2.0`.

## Source Branch Preparation

Before starting version-branch migration:

- Ensure local `dev` is synced with `origin/dev`.
- Update `gradle.properties` on `dev` from `mod_version=1.1.8` to `mod_version=1.2.0`.
- Run validation on `dev`.
- Commit and push the version bump before propagating the branch chain.

## Branch Order

Migrate in dependency order:

1. `dev_1.21.2`
2. `dev_1.21.3`
3. `dev_1.21.4`
4. `dev_1.21.5`
5. `dev_1.21.6`
6. `dev_1.21.7`
7. `dev_1.21.8`

## Migration Strategy

For `dev_1.21.2`:

- Start from `origin/dev_1.21.2`.
- Merge latest `dev`.
- Resolve conflicts by preserving target-version API adaptations while applying the new logic.
- Validate before pushing.

For later branches:

- Start from the current remote branch.
- Prefer merging the previous completed version branch when that better preserves the version chain.
- Resolve only the incremental version-specific conflicts.
- Validate before moving to the next branch.

## Expected Conflict Areas

Dry-run merge checks already found conflicts in:

- `src/main/java/com/river_quinn/enchantment_custom_table/block/entity/EnchantingTableLikeBlockEntity.java`
- `src/main/java/com/river_quinn/enchantment_custom_table/client/gui/EnchantmentConversionScreen.java`
- `src/main/java/com/river_quinn/enchantment_custom_table/utils/EnchantmentUtils.java`
- `src/main/java/com/river_quinn/enchantment_custom_table/world/inventory/EnchantingCustomMenu.java`
- `src/main/java/com/river_quinn/enchantment_custom_table/world/inventory/EnchantmentConversionMenu.java`

Additional conflicts are expected on 1.21.7 and 1.21.8 in:

- `src/main/java/com/river_quinn/enchantment_custom_table/client/gui/EnchantingCustomScreen.java`

## Validation Per Branch

Minimum validation:

- `./gradlew compileJava`
- `./gradlew test`
- `./gradlew build`

Preferred validation when environment permits:

- `./gradlew runGameTestServer`

If a branch cannot run a validation task due to version-specific environment/tooling issues, record the failure and continue only after the compile-level risk is understood.

## Progress

- [x] Sync local `dev` with `origin/dev`.
- [x] Bump `dev` `mod_version` to `1.2.0`, validate, commit, and push.
- [x] Migrate and validate `dev_1.21.2`. Pushed `71d147d`.
- [x] Migrate and validate `dev_1.21.3`. Pushed `1e7306f`.
- [x] Migrate and validate `dev_1.21.4`. Pushed `6233341`.
- [x] Migrate and validate `dev_1.21.5`. Pushed `99339e7`.
- [x] Migrate and validate `dev_1.21.6`. Pushed `9eb4b89`.
- [x] Migrate and validate `dev_1.21.7`. Pushed `77914ad`.
- [x] Migrate and validate `dev_1.21.8`. Pushed `588bc31`.

## Notes

- Do not force-push existing version branches.
- Push each version branch only after its own validation pass.
- Keep migration commits version-scoped so regressions can be isolated by Minecraft target version.
- Prefer small explicit fixes over wholesale replacement with `dev` files when a target version has API-specific adaptations.
