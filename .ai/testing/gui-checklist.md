# Manual GUI Checklist

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
