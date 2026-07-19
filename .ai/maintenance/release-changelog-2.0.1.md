# 2.0.1 Release Changelog Draft

Created: 2026-07-19
Previous release: `2.0.0`
Current release candidate: `2.0.1`

## Branches And Release Prep Commits

Main multi-version branch:

- Branch: `dev`
- Version prep commit: `3698bba chore: prepare 2.0.1 release`
- Main functional commits since `2.0.0`:
  - `ce8c69b feat: add free conversion cost mode`
  - `46a18f9 feat: add 26.2 support`

Minecraft 1.20.1 adaptation branch:

- Branch: `codex/port-1.20.1`
- Version prep commit: `d47dc97 chore: prepare 2.0.1 release`
- Notable commits since `2.0.0`:
  - `68933b6 build: collapse project to 1.20.1 loaders`
  - `8cc61b9 refactor: port common and forge code to 1.20.1`
  - `c08a7be refactor: port fabric platform to 1.20.1`
  - `38fc962 feat: add free conversion cost mode`
  - `d15466d fix: replace stored enchantments on split books`
  - `3fce576 feat: add forge in-game config screen`

## Release Page Draft

This is a small follow-up release after `2.0.0`. It adds a new optional free-cost mode for the enchantment conversion table, expands the latest supported Minecraft version to `26.2`, and prepares the separate Minecraft `1.20.1` Fabric/Forge adaptation line.

### New Features

- Added `freeConversionTableCosts`.
  - When enabled, the enchantment conversion table can generate and copy enchanted books for free.
  - The normal book slot and payment slot stop accepting input in this mode.
  - Manual generated-book pickup and automation extraction from the copy output slot still work normally.
- Added Minecraft `26.2` support for both NeoForge and Fabric.
- Added the separate Minecraft `1.20.1` adaptation branch for Fabric and Forge builds.

### Fixes And Compatibility

- Updated NeoForge `26.1.2` to a newer patch dependency and updated Fabric `26.1.2` API dependency while adding `26.2`.
- Fixed a Minecraft `1.20.1` split-book issue where taking a generated split book could incorrectly leave copied enchantment data behind.
- Fixed Minecraft `1.20.1` Forge in-game config access so config values can be edited from the game and hot-reloaded.
- Completed several Minecraft `1.20.1` resource and loader compatibility adjustments, including GUI texture paths, resource pack metadata, Forge menu translations, and block-atlas stitching.

### Known Issues

- On NeoForge Minecraft `1.21.9`, opening the NeoForge Mods list may crash because of a NeoForge `21.9.16-beta` GUI issue. This happens before this mod's config screen is opened. If this affects you, edit the mod config file directly instead of using the in-game Mods list.

### Notes

- `freeConversionTableCosts` defaults to `false`, so existing worlds keep the normal book and emerald payment behavior unless the option is enabled.
- Make sure the downloaded jar matches both the loader and the Minecraft version.
- Minecraft `26.x` builds require Java 25.
- Minecraft `1.20.1` builds are maintained separately on the dedicated Fabric/Forge adaptation branch.

## Verification

User-side GUI checks:

- River tested the current `dev` candidate after the `26.2` migration and did not find new functional issues.

Local verification performed during release prep:

- `dev`: `buildReleaseArtifacts`
  - Generated 30 release jars.
  - Confirmed `enchantment_custom_table-2.0.1-neoforge-mc26.2.jar`.
  - Confirmed `enchantment_custom_table-2.0.1-fabric-mc26.2.jar`.
- `codex/port-1.20.1`: `:verifyCi`
- `codex/port-1.20.1`: `buildReleaseArtifacts`
  - Confirmed `enchantment_custom_table-2.0.1-forge-mc1.20.1.jar`.
  - Confirmed `enchantment_custom_table-2.0.1-fabric-mc1.20.1.jar`.

## Internal Notes

- The NeoForge `1.21.9` mod-list crash is tracked separately in `.ai/maintenance/known-issues-2026-07-19.md`.
- This document is intended as the source for the external mod-page changelog, not as a player-facing README replacement.
