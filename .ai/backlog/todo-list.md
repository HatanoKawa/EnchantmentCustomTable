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
- [x] P2: Add focused JVM tests or game tests for enchantment merge, split, export, and conversion-table payment paths.
- [ ] P3: Remove leftover template/debug code and unused GUI state maps once behavior is stable.

## Test system setup: 2026-05-14

- [x] Add JVM unit-test infrastructure and a first passing smoke test.
- [x] Add NeoForge GameTest infrastructure and a first discoverable passing GameTest.
- [x] Add release UI smoke-test checklist for manual / Computer Use assisted validation.
- [x] Backfill meaningful JVM tests for extracted pure logic.
- [x] Backfill meaningful GameTests for table block/menu behavior.

## User priorities: 2026-09-23

- [ ] P3 / deferred by user: MIG-01 (old table enchantment components lost) and MIG-02 (old NeoForge inventory format not loaded). Preserve [round-three evidence](../../tools/gui-validation/round3-2026-09-22.md); do not spend the current development pass repairing cross-version saves/resources. This is a priority decision, not a fix or a passing migration result.
- [ ] Manual follow-up: E02 continuous Shift-click extraction and E06 Shift-click insertion remain blocked in the GUI driver, 36 checks across nine full-suite versions and two loaders. User will test when available; development may continue without these results. Record further gaps explicitly.
- [ ] Extend `dev` beyond 26.2 to the latest available supported release, with build and actual GUI verification.
- [ ] Verify the independent `codex/port-1.18.2`, `codex/port-1.19.2`, and `codex/port-1.20.1` branches on Forge and Fabric; keep their legacy build structures separate from the mainline matrix.
