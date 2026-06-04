# Multi-Loader And Stonecutter Migration Archive

Created: 2026-06-04

This document summarizes the temporary AI planning and execution notes archived from `/Users/river_quinn/workspace/ai_temp/EnchantmentCustomTable` after the multi-version, multi-platform migration work. The original temporary documents were moved under that directory's `_archived` folder for historical lookup.

## Source Documents

- `2026-05-29/platform-decoupling-refactor-reference.md`
- `2026-06-01/cross_platform_migration_plan.md`
- `2026-06-01/platform_decoupling_refactor_plan.md`
- `2026-06-02/fabric-runtime-validation-and-known-differences.md`
- `2026-06-02/multi-version-multi-platform-master-plan.md`
- `2026-06-03/fabric-version-config-followup-plan.md`
- `2026-06-04/fabric-matrix-gui-recipe-fix-plan.md`

## Current Direction

- This repository targets Minecraft `1.21.1` through `26.x`. The previously considered `1.20.1` Fabric/Forge work is out of scope for this repository and should live in a separate repository if it is resumed.
- NeoForge remains the primary platform. Its version axis is managed through Stonecutter metadata under `versions/`.
- Fabric is maintained in the same repository through explicit Fabric projects under `fabric_versions/` and shared Fabric build/source layers under `fabric/`.
- The high-risk Stonecutter and MultiLoader work was isolated on `codex/multiloader-spike` instead of directly rewriting the old branch chain.
- GUI visual alignment, especially for the enchantment conversion table, is intentionally left as a manual redraw/alignment pass after functional migration.

## Architecture Results

- Pure Java logic lives in `src/common/java` and is compiled by the `:common` project for fast JVM tests.
- Minecraft-dependent, loader-neutral logic lives in `src/common-minecraft/java`.
- NeoForge platform code continues to use `src/main/java` and the root Stonecutter version projects.
- The latest NeoForge platform subproject lives in `neoforge/`.
- Fabric platform code lives in `fabric/`, with version-specific source layers in `fabric/src/versioned/*`.
- Fabric version project metadata lives in `fabric_versions/`.
- The migration residue directory `fabric/versions/` is not active project structure and was documented as safe to remove when empty.

The main shared boundaries introduced during the refactor are:

- `TableConfigService` for platform-provided config snapshots.
- `EnchantmentAccessService` for enchantment read/write, ids, max levels, book creation, and candidate enumeration.
- `LogicalInventory` and `AutomationPort` for GUI/session/automation inventory semantics.
- `ConversionTableSession` for conversion-table search, payment, generated-slot refresh, copy mode, and take behavior.
- `EnchantingTableSession` for enchanting-table add/remove/merge/split/export behavior, including incremental same-level merge mode.
- Common network intent dispatch helpers so platform payload handlers decode, validate menu type, and forward intent instead of owning business rules.

## Supported Version Matrix

NeoForge version projects cover:

- `1.21.1` through `1.21.11`
- `26.1`
- `26.1.1`
- `26.1.2`

Fabric version projects now mirror the NeoForge version range:

- `fabric_1_21_1` through `fabric_1_21_11`
- `fabric_26_1`
- `fabric_26_1_1`
- `fabric_26_1_2`

Fabric source layers are split by API shape:

- `legacy`: Minecraft `1.21.1`
- `legacy_pair_render`: early versions whose slot icon helper returns a pair-style no-item icon value
- `legacy_render`: early versions whose slot icon helper returns a single resource location
- `legacy_optional_tag`: Minecraft `1.21.5`
- `modern_legacy_input`: Minecraft `1.21.6` through `1.21.8`
- `modern`: Minecraft `1.21.9` through `1.21.10`
- `identifier`: Minecraft `1.21.11`
- `official26`: Minecraft `26.1+` Fabric official-names builds

The older notes may mention Fabric `26.x` as blocked by mappings/tooling. That was true at the time of the first master plan, but later work adopted the Fabric `26.1+` official-names / non-remap build model and added `26.1`, `26.1.1`, and `26.1.2`.

## Key Compatibility Fixes

- Fabric config is self-managed at `enchantment_custom_table.json`; shared JSON defaults and deprecated-field warning detection live in `JsonTableConfigCodec`, while Fabric file IO and logging live in `FabricTableConfig`.
- Recipe data was split by Minecraft data format:
  - `1.21.1` keeps object-style ingredients such as `{ "item": "minecraft:quartz_block" }`.
  - `1.21.2+` uses string-style ingredients such as `"minecraft:quartz_block"`.
- GUI empty-slot resources were restored for Fabric parity:
  - legacy `textures/item/empty_slot_book.png`
  - modern GUI sprite path for newer versions
- Fabric table slot helpers are version-layered so no-item icons use the correct return type for:
  - `1.21.1` through `1.21.3`
  - `1.21.4` through `1.21.10`
  - `1.21.11+` and `26.x`
- Fabric `26.x` projects use Fabric Loom `1.17.x`, Java 25, official Minecraft names, no mappings declaration, and normal Gradle dependency configurations.
- Transfer automation semantics were aligned so generated candidate slots are not exposed to automation and Fabric transfer rollback uses snapshot-style behavior.

## Validation Record

Historical validation recorded in the temporary documents includes:

- NeoForge Stonecutter version projects from `1.21.1` through `26.1.2` built successfully during the Stonecutter pilot.
- NeoForge representative GameTests passed on key transition versions including early `1.21.x`, `1.21.5+`, `1.21.9+`, `1.21.11`, and latest `26.x`.
- `:common:test` passed after the common/config/session refactors.
- Fabric `1.21.1` through `1.21.11` full matrix builds passed.
- Fabric `26.1.2` compile/build passed during the first official-names proof.
- Fabric `26.1`, `26.1.1`, and `26.1.2` were added later and included in the `:verifyCi` pass after the GUI sprite and recipe format fixes.
- Fabric server smoke tests reached the mod initialization log and then stopped at EULA as expected.

For release preparation, prefer the root verification/release aliases documented in `AGENTS.md`, especially `:verifyCi` and `buildReleaseArtifacts`.

## Remaining Follow-Ups

- Run manual GUI click testing for Fabric across representative versions, especially `1.21.1`, `1.21.9`, `1.21.11`, and latest `26.x`.
- Manually redraw or realign conversion-table GUI details after functional migration is stable.
- Consider adding Fabric-side GameTest or equivalent integration coverage if Fabric runtime regressions become frequent.
- Keep the Fabric version-source-layer table in sync whenever new Minecraft or Fabric API versions introduce signature changes.
- If `1.20.1` support resumes, start it in a separate repository and do not mix old NBT enchantment compatibility into this 1.21+ codebase.
