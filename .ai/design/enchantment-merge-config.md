# Enchantment Merge Configuration

Created: 2026-05-20
Archived: 2026-05-25
Source branch: `dev`

## Purpose

This document records the intended behavior for the configurable enchantment merge rules introduced after the 1.2.0 refactor.

The default gameplay remains intentionally powerful: duplicate enchantment levels are added directly and can exceed vanilla caps. The new options are opt-in controls for players who want stricter balance.

## Boolean Config Semantics

Boolean options should use the same mental model where possible:

- Default `false` means no extra restriction or alternate behavior is enabled.
- Players opt into stricter or alternate behavior by setting a value to `true`.
- Default behavior should remain compatible with the original mod design.

Current behavior-oriented boolean options:

- `enforceEnchantmentLevelLimit = false`
- `incrementalSameLevelMerge = false`
- `convertOnlyLevelOneBook = false`

Deprecated compatibility options are retained only to emit migration warnings when set to a non-default value:

- `ignoreEnchantmentLevelLimit`
- `convert_max_level_book`

Deprecated options must not participate in runtime behavior. This keeps the active behavior controlled by one clear path.

## Level Limit Enforcement

`enforceEnchantmentLevelLimit` controls whether the custom enchanting table obeys vanilla maximum levels when adding or merging enchantments.

When `false`:

- Existing default behavior is preserved.
- Duplicate enchantments can exceed vanilla max level.

When `true`:

- Adding a new enchantment above its vanilla max level is rejected.
- Merging duplicate enchantments is rejected if the result would exceed vanilla max level.

This option is independent from incremental merge mode.

## Incremental Same-Level Merge

`incrementalSameLevelMerge` is an optional balance mode for duplicate enchantments.

When `false`:

- Duplicate enchantments use the original direct-add mode.
- Example: Sharpness 4 + Sharpness 4 becomes Sharpness 8 if level limits are not enforced.

When `true`:

- Duplicate enchantments can merge only when the existing level and added book level are identical.
- A successful duplicate merge increases the level by exactly 1.
- Example: Sharpness 5 + Sharpness 5 becomes Sharpness 6 if level limits allow it.
- Example: Sharpness 5 + Sharpness 4 is rejected.

Adding a new enchantment that is not already present on the target item is not restricted by incremental mode, except for level limit enforcement.

Books with multiple enchantments are handled atomically. If any enchantment on the book is invalid for the active rules, the entire book is rejected and no partial merge is applied.

## Single-Enchantment Book Splitting

The custom enchanting table can split an enchanted book placed in the first slot.

When `incrementalSameLevelMerge = false`, single-enchantment books keep the historical binary split behavior:

- Sharpness 8 generates Sharpness 4, Sharpness 2, and Sharpness 1.
- Sharpness 5 generates Sharpness 2 and Sharpness 1.
- Level 1 books generate no split candidates.

When `incrementalSameLevelMerge = true`, single-enchantment books use a minus-one pair:

- Sharpness 8 generates two Sharpness 7 books.
- Sharpness 2 generates two Sharpness 1 books.
- Level 1 books generate no split candidates.

The duplicate pair is intentional. It visually communicates that one higher-level book is formed by merging two same-level lower books in incremental mode.

Taking one split result in incremental mode should reduce the source book by only one level. For example, taking a Sharpness 4 split result from a Sharpness 5 source should leave Sharpness 4 in the source slot.

## Conversion Table Level Option

`convertOnlyLevelOneBook` controls the enchantment conversion table output level.

When `false`:

- The conversion table generates highest-level enchanted books.
- This preserves the default behavior.

When `true`:

- The conversion table generates level-one enchanted books only.

## Rule Ownership

Merge and split behavior should be owned by shared rule code rather than scattered UI slot checks.

Expected callers include:

- Slot `mayPlace` checks.
- Drag insertion.
- Shift quick move.
- Generated-slot click handling.
- Direct menu methods used by tests.
- Generated split candidate creation.

This keeps config behavior from being accidentally bypassed by a new interaction path.

## Test Expectations

Coverage should include:

- Default direct-add merge can exceed vanilla max level.
- `enforceEnchantmentLevelLimit = true` rejects over-cap merge results.
- Incremental mode accepts same-level duplicate merges and adds only one level.
- Incremental mode rejects different-level duplicate merges.
- Incremental mode and level-limit enforcement combine correctly.
- New enchantments can still be added in incremental mode.
- Multi-enchantment books are rejected atomically when any entry is invalid.
- Incremental split mode generates two `sourceLevel - 1` books.
- Taking an incremental split book only decreases the source book by one level.
- Conversion table defaults to highest-level output.
- `convertOnlyLevelOneBook = true` generates level-one output.
