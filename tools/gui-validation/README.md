# Legacy GUI verification

Use the isolated creative test world only. Fixtures clear player inventory and replace the table at 0 -60 2; they prepare vanilla items but never call mod menu methods. This branch keeps its own Forge/Fabric projects and Java 17 toolchain.

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew --no-configuration-cache -I tools/gui-validation/export-client.init.gradle -PguiLegacy=true -PguiLoader=both -PguiVersion=1.18.2 :forge:exportGuiClientLaunch :fabric:exportGuiClientLaunch
python3 tools/gui-validation/macos_client.py --legacy --loader fabric --minecraft 1.18.2 --launch --keep-awake
```

Choose `--loader forge` for Forge. Copy `fixtures/` to the dedicated world's `datapacks/ectgui`, then `/reload`; commands use `/function ectgui...` aliases. Config/options must be backed up and restored. Evidence pairs F2 screenshots with `/function ectguiinspect` server inventory output. Never reset fixtures between an operation and its save/reload check.

Planned core cases on each loader:

| ID | GUI operation and expected result |
| --- | --- |
| Q01 | Both table models, textures, empty GUI layout and item tooltips render correctly. |
| Q02 | `cboundary`: 2 books / 35 emeralds give no output; add 1 emerald, search sharpness and take V. Reopen and save/reload: 1 book / zero payment, one owned V. |
| Q03 | `ccopy`: 3 books / 12 blocks + Sharpness V template; generate, take once and refill. Reopen and save/reload: 1 book / 4 blocks, template and next output V retained. |
| Q04 | `cbase`: paging, sharpness/no-match/clear search, text containing e, Esc/reopen; reload while menu open then take a result. |
| Q05 | `esync`: take four enchantments normally without ghost slots; fresh scene export all four to one book. |
| Q06 | `einsert`: input slot, empty generated slot and same-name slot insertion consume books once and reach expected levels. |
| Q07 | `esplit`: Looting III -> II -> III, three normal take/return cycles; no repeated gain or loss. |
| Q08 | Save/reload merged item and exported book, preserving counts and levels. |

Configuration supplement: after stopping client enable incrementalSameLevelMerge, enforceEnchantmentLevelLimit, convertOnlyLevelOneBook and freeConversionTableCosts. K01 equal II+II->III; K02 different-level/mixed book atomic rejection; K03 equal merges through V then reject above limit; K04 allow a new Looting IV entry; K05 free ordinary conversion returns I; K06 free copy retains template V and rejects payment/book input.

Shift extraction/insertion require manual follow-up because the current driver lacks modified mouse clicks. Multiplayer, automation/hoppers, full inventory, quick-craft, race/stress checks and migration are outside this focused round. Results must distinguish PASS, FAIL, BLOCKED and NOT_EXECUTED.

Forge 1.18.2 on Apple Silicon: the stock LWJGL natives are Intel-only. The explicit `--legacy-forge-arm` option uses only the companion Fabric Loom LWJGL jars/native runtime, leaving the original launch manifest, dependency cache and release jars unchanged. Export the companion Fabric task first, and label GUI results with this runtime difference.

本轮结果见 [round4-2026-09-23.md](round4-2026-09-23.md)。Forge 1.18.2 在 ARM Mac 可显式加 `--legacy-forge-arm` 使用同版本 Loom 的测试运行库；先导出两个客户端，此差异必须保留在结果中，不能用于声称普通运行环境已通过。
