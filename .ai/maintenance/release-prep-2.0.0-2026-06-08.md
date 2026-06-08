# Release Prep Archive: 2.0.0

Created: 2026-06-08
Branch: `codex/multiloader-spike`
Previous release baseline: `1.2.0`, released 2026-05-19, `master` / `FETCH_HEAD` at `1012db0`
Release candidate before prep commit: `c6984f6`

## Purpose

This document archives the last temporary planning and review notes from `/Users/river_quinn/workspace/ai_temp/EnchantmentCustomTable` before the 2.0.0 release preparation pass.

The temporary documents summarized here were moved to that directory's `_archived/2026-06-08/` folder after this project-level archive was created.

## Source Temporary Documents

- `2026-06-04/fabric-1.21.1-gui-renderer-sound-fix-plan.md`
- `2026-06-06/project-review-optimization-plan.md`
- `2026-06-07/neoforge-generated-slot-ghost-flicker-fix-plan.md`
- `ai_project_review_report.md`

The local `.DS_Store` file under `ai_temp` was ignored as non-project metadata.

## Release Decision

The project version was bumped from `1.2.0` to `2.0.0`.

This is a major release boundary because the current branch is no longer a narrow patch over 1.2.0. It contains:

- A multi-loader migration with Fabric support.
- A multi-version matrix for NeoForge and Fabric.
- Persistent table storage and automation behavior.
- Config key cleanup and behavior-oriented config naming.
- Recipe id migration and wider quartz-block recipe support.
- Shared rule/session/layout refactors across NeoForge and Fabric.
- Build, CI, and release artifact workflow changes.

## Archived Work Summary

### Fabric GUI parity and runtime behavior

The 2026-06-04 plan covered missing Fabric client behavior compared with NeoForge:

- Added Fabric block entity renderer registration for the table book animation.
- Added per-table book texture paths for custom blue/green book covers.
- Restored generated-slot placement/replacement behavior in Fabric menus.
- Restored GUI interaction sounds on Fabric.
- Added Fabric version-layer adaptations through `26.1.2`.

Key commit:

- `389345b fix: restore fabric table gui parity`

### Project review optimization pass

The 2026-06-06 optimization plan accepted the review report's main conclusion: the project did not need another broad rewrite, but did need cleanup of multi-version and multi-platform maintenance noise.

Completed milestones included:

- `4323a1b fix: sync fabric gui slot state`
- `7e73ece chore: remove deprecated config aliases`
- `28ddd38 doc: refresh multiloader project metadata`
- `49792fc refactor: avoid stale conversion enchantment cache`
- `7975d37 build: clear gradle script deprecations`
- `b2e52bf refactor: share conversion table layout constants`
- `7ed63f3 test: extract gametest assertions`
- `509e2c4 refactor: share enchanting table layout constants`
- `64d85b1 ci: extend gradle wrapper download timeout`

Important decisions from that pass:

- Deprecated config aliases were removed instead of kept for compatibility.
- The misspelled conversion recipe id was migrated directly for the major release.
- The conversion recipe now uses a custom quartz block item tag.
- Cross-platform config UI was not expanded beyond the lightweight NeoForge config screen because Fabric has no equally light built-in equivalent.
- Conversion table generated enchantment lists are rebuilt from the current registry instead of relying on a long-lived menu cache.

### Generated slot flicker fix

The 2026-06-07 plan covered generated-book slot ghost/flicker behavior in the custom enchanting table.

The final fix path was intentionally small:

- Guarded empty generated slots against stale pickup.
- Removed generated books atomically by cache index.
- Refreshed menu slot state before broadcasting changes.
- Acknowledged menu-owned block-entity inventory changes so bound inventory watchers did not treat them as external automation updates.
- Added GameTest coverage for generated slot holes surviving broadcast after removal.

Key commit:

- `4c9ba41 fix: avoid generated slot flicker after removals`

Related later fixes expanded this stability work to Fabric left-click removal, generated-slot sync, automation refresh, table drops, and Fabric texture alignment.

## Verification Notes

Historical verification recorded in the temporary plans includes:

- `:common:test`
- Representative NeoForge and Fabric builds across early, middle, latest, and `26.x` versions.
- `:verifyRepresentative`
- `:verifyCi`
- NeoForge `:1.21.1:runGameTestServer` with required GameTests passing.
- Manual GUI checks on NeoForge and Fabric performed by River during the stabilization loop.

For publishing, prefer `./gradlew buildReleaseArtifacts` to generate the final jar set under `build/release-artifacts/`.

## Remaining Follow-Ups

- Continue manual GUI checks on representative Fabric versions, especially after resource or slot-coordinate edits.
- Keep an eye on Fabric versioned source duplication; a deeper source-layer consolidation should be handled on a separate branch.
- If a game-side Fabric config screen becomes necessary, evaluate a lightweight optional dependency later rather than adding a custom UI now.
- For future releases, update this archive only with high-signal release decisions; do not copy temporary plans verbatim.
