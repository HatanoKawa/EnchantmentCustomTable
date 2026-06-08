# 2.0.0 Release Changelog Draft

Previous release: `1.2.0`, released 2026-05-19
Current release candidate: `2.0.0`
Comparison baseline: `master` / `FETCH_HEAD` at `1012db0`

## Release Page Draft

This is a major release. Compared with `1.2.0`, the mod has been rebuilt around a multi-loader and multi-version structure, adds Fabric support, expands automation, cleans up config behavior, and fixes a long list of GUI and inventory synchronization issues.

### New Platform And Version Support

- Added Fabric support.
- NeoForge and Fabric now both target Minecraft `1.21.1` through `1.21.11`, plus `26.1`, `26.1.1`, and `26.1.2`.
- Fabric `26.x` builds use the official-names / non-remap Fabric build model.
- Release jar names now include the loader, for example `enchantment_custom_table-2.0.0-neoforge-mc1.21.1.jar` or `enchantment_custom_table-2.0.0-fabric-mc1.21.1.jar`.

### New Gameplay Features

- Added controlled table automation support for hoppers and compatible item-transport mods.
- The custom enchanting table can accept enchanted books through automation and apply them through the same rules used by the GUI.
- The enchantment conversion table can accept normal books and emerald payment through automation.
- Added conversion-table copy template mode:
  - Put a valid enchanted book into the template slot.
  - The table consumes one normal book plus payment and creates a matching copy in the output slot.
  - Templates must contain exactly one enchantment and must not exceed that enchantment's normal max level.
  - Automation can extract only from the copy output slot, not from virtual candidate slots.
- Added custom blue/green animated table-book textures for the two table types.

### Config And Balance Changes

- Added `enforceEnchantmentLevelLimit`.
  - When enabled, duplicate enchantment merges cannot exceed the vanilla max level.
  - Adding a new over-cap enchantment from another mod is still allowed when it is not merging with an existing same enchantment.
- Added `incrementalSameLevelMerge`.
  - When enabled, same-name enchantments only merge if their levels match.
  - A successful merge increases the level by only `+1`, for example Sharpness V + Sharpness V becomes Sharpness VI.
  - This is intended as an opt-in balance mode for players and modpacks that want weaker stacking.
- Incremental merge mode also changes single-enchantment book splitting:
  - Default mode keeps the old binary split behavior.
  - Incremental mode splits a level N book into two level N-1 options.
- Kept `convertOnlyLevelOneBook` for conversion-table output level control.
- Removed old/deprecated config aliases. Old config files should be regenerated or manually migrated.

### Recipes And Assets

- Fixed the conversion table recipe id typo by migrating from the old misspelled id to `enchantment_conversion_table`.
- Table recipes now accept multiple quartz block variants through the `enchantment_custom_table:quartz_blocks` item tag:
  - Quartz Block
  - Chiseled Quartz Block
  - Smooth Quartz
  - Quartz Pillar
  - Quartz Bricks
- Updated GUI textures, slot icons, disabled-slot visuals, output-slot visuals, and README screenshots.
- Added English and Simplified Chinese README coverage focused on player-facing usage.

### Fixes And Stability

- Fixed world-entry crashes on newer versions caused by GameTest registration behavior.
- Fixed multiple conversion-table refresh bugs after taking generated enchanted books.
- Fixed search-filter flicker and page reset behavior in the conversion table.
- Fixed generated enchanted-book slot ghost/flicker behavior after taking books from a full page.
- Fixed Fabric GUI/client sync issues where visible generated slots could disagree with server-side slot data.
- Fixed Fabric left-click removal from generated slots so enchantments are actually removed from the tool.
- Fixed Fabric automation refresh behavior while the GUI is open.
- Fixed Fabric and NeoForge table block drops so stored items are dropped when blocks are broken.
- Fixed Fabric missing floating book renderer, GUI interaction sounds, generated-slot placement, block translations, and texture-path issues.
- Fixed NeoForge `1.21.9+` inventory handling by migrating away from deprecated item-handler APIs where needed.
- Fixed conversion table automation input so books and payment can still be inserted when the copy output slot is occupied.

### Build And Developer Workflow

- Added shared common source sets and fast JVM tests for common rules/config/network behavior.
- Added shared session/rule/layout helpers to reduce duplicated NeoForge/Fabric behavior.
- Added Fabric and NeoForge version matrices to Gradle and GitHub Actions.
- Added verification aliases such as `verifyCommon`, `verifyRepresentative`, `verifyNeoForgeAll`, `verifyFabricAll`, and `verifyCi`.
- Added `buildReleaseArtifacts`, which builds publishable version jars and collects them under `build/release-artifacts/`.
- Cleared project-owned Gradle deprecation warnings and increased Gradle wrapper download timeout for CI stability.

### Compatibility Notes

- This release is a major version bump to `2.0.0`.
- Old config keys such as `ignoreEnchantmentLevelLimit` and `convert_max_level_book` are no longer read.
- Data packs or scripts referencing the old misspelled conversion recipe id should update to `enchantment_custom_table:enchantment_conversion_table`.
- Make sure to download the jar matching both your loader and Minecraft version.
- Minecraft `26.x` targets require Java 25 at build/runtime setup level; `1.21.x` targets remain Java 21 based.
