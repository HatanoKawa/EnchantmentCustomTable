# Test Refactor Execution

## Commit Plan

- Commit 1: Refactor production code so core table rules can be tested without driving GUI/container lifecycle.
- Commit 2: Add and update JVM tests plus NeoForge GameTests based on the current design goals.

## Scope

- UI direct testing with Computer Use is paused by request. Do not add UI automation code in this task.
- JVM tests should cover production code that is actually called by menus or shared services.
- GameTests should cover Minecraft-integrated behavior that cannot be represented as simple JVM tests.

## Target List

- [x] Audit and classify the previous incomplete test-setup residue.
- [x] Extract pagination, payment, enchanted-book split, and enchantment merge/subtract rules from menu lifecycle code.
- [x] Wire `EnchantingCustomMenu` and `EnchantmentConversionMenu` through the extracted rules without changing intended behavior unexpectedly.
- [x] Replace temporary/sample-only rule tests with JVM tests against production-used rules.
- [x] Add GameTests for block/entity/menu basics and design-level server-side flows where practical.
- [x] Run `./gradlew test`, `./gradlew runGameTestServer`, and `./gradlew build`.
- [x] Split final git history into one refactor commit and one test commit.

## Progress Log

- 2026-05-14: Execution plan created.
- 2026-05-14: Previous residue confirmed: JUnit dependency setup, temporary `EnchantmentTableRules`, initial GameTest scaffold, structure template, and UI smoke notes are present but uncommitted.
- 2026-05-14: Additional residue confirmed in `EnchantingCustomMenu`: partial refactor calls existed with broken formatting and an incomplete helper extraction. The plan is to complete and stabilize this work instead of preserving the half-applied state.
- 2026-05-14: Production refactor completed locally: menu pagination/payment/split/merge/subtract logic now routes through `EnchantmentTableRules`.
- 2026-05-14: Test layer refreshed: JVM tests now cover production-used rules, and GameTests cover conversion payment plus custom-table split/merge/remove flows.
- 2026-05-14: Verification passed with `./gradlew test`, `./gradlew runGameTestServer`, and `./gradlew build`.
- 2026-05-14: Final history split prepared as a production refactor commit followed by a test/documentation commit.
