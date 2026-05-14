# Test Strategy

## Goals

Build a layered test system that improves stability without depending on a single fragile test style.

## Layer 1: JVM unit tests

Purpose: cover deterministic logic without launching Minecraft.

Planned scope:
- Page-count and slot-index calculations.
- Payment amount decisions for emerald / emerald block conversion.
- Payload operation parsing and invalid-input behavior.
- Pure enchantment merge/split rules after those rules are extracted from menu classes.

Verification command:
- `./gradlew test`

## Layer 2: NeoForge GameTest

Purpose: cover server-side in-game behavior inside Minecraft's test runner.

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

Planned scope:
- Launch `runClient`.
- Open both custom tables.
- Move items through the GUI.
- Click page buttons and export buttons.
- Confirm no client crash, no ghost item, and expected visual state.

Verification approach:
- Use a documented release checklist first.
- Computer Use can assist by operating the local Minecraft client window, but this should be treated as a smoke-test helper rather than CI-grade automation.

