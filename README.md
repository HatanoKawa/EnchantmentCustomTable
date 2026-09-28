# Enchantment Custom Table

<a href="https://www.curseforge.com/minecraft/mc-mods/enchantment-custom-table"><img src="http://cf.way2muchnoise.eu/versions/1229709.svg" alt="CurseForge versions"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/enchantment-custom-table"><img src="http://cf.way2muchnoise.eu/1229709.svg" alt="CurseForge downloads"></a>
<a href="https://modrinth.com/mod/enchantment-custom-table"><img src="https://img.shields.io/modrinth/dt/enchantment-custom-table?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth downloads"></a>

[中文说明](./README.zh_cn.md)

Tired of grinding the vanilla enchanting table, building libraries, and hoarding XP just to land the enchantments you actually want? This mod gives you two new workstations that let you rearrange enchantments as freely as you sort your inventory.

- **Enchanting Custom Table** lets you add, remove, split and merge enchantments on tools and enchanted books.
- **Enchantment Conversion Table** turns plain books plus a configurable payment into enchanted books, and can copy a template book.

The defaults are intentionally generous. Modpacks and players can adjust the payment items, prices and enchantment rules below.

Download releases from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/enchantment-custom-table) or [Modrinth](https://modrinth.com/mod/enchantment-custom-table). Report bugs and suggestions on [GitHub Issues](https://github.com/HatanoKawa/EnchantmentCustomTable/issues).

## Supported versions and installation

**This branch (`dev`) targets Minecraft `26.1`, `26.1.1`, `26.1.2`, `26.2`, `26.3` on NeoForge and Fabric, with Java 25.** This README describes the branch's source code, including the structured payment configuration introduced in **2.1.0**; available downloads may lag behind the branch. Check the selected release's Minecraft version, loader and changelog.

The active maintenance branches cover the following targets. Every listed Minecraft version has both loaders shown in its row; a series name does not imply support for every release in that series.

| Branch | Supported Minecraft versions | Loaders | Game Java version |
| --- | --- | --- | --- |
| [`dev`](https://github.com/HatanoKawa/EnchantmentCustomTable/tree/dev) | `26.1`, `26.1.1`, `26.1.2`, `26.2`, `26.3` | NeoForge / Fabric | 25 |
| [`maint/1.21.x`](https://github.com/HatanoKawa/EnchantmentCustomTable/tree/maint/1.21.x) | `1.21.1`, `1.21.2`, `1.21.3`, `1.21.4`, `1.21.5`, `1.21.6`, `1.21.7`, `1.21.8`, `1.21.9`, `1.21.10`, `1.21.11` | NeoForge / Fabric | 21 |
| [`maint/1.20.x`](https://github.com/HatanoKawa/EnchantmentCustomTable/tree/maint/1.20.x) | `1.20.1` | Forge / Fabric | 17 |
| [`maint/1.19.x`](https://github.com/HatanoKawa/EnchantmentCustomTable/tree/maint/1.19.x) | `1.19.2` | Forge / Fabric | 17 |
| [`maint/1.18.x`](https://github.com/HatanoKawa/EnchantmentCustomTable/tree/maint/1.18.x) | `1.18.2` | Forge / Fabric | 17 |

Install the matching loader and place the matching mod jar in `mods/`. Fabric also requires **Fabric API** for that Minecraft version. Install the mod on **both the client and server** for multiplayer; use matching mod versions so configuration synchronization works. Each instance needs only the jar for its own loader and Minecraft version.

## Enchanting Custom Table: rearrange your enchantments

![Enchanting Custom Table GUI](./src/main/resources/doc/enchantment_custom_table_gui.jpg)

The GUI screenshots in this README are from earlier versions and illustrate the basic layout. Slot positions and payment icons may differ in your version; the in-game payment tooltip shows the effective prices.

Put a tool or enchanted book in the main slot. The right side shows the enchantments you can remove, or split options for a book with a single enchantment above level I.

- **Remove:** take a displayed enchantment book to remove that enchantment from the item.
- **Add or merge:** put an enchanted book in the input slot or a suitable slot on the right. Its enchantments are applied to the main item, consuming the input book.
- **Split:** with a single-enchantment book above level I, take one of the lower-level choices. Under the default rules, its level is subtracted from the source and the choices refresh; for example, taking Sharpness II from Sharpness V leaves Sharpness III in the main slot. The displayed choices are previews, not extra stored books.
- **Export:** the export button collects a tool's enchantments into one book. If the main item is already an enchanted book, it returns that book instead.

By default, matching enchantments **add their levels together**: Sharpness IV + Sharpness IV becomes Sharpness VIII. The configuration below can require equal levels and/or reject merges above the enchantment's normal maximum.

## Enchantment Conversion Table: trade plain books for enchanted ones

![Enchantment Conversion Table GUI](./src/main/resources/doc/enchantment_conversion_table_gui.jpg)

In **normal trade mode**, each book you take costs **one plain book plus one payment option**. The defaults are **36 emeralds OR 4 emerald blocks OR 1 nether star**. Put one accepted item type in the payment slot; different options cannot be mixed to make up a price. The default nether star option comes from a player's suggestion.

The right side lists available enchantments when there are enough materials. Use the search box and page controls to find a book. Normal trades give the enchantment's **normal maximum level**, or **level I** when `convertOnlyLevelOneBook` is enabled. Payment is deducted when you take a book.

### Copy template mode

Place a valid enchanted book in the **template slot** to replace the normal trade list with copying. A template must contain **exactly one enchantment**, with a level **above 0 and no higher than its normal maximum**. Tools, books with multiple enchantments and over-level books are not valid templates.

When the copy output is empty and materials are available, the table consumes one plain book and the configured payment, then stores an exact copy in the output slot. **The template is retained.** Copying preserves its level and other item data; `convertOnlyLevelOneBook` only affects normal trades. Materials are charged **when the copy is generated**, so an already-produced output has already been paid for. In free mode, both trades and copies need no materials, but template restrictions still apply.

![Enchantment Conversion Table Automation GUI](./src/main/resources/doc/enchantment_conversion_table_automation_gui.jpg)

## Storage and automation

The tables store their actual inventory in the block, including unused materials, the main item/template and produced copy output. These stored items survive closing the screen and saving the world, and drop when the block is broken. The right-side enchantment choices are derived previews and do not create extra drops.

- **Enchanting Custom Table:** automation can insert enchanted books that can merge into an item already in the table. A player must place and retrieve the main item; automation cannot extract it.
- **Enchantment Conversion Table:** automation can insert plain books and accepted payment items, and extract only the **copy output**.
- The template slot is managed by players; automation cannot insert, replace or extract its book.
- Normal trade choices cannot be extracted by automation. Use a template for repeated automatic production.
- In free mode, the book and payment slots reject new input. Existing items can still be taken out by a player, and automation can still extract produced copies.

## Configuration

Paths below are relative to the game instance or dedicated server directory. Files are created on first launch.

- **NeoForge: `config/enchantment_custom_table-common.toml`.** Edit the file or open this mod's configuration screen through the Mods list. Its payment editor supports adding, deleting and editing rows, plus save, cancel and defaults.
- **Fabric: `config/enchantment_custom_table.json`.** Edit this file directly; this mod does not provide a Fabric configuration screen.

| Option | Default | Effect |
| --- | --- | --- |
| `paymentOptions` | 36 emeralds / 4 emerald blocks / 1 nether star | Alternative payments for conversion and copying; one option plus one plain book per output. |
| `enforceEnchantmentLevelLimit` | `false` | Rejects a **duplicate-enchantment merge** if the result exceeds its normal maximum. Adding an enchantment the item does not already have is not capped by this option. |
| `incrementalSameLevelMerge` | `false` | Duplicate enchantments must have equal levels; merging increases the level by one instead of adding both levels. Also changes single-enchantment book splitting as described below. |
| `convertOnlyLevelOneBook` | `false` | Normal trades produce level-I books instead of the normal maximum. Template copies keep the template's level. |
| `freeConversionTableCosts` | `false` | Trades and copies consume neither plain books nor payment items. Both material slots reject new input. |

`enforceEnchantmentLevelLimit` and `incrementalSameLevelMerge` can be enabled independently. With incremental merging alone, Sharpness V + Sharpness V becomes Sharpness VI; with the level limit also enabled, that merge is rejected. Incremental splitting is the reverse: taking a Sharpness IV from a single-enchantment Sharpness V book leaves another Sharpness IV in the main slot.

### Complete default configuration examples

`configVersion` is format metadata managed by the mod, not a difficulty setting. Keep it at `2`. Costs are integers, without quotes. When customizing an existing file, preserve any other settings you want to keep.

Fabric JSON:

```json
{
  "configVersion": 2,
  "paymentOptions": [
    { "item_id": "minecraft:emerald", "cost": 36 },
    { "item_id": "minecraft:emerald_block", "cost": 4 },
    { "item_id": "minecraft:nether_star", "cost": 1 }
  ],
  "enforceEnchantmentLevelLimit": false,
  "incrementalSameLevelMerge": false,
  "convertOnlyLevelOneBook": false,
  "freeConversionTableCosts": false
}
```

NeoForge common TOML:

```toml
configVersion = 2
paymentOptions = [
  { item_id = "minecraft:emerald", cost = 36 },
  { item_id = "minecraft:emerald_block", cost = 4 },
  { item_id = "minecraft:nether_star", cost = 1 }
]
enforceEnchantmentLevelLimit = false
incrementalSameLevelMerge = false
convertOnlyLevelOneBook = false
freeConversionTableCosts = false
```

### Payment validation

Use full item IDs such as `minecraft:nether_star` or a registered modded item ID. Rules match the item ID only: no item tags, NBT/component requirements or container returns. Hover over the question mark in the payment area to see effective costs.

- Plain books (`minecraft:book`), enchanted books (`minecraft:enchanted_book`), air, invalid/unregistered IDs and missing/non-integer costs are ignored. The first valid entry for a duplicate ID wins; at most 256 entries are accepted.
- If no usable entries remain, the three defaults are restored. An empty list does **not** disable the table or make it free.
- Effective cost is `max(1, min(default item stack limit, payment slot limit of 64, configured cost))`. Zero and negative values become 1; excessive costs are capped. For example, an item that stacks to 16 costs at most 16. A particular stack's custom components do not reduce the fixed price. The configuration must still be valid JSON/TOML.
- **Delete an entry to disable that payment.** Zero no longer means disabled. Use `freeConversionTableCosts` for free trades, or remove/disable the block's recipe in your modpack to prevent players from crafting it.

### Existing configurations and migration

When `paymentOptions` is absent, the old `minimumEmeraldCost` and `minimumEmeraldBlockCost` fields are migrated automatically. The original file is retained beside it with the suffix **`.payment-v1.bak`** before the first migration.

- Positive legacy costs become list entries; zero-cost legacy entries are omitted. If both are zero, the new three-option defaults apply.
- An existing custom list or a nonempty migrated list does **not** automatically gain a nether star entry. Add it yourself if desired. The four current gameplay flags keep their values.
- Older `ignoreEnchantmentLevelLimit` and `convert_max_level_book` fields are ignored, not automatically migrated. To preserve their old meaning, set `enforceEnchantmentLevelLimit` and `convertOnlyLevelOneBook`, respectively, to the **opposite Boolean value**. Renaming while keeping the value reverses the behavior.
- `enableXpRequirement` is removed with no replacement.

Back up your configuration before manual changes; there is no need to delete the whole file to migrate.

### Server authority and applying changes

In multiplayer, **the server controls the payment list and all four gameplay flags**. Its rules override the client's local settings for that connection. They are not written into the client's file and are cleared on disconnect. While connected to a remote server, the NeoForge configuration screen displays server rules as read only; edit the server's file to change them. Singleplayer uses the local instance's configuration.

NeoForge applies valid file edits while running and synchronizes them to connected players. A malformed edit keeps the last valid rules and leaves the file available for repair. On Fabric, **restart the world or dedicated server after editing**. If a file cannot be parsed at startup, repair it using the log's error message; it is not automatically overwritten with defaults. Trading waits until valid server rules are available.

## How to craft the two tables

In both recipes, the starred (\*) slots accept **any kind of quartz block** — specifically these (the `enchantment_custom_table:quartz_blocks` item tag):

- Quartz Block
- Chiseled Quartz Block
- Smooth Quartz Block
- Quartz Pillar
- Quartz Bricks

### Enchanting Custom Table recipe

![Enchanting Custom Table Recipe](./src/main/resources/doc/enchanting_custom_table_recipe.png)

Lay it out on the 3×3 crafting grid like this:

| Left | Middle | Right |
| :---: | :---: | :---: |
| Lapis Block | Book | Lapis Block |
| Diamond Block | Quartz Block* | Diamond Block |
| Quartz Block* | Quartz Block* | Quartz Block* |

\* Starred slots take **any of the quartz blocks** listed above.

### Enchantment Conversion Table recipe

![Enchantment Conversion Table Recipe](./src/main/resources/doc/enchantment_conversion_table_recipe.png)

Lay it out on the 3×3 crafting grid like this:

| Left | Middle | Right |
| :---: | :---: | :---: |
| Lapis Block | Book | Lapis Block |
| Emerald Block | Quartz Block* | Emerald Block |
| Quartz Block* | Quartz Block* | Quartz Block* |

\* Starred slots take **any of the quartz blocks** listed above.

## Building this branch

Use JDK 25. Run the wrapper from this checkout's root:

```sh
./gradlew :common:test
./gradlew :26.3:build :fabric_26_3:build
./gradlew :verifyAll
./gradlew :buildReleaseArtifacts
```

The second command builds one supported Minecraft version for both loaders. `verifyAll` checks the whole branch; `buildReleaseArtifacts` builds and collects only this branch's publishable jars into `build/release-artifacts/`. Other maintenance families must be built from their own branches. See [AGENTS.md](./AGENTS.md) for build details and [GUI validation](./tools/gui-validation/README.md) for in-game checks.
