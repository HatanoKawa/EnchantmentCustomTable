package com.river_quinn.enchantment_custom_table;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = EnchantmentCustomTable.MODID)
public class Config
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final boolean DEPRECATED_IGNORE_ENCHANTMENT_LEVEL_LIMIT_DEFAULT = true;
    private static final boolean DEPRECATED_CONVERT_MAX_LEVEL_BOOK_DEFAULT = true;

    private static final ModConfigSpec.IntValue MINIMUM_LAPIS_COST = BUILDER
            .comment("Minimum lapis cost when using enchantment conversion table, when set to 0, emerald will be banned on the table")
            .defineInRange("minimumEmeraldCost", 36, 0, 64);

    private static final ModConfigSpec.IntValue MINIMUM_LAPIS_BLOCK_COST = BUILDER
            .comment("Minimum lapis block cost when using enchantment conversion table, when set to 0, emerald block will be banned on the table")
            .defineInRange("minimumEmeraldBlockCost", 4, 0, 64);

    private static final ModConfigSpec.BooleanValue ENFORCE_ENCHANTMENT_LEVEL_LIMIT = BUILDER
            .comment("When enabled, the custom enchanting table enforces each enchantment's vanilla max level.")
            .define("enforceEnchantmentLevelLimit", false);

    private static final ModConfigSpec.BooleanValue INCREMENTAL_SAME_LEVEL_MERGE = BUILDER
            .comment("When enabled, duplicate enchantments can only be merged if the current level and the added book level are the same.")
            .comment("A successful duplicate merge increases the level by 1 instead of adding both levels directly. New enchantments are still added normally.")
            .define("incrementalSameLevelMerge", false);

    private static final ModConfigSpec.BooleanValue CONVERT_ONLY_LEVEL_ONE_BOOK = BUILDER
            .comment("When enabled, the enchantment conversion table exchanges only level-one enchanted books.")
            .define("convertOnlyLevelOneBook", false);

    private static final ModConfigSpec.BooleanValue IGNORE_ENCHANTMENT_LEVEL_LIMIT = BUILDER
            .comment("DEPRECATED and ignored. Use enforceEnchantmentLevelLimit instead.")
            .comment("This option is inverted: ignoreEnchantmentLevelLimit=false should become enforceEnchantmentLevelLimit=true.")
            .define("ignoreEnchantmentLevelLimit", DEPRECATED_IGNORE_ENCHANTMENT_LEVEL_LIMIT_DEFAULT);

//    private static final ModConfigSpec.BooleanValue ENABLE_XP_REQUIREMENT = BUILDER
//            .comment("Enable XP requirement when using enchanting custom table")
//            .define("enableXpRequirement", false);

    private static final ModConfigSpec.BooleanValue CONVERT_MAX_LEVEL_BOOK = BUILDER
            .comment("DEPRECATED and ignored. Use convertOnlyLevelOneBook instead.")
            .comment("This option is inverted: convert_max_level_book=false should become convertOnlyLevelOneBook=true.")
            .define("convert_max_level_book", DEPRECATED_CONVERT_MAX_LEVEL_BOOK_DEFAULT);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int minimumEmeraldCost = 36;
    public static int minimumEmeraldBlockCost = 4;
    public static boolean enforceEnchantmentLevelLimit = false;
    public static boolean incrementalSameLevelMerge = false;
    public static boolean enableXpRequirement;
    public static boolean convertOnlyLevelOneBook = false;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        minimumEmeraldCost = MINIMUM_LAPIS_COST.get();
        minimumEmeraldBlockCost = MINIMUM_LAPIS_BLOCK_COST.get();
        enforceEnchantmentLevelLimit = ENFORCE_ENCHANTMENT_LEVEL_LIMIT.get();
        incrementalSameLevelMerge = INCREMENTAL_SAME_LEVEL_MERGE.get();
//        enableXpRequirement = ENABLE_XP_REQUIREMENT.get();
        convertOnlyLevelOneBook = CONVERT_ONLY_LEVEL_ONE_BOOK.get();

        warnIfDeprecatedBooleanChanged(
                "ignoreEnchantmentLevelLimit",
                IGNORE_ENCHANTMENT_LEVEL_LIMIT.get(),
                DEPRECATED_IGNORE_ENCHANTMENT_LEVEL_LIMIT_DEFAULT,
                "enforceEnchantmentLevelLimit",
                "ignoreEnchantmentLevelLimit=false should become enforceEnchantmentLevelLimit=true."
        );
        warnIfDeprecatedBooleanChanged(
                "convert_max_level_book",
                CONVERT_MAX_LEVEL_BOOK.get(),
                DEPRECATED_CONVERT_MAX_LEVEL_BOOK_DEFAULT,
                "convertOnlyLevelOneBook",
                "convert_max_level_book=false should become convertOnlyLevelOneBook=true."
        );
    }

    private static void warnIfDeprecatedBooleanChanged(
            String deprecatedName,
            boolean value,
            boolean defaultValue,
            String replacementName,
            String migrationHint
    ) {
        if (value != defaultValue) {
            LOGGER.warn(
                    "Config option '{}' is deprecated and ignored. Use '{}' instead. {}",
                    deprecatedName,
                    replacementName,
                    migrationHint
            );
        }
    }
}
