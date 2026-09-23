# Latest and legacy compatibility follow-up

Status: in progress. Base: `dev` at `852a0cf` after the completed 30-target tiered GUI round.

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

26.3 is a released Minecraft target. Exact loader and API versions, adaptation results, and legacy results are pending.
