# Table Storage Automation

Created: 2026-05-25
Archived: 2026-06-01
Source branch: `feature/table-automation-storage`
Base branch: `dev` (Minecraft 1.21.1)

## Goal

The storage automation feature gives both functional table blocks real block-entity storage and exposes controlled item automation through NeoForge item capabilities.

Before this work, the table inventories mostly lived in menus. That was enough for direct GUI interaction, but it was not a good model for persistence, block breaking, hoppers, or logistics mods. The new direction is:

- Block entities own the real inventory.
- Menus bind to block-entity storage for persistent slots.
- Right-side generated book choices remain virtual and are not saved.
- Automation calls the same business rules as player interaction.

## Shared Architecture

Each table block entity should be responsible for:

- Holding its real inventory.
- Saving and loading inventory data.
- Dropping stored items when the block is broken.
- Marking state changes and refreshing menu state.
- Exposing a restricted item handler capability.

Menus should be responsible for:

- Displaying real stored slots.
- Maintaining virtual generated-book slots.
- Preserving existing click, drag, and quick-move behavior.

The virtual generated-book area must not be written to NBT or exposed to automation.

## Enchanting Custom Table

Persistent and transient slot model:

- Slot 0: persistent tool or enchanted-book slot.
- Slot 1: transient enchanted-book input slot.
- Right-side generated slots: virtual output choices.

Automation behavior:

- Automation may insert enchanted books into slot 1.
- Inserted books must pass the same merge validation as GUI input.
- A successful insert immediately merges the enchantments into slot 0 and consumes the input book.
- Automation must not extract slot 0 by default, avoiding accidental removal of the core tool or book.

Player behavior:

- Players can still place and remove slot 0 items normally.
- Slot 1 keeps the existing immediate-merge-and-clear behavior.
- Generated slots keep the existing split, remove, and export interactions.

## Enchantment Conversion Table

Persistent slot model:

- Slot 0: normal books.
- Slot 1: emerald or emerald-block payment.
- Slot 2: enchanted-book template for copy mode.
- Slot 3: copy result.
- Right-side generated slots: virtual exchange choices.

Normal exchange mode is active when slot 2 is empty:

- The right-side generated area behaves like the original conversion table.
- Taking a generated book consumes one normal book and the configured payment.

Copy mode is active when slot 2 contains a legal template:

- The right-side generated area is disabled and cleared.
- Slot 3 becomes the copy result slot.
- If slot 0 and slot 1 have enough materials and slot 3 is empty, the table consumes one book plus payment and creates one copy of the template in slot 3.
- After slot 3 is emptied, the table can create the next copy if materials are still sufficient.

Template books must satisfy all of these rules:

- The item is an enchanted book.
- It has exactly one enchantment entry.
- The enchantment level is greater than 0.
- The enchantment level does not exceed that enchantment's max level.

Automation behavior:

- Automation may insert normal books into slot 0.
- Automation may insert configured payment items into slot 1.
- Automation may extract only from slot 3.
- Automation may not access slot 2 or the virtual generated area.

## Capability Rules

NeoForge item capability exposure should use restricted wrappers instead of exposing complete internal handlers.

Registered block item handlers should:

- Enforce per-table insert and extract rules.
- Trigger the same table logic after insert or extract.
- Call `setChanged()` after meaningful inventory changes.
- Keep generated virtual slots inaccessible.

This lets vanilla hoppers and common logistics mods automate safe paths without bypassing menu behavior.

## Test Expectations

GameTest is the preferred coverage layer because the feature is block-entity and container driven.

Expected coverage includes:

- Enchanting Custom Table slot 0 persists through save/load.
- Automation inserting a valid enchanted book updates the slot 0 item and consumes the input.
- Automation rejects invalid enchanted-book input.
- Generated virtual slots are not exposed through item capability.
- Conversion Table normal exchange mode still generates and gives selectable books.
- Conversion Table clears generated choices when a valid template is present.
- Conversion Table rejects multi-enchantment template books.
- Conversion Table rejects template books above the enchantment max level.
- Copy mode consumes materials and creates a template copy in slot 3.
- Copy mode refills slot 3 after the previous result is extracted when materials remain.
- Capability access allows only books and payment input, and only copy-result output.

Manual GUI checks remain important for:

- Persistent slots after closing and reopening the GUI.
- Block breaking item drops.
- Template mode visual clarity.
- Hopper input and output direction expectations.

## Migration Notes

When this feature is propagated beyond `dev`, the risky version-specific areas are:

- 1.21.9 transfer-slot and item handler API changes.
- 26.x `ContainerInput` click API.
- 26.x capability and screen API differences.

Version branches should preserve their local API adaptations while keeping the automation rules identical.
