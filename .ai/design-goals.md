# Design Goals

This document records the intended behavior of Enchantment Custom Table so future tests can use it as a functional specification.

## Overview

The mod provides two functional blocks:

- Enchanting Custom Table (`enchanting_custom_table`)
- Enchantment Conversion Table (`enchantment_conversion_table`)

## Enchanting Custom Table

The Enchanting Custom Table supports two main workflows:

- Separate, merge, and export enchantments on a tool or item.
- Split enchanted books into smaller enchanted books.

The GUI has three functional areas:

- Top-left first slot: tool slot. It can accept any item.
- Top-left second slot: enchanted-book input slot. It only accepts enchanted books.
- Right-side selection area: paginated enchanted-book slots generated from the current input item.

### Tool Enchantment Editing

When a player places an item into the tool slot, the right-side selection area should show one enchanted book for each enchantment currently on the item. The list is paginated when needed.

When a player removes an enchanted book from the selection area:

- The matching enchantment should be removed from the item in the tool slot.
- The removed enchanted book should be given to the player through normal container interaction.

When a player adds an enchanted book to either the selection area or the single enchanted-book input slot:

- Each enchantment on the added book should be added to the item in the tool slot.
- If the item already has the same enchantment, the enchantment levels should be added together.
- The mod should not enforce normal vanilla enchantment restrictions during these operations, including level caps and mutual exclusivity.

When a player clicks the export button:

- All enchantments on the item in the tool slot should be combined into one enchanted book.
- The combined enchanted book should be placed into the player's inventory.
- All enchantments should be removed from the original item.

### Enchanted Book Splitting

When a player places an enchanted book into the tool slot:

- If the book has multiple enchantments, the selection area should show one enchanted book per enchantment.
- Each generated book should keep the same enchantment level as the source book.

If the book has exactly one enchantment and its level is greater than 1:

- The selection area should show generated books carrying the same enchantment at smaller levels.
- The intended split strategy is binary-style splitting.
- Example: an input book with Sharpness 8 should generate Sharpness 4, Sharpness 2, and Sharpness 1 books.
- For odd levels, round down while splitting and avoid duplicate generated results.

If the book has exactly one enchantment at level 1:

- The selection area should not generate any enchanted books.

Adding and removing generated books should follow the same logic as tool enchantment editing.

## Enchantment Conversion Table

The Enchantment Conversion Table lets players exchange books plus emerald resources for enchanted books.

The GUI has three functional areas:

- Left first slot: book slot. It only accepts normal books.
- Left second slot: payment slot. It only accepts emeralds or emerald blocks.
- Right-side selection area: paginated enchanted-book slots generated from available enchantments.

When the book and emerald resource counts satisfy the configured cost:

- The selection area should show all obtainable enchantments in the current game as enchanted books.
- Each generated book should use the legal highest level for that enchantment.
- The list is paginated when needed.

When a player takes an enchanted book from the selection area:

- One normal book should be consumed.
- The configured emerald or emerald-block cost should be consumed.
- The selected enchanted book should be given to the player through normal container interaction.
- If resources become insufficient, the selection area should be cleared.

## Open Design Questions

- Define "obtainable enchantments" for the conversion table precisely: all registered enchantments, all non-curse enchantments, all trade/treasure discoverable enchantments, or another filtered set.
