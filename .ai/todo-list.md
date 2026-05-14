# AI Todo List

This folder stores AI-produced maintenance notes for the project.

## Stability review: 2026-05-12

- [x] P0: Harden server payload handlers against wrong menus and invalid operation strings.
- [x] P1: Stop client screens from mutating menu/item state directly before sending packets.
- [x] P1: Add menu validity checks so tables cannot be used after the block is gone or too far away.
- [x] P1: Replace nullable enchantment component reads with safe empty enchantment defaults.
- [x] P1: Replace fragile enchantment identity logic based on direct holders / registry ids with stable registry keys.
- [x] P1: Fix enchantment removal matching so it removes the selected enchantment instead of the first enchantment in the registry.
- [x] P2: Make conversion-table result pickup charge resources through the result slot take path.
- [x] P2: Replace conversion-table global integer enchantment cache with per-menu registered holders.
- [ ] P2: Add focused JVM tests or game tests for enchantment merge, split, export, and conversion-table payment paths.
- [ ] P3: Remove leftover template/debug code and unused GUI state maps once behavior is stable.

## Test system setup: 2026-05-14

- [ ] Add JVM unit-test infrastructure and a first passing smoke test.
- [ ] Add NeoForge GameTest infrastructure and a first discoverable passing GameTest.
- [ ] Add release UI smoke-test checklist for manual / Computer Use assisted validation.
- [ ] Backfill meaningful JVM tests for extracted pure logic.
- [ ] Backfill meaningful GameTests for table block/menu behavior.
