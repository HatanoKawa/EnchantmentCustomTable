# Platform Decoupling Refactor Execution

Created: 2026-06-02
Source branch: `dev` (Minecraft 1.21.1 / NeoForge)

## Scope

This refactor starts the platform-decoupling plan before Fabric and Forge migrations.

The first executed slice keeps player-visible behavior unchanged and focuses on the lowest-risk boundaries:

- Configuration values are exposed through a `TableConfigView` snapshot.
- Core table rules are split into loader-independent classes under `core/rules`.
- The existing `utils.EnchantmentTableRules` class remains as a compatibility facade for Minecraft-facing callers.
- Menu and block-entity paths now use the config snapshot where table behavior depends on config.
- JVM tests cover the pure rule layer without requiring a Minecraft runtime classpath.

## New Boundaries

`core/config` owns immutable configuration views:

- `TableConfigView`
- `TableConfigSnapshot`

`core/rules` owns pure rules:

- `MergeRules`
- `SplitRules`
- `PaymentRules`
- `PaginationRules`
- `CopyRules`

The facade `EnchantmentTableRules` still owns Minecraft-specific integration points:

- `ItemStack` payment mutation
- `Items.EMERALD` / `Items.EMERALD_BLOCK` mapping
- `Holder<Enchantment>` and `ItemEnchantments` merge/subtract adapters

This keeps current NeoForge behavior stable while giving Fabric and 1.20.1 migrations a smaller surface to replace later.

## Validation

Validated on `dev`:

- `./gradlew test`
- `./gradlew build`
- `./gradlew runGameTestServer`

GameTest result: 23 required tests passed.

## Follow-Up Boundary Work

The next refactor slices should continue with:

- `EnchantmentAccess` for 1.21 data components vs 1.20.1 NBT enchantment storage.
- Session/controller extraction for generated slots and menu operations.
- Logical inventory adapters for persistent slots, virtual generated slots, and automation handlers.
- Network intent handlers that only translate payloads into menu/session operations.

These should be done after this first slice has been merged through the NeoForge version chain and verified.
