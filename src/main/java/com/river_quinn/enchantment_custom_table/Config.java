package com.river_quinn.enchantment_custom_table;

import com.river_quinn.enchantment_custom_table.core.config.TableConfigSnapshot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

//? if >=1.21.6 {
@EventBusSubscriber(modid = EnchantmentCustomTable.MODID)
//?} else {
/*@EventBusSubscriber(modid = EnchantmentCustomTable.MODID, bus = EventBusSubscriber.Bus.MOD)
*///?}
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue MINIMUM_EMERALD_COST = BUILDER
            .comment("Minimum emerald cost when using enchantment conversion table. When set to 0, emeralds are not accepted on the table.")
            .defineInRange("minimumEmeraldCost", 36, 0, 64);

    private static final ModConfigSpec.IntValue MINIMUM_EMERALD_BLOCK_COST = BUILDER
            .comment("Minimum emerald block cost when using enchantment conversion table. When set to 0, emerald blocks are not accepted on the table.")
            .defineInRange("minimumEmeraldBlockCost", 4, 0, 64);

    private static final ModConfigSpec.BooleanValue ENFORCE_ENCHANTMENT_LEVEL_LIMIT = BUILDER
            .comment("When enabled, duplicate enchantment merges cannot exceed each enchantment's vanilla max level.")
            .comment("Adding a new enchantment entry is still allowed even if another mod created a book above that level.")
            .define("enforceEnchantmentLevelLimit", false);

    private static final ModConfigSpec.BooleanValue INCREMENTAL_SAME_LEVEL_MERGE = BUILDER
            .comment("When enabled, duplicate enchantments can only be merged if the current level and the added book level are the same.")
            .comment("A successful duplicate merge increases the level by 1 instead of adding both levels directly. New enchantments are still added normally.")
            .define("incrementalSameLevelMerge", false);

    private static final ModConfigSpec.BooleanValue CONVERT_ONLY_LEVEL_ONE_BOOK = BUILDER
            .comment("When enabled, the enchantment conversion table exchanges only level-one enchanted books.")
            .define("convertOnlyLevelOneBook", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int minimumEmeraldCost = 36;
    public static int minimumEmeraldBlockCost = 4;
    public static boolean enforceEnchantmentLevelLimit = false;
    public static boolean incrementalSameLevelMerge = false;
    public static boolean convertOnlyLevelOneBook = false;

    public static TableConfigSnapshot snapshot() {
        return new TableConfigSnapshot(
                minimumEmeraldCost,
                minimumEmeraldBlockCost,
                enforceEnchantmentLevelLimit,
                incrementalSameLevelMerge,
                convertOnlyLevelOneBook
        );
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        minimumEmeraldCost = MINIMUM_EMERALD_COST.get();
        minimumEmeraldBlockCost = MINIMUM_EMERALD_BLOCK_COST.get();
        enforceEnchantmentLevelLimit = ENFORCE_ENCHANTMENT_LEVEL_LIMIT.get();
        incrementalSameLevelMerge = INCREMENTAL_SAME_LEVEL_MERGE.get();
        convertOnlyLevelOneBook = CONVERT_ONLY_LEVEL_ONE_BOOK.get();
    }
}
