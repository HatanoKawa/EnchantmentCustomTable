# Test Strategy

## Goals

Build a layered test system that improves stability without depending on a single fragile test style.

## Layer 1: JVM unit tests

Purpose: cover deterministic logic without launching Minecraft.

Current status:
- Configured JUnit 5 through Gradle.
- Added focused rule tests for pagination, generated-slot cache indexes, configured payment counts, and binary-style enchanted-book splitting.
- Verified with `./gradlew test`.

Planned scope:
- Page-count and slot-index calculations.
- Payment amount decisions for emerald / emerald block conversion.
- Payload operation parsing and invalid-input behavior.
- Pure enchantment merge/split rules after those rules are extracted from menu classes.

Verification command:
- `./gradlew test`

## Layer 2: NeoForge GameTest

Purpose: cover server-side in-game behavior inside Minecraft's test runner.

Current status:
- Added a minimal namespaced structure at `data/enchantment_custom_table/structure/gametest/empty.nbt`.
- Added GameTests for functional block placement, BlockEntity creation, menu binding, `stillValid` behavior, conversion-table payment/result clearing, and custom-table split/merge/remove flows.
- Verified with `./gradlew runGameTestServer`.

Planned scope:
- Blocks and block entities can be placed and loaded.
- Menu validity rejects removed blocks / distant players where practical.
- Conversion-table payment and generated enchanted-book behavior.
- Custom-table export / remove / merge flows using server-side containers.

Verification command:
- `./gradlew runGameTestServer`

Notes:
- GameTest is not equivalent to real UI interaction. It runs inside Minecraft and is good for server/world/container behavior.
- Structure templates should live under the mod namespace so `neoforge.enabledGameTestNamespaces=enchantment_custom_table` discovers them.

## Layer 3: UI smoke checks

Purpose: manually or semi-automatically validate client GUI behavior before releases.

Current status:
- Resumed by user request on 2026-09-22 for Fabric and NeoForge 1.21.1.
- An opt-in macOS application wrapper exports the existing `runClient` launch and lets Computer Use attach to the actual game window.
- The first round executed 28 cases per loader: 49 passed, 3 failed, 4 blocked. The failures represent three distinct defects; see [the report](../../tools/gui-validation/round1-2026-09-22.md).
- After merging the validation branch into `dev`, the [second round](../../tools/gui-validation/round2-2026-09-22.md) fixed those defects and retested both 1.21.1 clients: 52 passed, 0 failed, 4 blocked. This does not close Shift-click coverage or validate other game versions.
- `ConversionPaymentSessionTest` additionally exercises the actual Minecraft-dependent conversion session and persistent-inventory notification on emerald and block payments; both new cases failed before the fix and pass after it. These are Fabric JVM tests, separate from the GUI counts.

Planned scope:
- Launch `runClient`.
- Open both custom tables.
- Move items through the GUI.
- Click page buttons and export buttons.
- Confirm no client crash, no ghost item, and expected visual state.

Verification approach:
- Use the [launcher and fixture guide](../../tools/gui-validation/README.md), real GUI operations, screenshots, and server-side item observations together.
- Keep Shift-click and other unsupported input paths on the manual release checklist. Ordinary clicks do not cover quick-move.
- Treat the supervised Computer Use workflow as a regression aid; it is not yet an unattended CI runner.
