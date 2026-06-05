# Enchantment Custom Table

<a href="https://www.curseforge.com/minecraft/mc-mods/enchantment-custom-table"><img src="http://cf.way2muchnoise.eu/versions/1229709.svg" alt="CF"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/enchantment-custom-table"><img src="http://cf.way2muchnoise.eu/1229709.svg" alt="CF"></a>
<a href="https://modrinth.com/mod/enchantment-custom-table"><img src="https://img.shields.io/modrinth/dt/enchantment-custom-table?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"></a>

Enchantment Custom Table adds workstations for freely editing enchantments on tools and exchanging books for enchanted books. It is built for players who prefer direct enchantment management over random rolls.

This repository now contains both NeoForge and Fabric builds. NeoForge is the primary platform and is supported through Minecraft `1.21.1` to `26.1.2`. Fabric version projects are included for Minecraft `1.21.1` to `1.21.11` and `26.1` to `26.1.2`.

Report issues through [GitHub Issues](https://github.com/HatanoKawa/EnchantmentCustomTable/issues). Published builds are available on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/enchantment-custom-table) and [Modrinth](https://modrinth.com/mod/enchantment-custom-table).

## Usage

### Enchantment Custom Table

![enchantment custom table gui](./src/main/resources/doc/enchantment_custom_table_gui.jpg)

Place a tool or an enchanted book containing multiple enchantments in the top-left slot. All enchantments on the item are displayed as individual enchanted books in the 4x6 grid on the right.

- Taking an enchanted book removes the matching enchantment from the tool.
- Placing an enchanted book applies its enchantment to the tool.
- Duplicate enchantments merge their levels by default. For example, Sharpness I plus Sharpness II becomes Sharpness III.
- The single book slot next to the tool slot immediately consumes valid enchanted books, which makes shift-click insertion convenient.
- The export button removes all enchantments from the tool and returns them as one enchanted book. If the inventory is full, the book is dropped in front of the player.

Recipe:

![enchantment custom table recipe](./src/main/resources/doc/enchanting_custom_table_recipe.png)

### Enchantment Conversion Table

![enchantment conversion table gui](./src/main/resources/doc/enchantment_conversion_table_gui.jpg)

Place a book and enough emeralds or emerald blocks in the left slots. When the configured cost is met, the 4x7 grid displays available enchanted books. Taking one generated book consumes the payment and book.

Recipe:

![enchantment conversion table recipe](./src/main/resources/doc/enchantment_conversion_table_recipe.png)

The recipes accept full quartz block variants through the `enchantment_custom_table:quartz_blocks` item tag: quartz block, chiseled quartz block, smooth quartz, quartz pillar, and quartz bricks.

## Configuration

The default behavior is intentionally powerful. Optional config values can add stricter balance rules:

- `minimumEmeraldCost`: emerald cost for the conversion table.
- `minimumEmeraldBlockCost`: emerald block cost for the conversion table.
- `enforceEnchantmentLevelLimit`: when `true`, duplicate enchantment merges cannot exceed the vanilla maximum level.
- `incrementalSameLevelMerge`: when `true`, duplicate enchantments only merge with the same level and increase by one level.
- `convertOnlyLevelOneBook`: when `true`, the conversion table generates level-one enchanted books.

NeoForge uses the loader-managed common config file. Fabric writes `enchantment_custom_table.json` under the Fabric config directory. This mod does not currently provide a cross-platform in-game config screen; edit the config file directly and restart or reload through the loader-supported path for the platform being tested.

Older development aliases such as `ignoreEnchantmentLevelLimit` and `convert_max_level_book` have been removed for the next major release. Regenerate or manually migrate old config files if those keys are still present.

## Development

Use the Gradle wrapper from the repository root. Java 21 is the baseline for Minecraft `1.21.x`, and Java 25 is required for Minecraft `26.x`.

Common commands:

```sh
./gradlew :common:test
./gradlew :verifyRepresentative
./gradlew :verifyCi
./gradlew buildReleaseArtifacts
```

Targeted examples:

```sh
./gradlew :1.21.1:build
./gradlew :26.1.2:build
./gradlew :fabric_1_21_1:build
./gradlew :fabric_26_1_2:build
```

On local machines whose default Java is lower than 25, run `26.x` targets with a Java 25 `JAVA_HOME`.

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home ./gradlew --no-daemon --no-configuration-cache --max-workers=1 -Dorg.gradle.jvmargs=-Xmx4g :fabric_26_1_2:build
```

Release jars collected by `buildReleaseArtifacts` are written to `build/release-artifacts/`.
