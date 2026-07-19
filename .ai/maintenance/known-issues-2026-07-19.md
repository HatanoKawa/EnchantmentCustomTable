# Known Issues - 2026-07-19

## NeoForge 1.21.9 Mod List Crash

Status: accepted known issue; no code workaround planned for now.

Scope:

- Affects NeoForge Minecraft `1.21.9` when using NeoForge `21.9.16-beta`.
- Reproduced when opening the in-game Mods list, before entering this mod's configuration screen.
- Minecraft `1.21.1` and `1.21.8` were tested by the user and did not show this issue.

Observed crash:

- Crash report: `versions/1.21.9/run/crash-reports/crash-2026-07-19_14.09.50-client.txt`
- Main exception: `java.lang.UnsupportedOperationException`
- Stack points to `net.neoforged.neoforge.client.gui.ModListScreen.tick`, where NeoForge attempts to sort an immutable list.

Analysis summary:

- The crash happens inside NeoForge's own `ModListScreen` ticking path, not inside `ModConfigScreens` or this mod's `ConfigurationScreen` factory.
- NeoForge `21.9.16-beta` is the latest available NeoForge line for Minecraft `1.21.9`; there is no newer `21.9.x` version to upgrade to.
- Later NeoForge lines such as `21.10.64` and `21.11.42` appear to avoid this issue by collecting the mod list into a mutable `ArrayList`, but those target later Minecraft versions and cannot be used as the `1.21.9` loader dependency.

Decision:

- Do not add a mod-side workaround/mixin for this issue at this time.
- The issue is rooted in a specific NeoForge `1.21.9` Mod List implementation, and a workaround would require fragile client-side patching of NeoForge internals.
- Users on NeoForge `1.21.9` should edit the config file directly instead of using the in-game Mods list GUI.

Follow-up documentation:

- Mentioned in `.ai/maintenance/release-changelog-2.0.1.md` for the next release page.
- A short Known Issues note can still be added to `README.md` and `README.zh_cn.md` if this becomes common enough to warrant player-facing placement in the repository landing page.
- Suggested user-facing wording: On NeoForge `1.21.9`, opening the NeoForge Mods list may crash because of a NeoForge `21.9.16-beta` GUI issue. If this happens, edit the mod config file directly.
