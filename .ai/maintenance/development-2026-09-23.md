# Latest and legacy compatibility follow-up

Status: completed with explicitly retained GUI/manual blockers. Base: `dev` at `852a0cf` after the completed 30-target tiered GUI round.

User direction:

- Defer cross-version table data/resource migration findings MIG-01 and MIG-02 as low priority; retain failures and evidence.
- Continue development without waiting for manual Shift-click and other uncovered scenarios. Never relabel these as passing.
- Push completed verified mainline work, investigate the latest release after 26.2, and adapt it on `dev`.
- Also verify the independent legacy ports on `codex/port-1.18.2`, `codex/port-1.19.2`, and `codex/port-1.20.1` (Forge/Fabric).

Plan: confirm official versions/dependencies; use separate local worktrees for the three existing legacy branches; run focused JVM/build checks and actual clients; record fixes, evidence, blocked input cases, and untested scope per target. Preserve previously validated results and use fresh isolated test worlds for new targets.

Latest-release sources checked 2026-09-23:

- https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3
- https://www.fabricmc.net/2026/09/15/263.html
- https://docs.neoforged.net/primer/docs/26.3/

26.3 release adaptation uses Fabric Loader 0.19.5 / API 0.161.0+26.3 and NeoForge 26.3.0.12-beta. ModDev 2.0.147 is required for the newer JST transformations. Both latest platform builds and Fabric's 40 JVM checks pass; the expanded full build matrix passed (32m13s; 298 tasks, 157 executed, 3 cache, 138 up-to-date).

26.3 GUI: both clients launched and displayed both table models and empty screens correctly. Found and fixed a real search-focused Escape bug (SDL uses InputConstants.KEY_ESCAPE rather than GLFW literal 256); actual keyboard retest passed on both loaders. CUA mouse movement is not delivered as current SDL coordinates, reproduced on vanilla title controls and both mod GUIs. C01 and V02 pass; V01 is partial (tooltip blocked), remaining business cases are BLOCKED, not passed. Original config/options restored after normal GUI exit. Raw evidence is under build/reports/gui-validation/round4.

Previous completed work pushed: origin/dev through 58034a0. New adaptation changes remain in progress.

Legacy: isolated worktrees on codex/port-1.18.2, codex/port-1.19.2 and codex/port-1.20.1. Added actual-inventory persistence tests and reproduced both emerald and emerald-block dirty-notification failures on all six loader targets before fixing. Shared payment fix applied; all three verifyAll checks now pass (38 Minecraft tests per loader plus common tests). GUI launcher export is being adapted for deferred Gradle 8 / ForgeGradle run configuration; this is test tooling, not a release dependency.

Legacy GUI plan: run the eight quick-suite scenarios on all six targets, emphasizing payment save/reload, copy synchronization and three split/return cycles; add strict/free configuration checks where the driver permits. Report only operations actually observed. Shift-click, multiplayer, automation and migration remain separate manual/deferred scope.

Legacy GUI progress: Fabric 1.18.2 Q01/Q02/Q05/Q06/Q07/Q08 observed passing. Q03 reproduced client-only copy material disappearance after world reload (server retains materials); server-only copy guard ported to all six targets. Q04 search suggestion overlap found on old EditBox; fixed for 1.18.2/1.19.2. All three branch builds pass after these fixes: 23 common plus 38 tests per loader each. Production commits: cbe3354, b9db390, 42a1988 (not pushed yet). Other five legacy GUI targets and configuration supplements remain pending.

Mainline adaptation commit 610ce47; buildReleaseArtifacts passed and produced exactly 32 version jars. Automated XML snapshot includes 1076 checks (including default/latest duplicate projects), zero failures/errors; some results are reused/up-to-date. Fabric 1.18.2 finished all eight Q cases plus six config cases: 14 PASS, two Shift cases BLOCKED; original options/config restored. Forge 1.18.2 stock launch failed due Intel LWJGL natives on ARM, now successfully reaches GUI with explicit test-only companion Loom LWJGL 3.3.2 override (`--legacy-forge-arm`); normal launch/release jars are not changed by the override. Forge GUI business tests and remaining legacy targets are still in progress.

2026-09-24 progress: Forge 1.18.2 also finished 14 PASS / 2 Shift BLOCKED with the documented ARM test runtime. Fabric 1.19.2 finished 14 PASS / 2 Shift BLOCKED, originals restored. Forge 1.19.2 starts successfully using its original LWJGL 3.3.1; its GUI suite and both 1.20.1 targets remain in progress. Consolidated report: tools/gui-validation/round4-2026-09-23.md.

Pushed codex/port-1.18.2 through c59f4ad (production cbe3354 plus branch GUI tooling/report/evidence). Mainline 610ce47 and other two legacy fixes still await final report commits/push.

1.19.2 both loaders finished 14 PASS / 2 Shift BLOCKED each and originals restored. Branch record commit 33759be plus b9db390 production fix pushed. Fabric 1.20.1 now launched for final legacy suite.

Fabric 1.20.1 completed 14 PASS / 2 Shift BLOCKED and originals restored. Forge 1.20.1 (47.2.0) launched normally, final GUI target in progress. 1.18.2 branch additionally pushed e778281 recording the non-blocking Narrator Intel native limitation.

Final 2026-09-24: all six legacy GUI targets finished 14 PASS / 2 Shift BLOCKED each (84 / 12 total). 26.3 both targets remain 2 PASS / 1 PARTIAL / 27 BLOCKED each. All eight new clients exited normally; all 16 config/options files verified restored. Mainline 32-target verifyAll/buildReleaseArtifacts and all three legacy verifyAll pass. Consolidated report and evidence: tools/gui-validation/round4-2026-09-23.md. Legacy report heads: e778281, 33759be, c682917. Mainline report/tooling finalization follows production 610ce47. Earlier progress paragraphs are chronological observations.
