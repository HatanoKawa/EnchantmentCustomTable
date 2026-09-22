# Manual GUI Checklist

## Executed 1.21.1 baseline

The 2026-09-22 round covers both Fabric and NeoForge with real GUI input.
See [repeatable cases](../../tools/gui-validation/cases-1.21.1.md) and
[results and defects](../../tools/gui-validation/round1-2026-09-22.md).
The [second round](../../tools/gui-validation/round2-2026-09-22.md) retested fixes on `dev`
at `81bb884`: each loader passed 26 cases with no failures and 2 blocked cases.
Shift-click scenarios remain blocked by the current input tool; the ordinary-click
successes do not close the historical Shift-click regression below.

## Conversion payment persistence and copy-slot synchronization

Scope: Fabric and NeoForge 1.21.1. R1-01 and R1-02 were fixed and retested in round 2.

1. Fill a conversion table with 2 books and 36 emeralds, then pause to save the chunk.
2. Resume, take one Sharpness V book, and confirm the table holds 1 book and no payment.
3. Save to the title screen and re-enter the world without resetting the fixture.
4. Check both GUI slots and server item data: spent materials must not return.
5. In a separate fixture, supply 3 books, 12 emerald blocks and a Sharpness V copy template.
6. Take one copy and confirm the automatic refill leaves 1 book, 4 blocks, the template and the output.
7. Save and re-enter again. All four persistent slots must agree with the server data.

## Ordinary split-book removal

Scope: Fabric and NeoForge 1.21.1. Fabric R1-03 was fixed and retested in round 2.

1. Put a Looting III book into the enchanting table.
2. Take the generated Looting I book with an ordinary click and put it in an empty hotbar slot.
3. Check the source is Looting II and the player holds Looting I.
4. Reinsert that book into a generated slot; confirm the source returns to III and the player's book is consumed.
5. Repeat three times, checking the GUI and server data after each step. A drop to source I after one take is a failure.

## Fabric Enchanting Custom Table Generated Slot Sync

Scope: Fabric 1.21.1, Fabric 1.21.11, and Fabric 26.1.2 representative clients.

Scenario:

1. Open the enchanting custom table.
2. Place a sword or tool with four enchantments into the tool slot. Treat the generated books as A, B, C, and D from left to right.
3. Shift-left-click the first generated book A.
4. Confirm the player inventory receives book A and the generated area shows `empty, B, C, D`.
5. Shift-left-click the second generated book B.
6. Confirm the player inventory receives books A and B and the generated area shows `empty, empty, C, D`.
7. Click generated slots 3 and 4 and confirm C and D can still be taken normally.
8. Confirm the tool enchantments match the removed books and no ghost books remain on the client.

Regression signature:

- If the client shows `C, D, empty, D` after taking B, the Fabric generated slot cache is out of sync with the server state.

## Fabric Enchantment Conversion Table Layout

Scope: Fabric 1.21.1, Fabric 1.21.11, and Fabric 26.1.2 representative clients.

Checks:

1. Open the enchantment conversion table.
2. Confirm the book slot is at `8, 23` and the payment slot is at `26, 23`.
3. Confirm generated enchanted book slots begin at `44, 23`.
4. Confirm the template slot is at `8, 77` and the copy result slot is at `26, 77`.
5. Confirm the page label appears centered at `25, 60`.
6. Confirm previous/next page buttons use the same placement and size as the current NeoForge GUI.

## Enchantment Conversion Table Reload Boundary

Scope: NeoForge 1.21.1, NeoForge 26.1.2, Fabric 1.21.1, and Fabric 26.1.2 representative clients.

Checks:

1. Open the enchantment conversion table with enough books and payment to show generated results.
2. Run `/reload` while the table screen is open.
3. Change the search text or use page controls to force the generated list to refresh.
4. Confirm the generated enchanted book list is still valid and no stale/ghost entries remain.

Notes:

- The session should rebuild its enchantment list from the current registry on each generation pass instead of keeping a long-lived per-menu cache.
