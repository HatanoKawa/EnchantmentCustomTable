package com.river_quinn.enchantment_custom_table;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = EnchantmentCustomTable.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue MINIMUM_LAPIS_COST = BUILDER
            .comment("Minimum lapis cost when using enchantment conversion table, when set to 0, emerald will be banned on the table")
            .defineInRange("minimumEmeraldCost", 36, 0, 64);

    private static final ModConfigSpec.IntValue MINIMUM_LAPIS_BLOCK_COST = BUILDER
            .comment("Minimum lapis block cost when using enchantment conversion table, when set to 0, emerald block will be banned on the table")
            .defineInRange("minimumEmeraldBlockCost", 4, 0, 64);

    private static final ModConfigSpec.BooleanValue IGNORE_ENCHANTMENT_LEVEL_LIMIT = BUILDER
            .comment("Ignore enchantment level limit when adding enchantments with the custom enchanting table")
            .define("ignoreEnchantmentLevelLimit", true);

    private static final ModConfigSpec.BooleanValue INCREMENTAL_SAME_LEVEL_MERGE = BUILDER
            .comment("When enabled, duplicate enchantments can only be merged if the current level and the added book level are the same.")
            .comment("A successful duplicate merge increases the level by 1 instead of adding both levels directly. New enchantments are still added normally.")
            .define("incrementalSameLevelMerge", false);

//    private static final ModConfigSpec.BooleanValue ENABLE_XP_REQUIREMENT = BUILDER
//            .comment("Enable XP requirement when using enchanting custom table")
//            .define("enableXpRequirement", false);

    private static final ModConfigSpec.BooleanValue CONVERT_MAX_LEVEL_BOOK = BUILDER
            .comment("if set to true, enchantment conversion table will use emerald to exchange max level book")
            .comment("if set to false, enchantment conversion table will use emerald to exchange 1-level book")
            .define("convert_max_level_book", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int minimumEmeraldCost = 36;
    public static int minimumEmeraldBlockCost = 4;
    public static boolean ignoreEnchantmentLevelLimit = true;
    public static boolean incrementalSameLevelMerge = false;
    public static boolean enableXpRequirement;
    public static boolean convertMaxLevelBook = true;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        minimumEmeraldCost = MINIMUM_LAPIS_COST.get();
        minimumEmeraldBlockCost = MINIMUM_LAPIS_BLOCK_COST.get();
        ignoreEnchantmentLevelLimit = IGNORE_ENCHANTMENT_LEVEL_LIMIT.get();
        incrementalSameLevelMerge = INCREMENTAL_SAME_LEVEL_MERGE.get();
//        enableXpRequirement = ENABLE_XP_REQUIREMENT.get();
        convertMaxLevelBook = CONVERT_MAX_LEVEL_BOOK.get();
    }
}
