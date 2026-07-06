# Enchantment Custom Table

<a href="https://www.curseforge.com/minecraft/mc-mods/enchantment-custom-table"><img src="http://cf.way2muchnoise.eu/versions/1229709.svg" alt="CurseForge versions"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/enchantment-custom-table"><img src="http://cf.way2muchnoise.eu/1229709.svg" alt="CurseForge downloads"></a>
<a href="https://modrinth.com/mod/enchantment-custom-table"><img src="https://img.shields.io/modrinth/dt/enchantment-custom-table?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth downloads"></a>

[中文说明](./README.zh_cn.md)

Tired of grinding the vanilla enchanting table, building libraries, and hoarding XP just to land the enchantments you actually want? This mod gives you two new workstations that let you rearrange enchantments as freely as you sort your inventory.

- **Enchanting Custom Table** lets you "mod" the enchantments on a tool or enchanted book directly — add one, remove one, split a high-level enchantment into lower ones, or merge two matching books into something stronger.
- **Enchantment Conversion Table** turns plain books plus a few emeralds into the enchanted books you want — and can even "photocopy" an exact duplicate of a book you give it.

> Out of the box, this mod is intentionally generous and easy to play with. If you (or a modpack you're building) want something more balanced and challenging, you can flip on a few stricter switches in the config — more on that below.

Grab a release from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/enchantment-custom-table) or [Modrinth](https://modrinth.com/mod/enchantment-custom-table). Found a bug or have an idea? Let us know on [GitHub Issues](https://github.com/HatanoKawa/EnchantmentCustomTable/issues).

## Supported Versions

There are builds for both the **NeoForge** and **Fabric** loaders, covering Minecraft `1.21.1` through `1.21.11`, plus `26.1`, `26.1.1`, and `26.1.2`.

**Tip:** when you download, make sure two things line up — your loader (NeoForge or Fabric) *and* your Minecraft version. Both have to match the jar, or the game won't load it.

## Enchanting Custom Table: rearrange your enchantments

![Enchanting Custom Table GUI](./src/main/resources/doc/enchantment_custom_table_gui.jpg)

Drop a **tool** or an **enchanted book** into the top-left slot, and the table lays out every enchantment on it as a separate enchanted book on the right. From there you can:

- **Want to remove an enchantment?** Just take that book from the right side — the matching enchantment comes off the item.
- **Want to add one?** Put an enchanted book in the input slot, or into any empty slot on the right, and it gets applied to the item.
- **Want to strengthen an enchantment you already have?** Stack a book onto the matching one on the right and the two merge together.
- **Want to break a high-level book apart?** Put a book that holds a *single* enchantment into the top-left slot, and it splits into a few lower-level books to choose from.
- **Want to clear everything at once?** Hit the export button — all enchantments come off the item and are bundled into a single enchanted book for you.

By default, two of the same enchantment **add their levels together** when merged. So Sharpness IV + Sharpness IV becomes Sharpness VIII. (If that feels too strong, the stricter config options below can rein it in.)

## Enchantment Conversion Table: trade plain books for enchanted ones

![Enchantment Conversion Table GUI](./src/main/resources/doc/enchantment_conversion_table_gui.jpg)

**Normal trade mode** is straightforward: add some plain books, then add enough emeralds or emerald blocks as the "fee." Once you've got enough, the right side lists the enchanted books you can get. Every book you take costs one plain book plus the emerald payment.

Too many books to scroll through? Use the **search box** at the top to filter by enchantment name.

What level you get is controlled by the `convertOnlyLevelOneBook` option: by default you get the **highest available level**; flip it on and you only get **level-one** books.

### Copy Template Mode: photocopy a book

Put an enchanted book into the **template slot** and the table switches to copy mode. The right-side trade list turns off, and instead: as long as the table holds one plain book plus enough emeralds, the output slot produces an **exact copy** of your template.

A book can only be used as a template if it meets these rules:

- It must be an **enchanted book** (not a tool).
- It must hold **exactly one** enchantment, not several at once.
- The enchantment level must be **greater than 0**.
- The level **must not exceed** that enchantment's normal maximum.

![Enchantment Conversion Table Automation GUI](./src/main/resources/doc/enchantment_conversion_table_automation_gui.jpg)

## Automation (hoppers, pipes, and the like)

Whatever you put in either table is **really stored there** — it stays put, and drops back out if the block is broken. Nothing disappears.

To keep hoppers and logistics mods from sneaking around the rules you'd follow by hand, automation is **deliberately limited**:

- **Enchanting Custom Table:** a hopper can feed enchanted books into the input slot, and they merge onto whatever item is already in the table — but the main tool/book inside **can't be pulled out** by automation.
- **Enchantment Conversion Table:** you can auto-feed plain books and emerald payment, but you can **only pull items out of the copy output slot**.
- The **template slot** must be set by a player by hand — automation can't insert or swap it.
- The trade books on the right are just on-screen choices for players, not real items, so automation can't touch them.
- **To auto-produce the same book over and over, use Copy Template Mode** rather than the right-side trade list.

The one rule worth memorizing: **a template book must hold a single enchantment whose level doesn't go above that enchantment's normal maximum.**

## Configuration: tune it to be more balanced

- **On NeoForge:** edit it right in-game through the Mod config screen, or in the loader-managed common config file.
- **On Fabric:** the config file lives in your Fabric `config` folder, named `enchantment_custom_table.json`.

| Option | Default | What it does |
| --- | --- | --- |
| `minimumEmeraldCost` | `36` | How many emeralds the conversion table charges. Set it to `0` to drop the emerald cost entirely. |
| `minimumEmeraldBlockCost` | `4` | How many emerald blocks it charges. Set it to `0` to drop the emerald-block cost. |
| `enforceEnchantmentLevelLimit` | `false` | When on, merging duplicate enchantments **can't go above that enchantment's normal max level**. Note: adding a brand-new enchantment isn't limited by this, so over-level books made by other mods still work. |
| `incrementalSameLevelMerge` | `false` | A more balanced merge rule. When on, two same-name books only merge if their levels **match exactly**, and a merge bumps the level by just **+1**. So Sharpness V + Sharpness V becomes Sharpness VI instead of Sharpness X. |
| `convertOnlyLevelOneBook` | `false` | When on, the conversion table only produces **level-one** books instead of max-level ones. |
| `freeConversionTableCosts` | `false` | When on, the conversion table produces and copies books without consuming plain books, emeralds, or emerald blocks. The book/payment slots stop accepting input. |

A couple of things to know:

- `enforceEnchantmentLevelLimit` and `incrementalSameLevelMerge` are independent — turn on either or both. With both on, same-level merges still won't exceed the normal max.
- Turning on `incrementalSameLevelMerge` also **changes how books split**: a Sharpness V book splits into two Sharpness IV books, instead of the default "split in half" options.
- `minimumEmeraldCost = 0` and `minimumEmeraldBlockCost = 0` only disable those specific payment items. Use `freeConversionTableCosts` if you want the whole conversion table to be free.

### Retired (deprecated) options

These are old option names that **no longer do anything**. If your old config file still has them, regenerate the file or rename them by hand:

- `ignoreEnchantmentLevelLimit` → now `enforceEnchantmentLevelLimit` (the meaning is reversed).
- `convert_max_level_book` → now `convertOnlyLevelOneBook` (the meaning is reversed).
- `enableXpRequirement` → removed, with no replacement.

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
