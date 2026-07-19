package com.river_quinn.enchantment_custom_table;

import com.river_quinn.enchantment_custom_table.core.config.JsonTableConfigCodec;
import com.river_quinn.enchantment_custom_table.core.config.TableConfigSnapshot;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = EnchantmentCustomTable.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final TableConfigSnapshot DEFAULTS = JsonTableConfigCodec.defaultSnapshot();
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue MINIMUM_EMERALD_COST = BUILDER
            .comment("Minimum emerald cost when using enchantment conversion table. When set to 0, emeralds are not accepted on the table.")
            .defineInRange("minimumEmeraldCost", DEFAULTS.minimumEmeraldCost(), 0, 64);

    private static final ForgeConfigSpec.IntValue MINIMUM_EMERALD_BLOCK_COST = BUILDER
            .comment("Minimum emerald block cost when using enchantment conversion table. When set to 0, emerald blocks are not accepted on the table.")
            .defineInRange("minimumEmeraldBlockCost", DEFAULTS.minimumEmeraldBlockCost(), 0, 64);

    private static final ForgeConfigSpec.BooleanValue ENFORCE_ENCHANTMENT_LEVEL_LIMIT = BUILDER
            .comment("When enabled, duplicate enchantment merges cannot exceed each enchantment's vanilla max level.")
            .comment("Adding a new enchantment entry is still allowed even if another mod created a book above that level.")
            .define("enforceEnchantmentLevelLimit", DEFAULTS.enforceEnchantmentLevelLimit());

    private static final ForgeConfigSpec.BooleanValue INCREMENTAL_SAME_LEVEL_MERGE = BUILDER
            .comment("When enabled, duplicate enchantments can only be merged if the current level and the added book level are the same.")
            .comment("A successful duplicate merge increases the level by 1 instead of adding both levels directly. New enchantments are still added normally.")
            .define("incrementalSameLevelMerge", DEFAULTS.incrementalSameLevelMerge());

    private static final ForgeConfigSpec.BooleanValue CONVERT_ONLY_LEVEL_ONE_BOOK = BUILDER
            .comment("When enabled, the enchantment conversion table exchanges only level-one enchanted books.")
            .define("convertOnlyLevelOneBook", DEFAULTS.convertOnlyLevelOneBook());

    private static final ForgeConfigSpec.BooleanValue FREE_CONVERSION_TABLE_COSTS = BUILDER
            .comment("When enabled, the enchantment conversion table does not require or consume normal books, emeralds, or emerald blocks.")
            .comment("The book and payment slots stop accepting input while this mode is enabled.")
            .define("freeConversionTableCosts", DEFAULTS.freeConversionTableCosts());

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int minimumEmeraldCost = DEFAULTS.minimumEmeraldCost();
    public static int minimumEmeraldBlockCost = DEFAULTS.minimumEmeraldBlockCost();
    public static boolean enforceEnchantmentLevelLimit = DEFAULTS.enforceEnchantmentLevelLimit();
    public static boolean incrementalSameLevelMerge = DEFAULTS.incrementalSameLevelMerge();
    public static boolean convertOnlyLevelOneBook = DEFAULTS.convertOnlyLevelOneBook();
    public static boolean freeConversionTableCosts = DEFAULTS.freeConversionTableCosts();

    public static TableConfigSnapshot snapshot() {
        return new TableConfigSnapshot(
                minimumEmeraldCost,
                minimumEmeraldBlockCost,
                enforceEnchantmentLevelLimit,
                incrementalSameLevelMerge,
                convertOnlyLevelOneBook,
                freeConversionTableCosts
        );
    }

    public static TableConfigSnapshot defaultSnapshot() {
        return DEFAULTS;
    }

    public static void save(TableConfigSnapshot snapshot) {
        MINIMUM_EMERALD_COST.set(snapshot.minimumEmeraldCost());
        MINIMUM_EMERALD_BLOCK_COST.set(snapshot.minimumEmeraldBlockCost());
        ENFORCE_ENCHANTMENT_LEVEL_LIMIT.set(snapshot.enforceEnchantmentLevelLimit());
        INCREMENTAL_SAME_LEVEL_MERGE.set(snapshot.incrementalSameLevelMerge());
        CONVERT_ONLY_LEVEL_ONE_BOOK.set(snapshot.convertOnlyLevelOneBook());
        FREE_CONVERSION_TABLE_COSTS.set(snapshot.freeConversionTableCosts());
        SPEC.save();
        syncValues();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        syncValues();
    }

    private static void syncValues()
    {
        minimumEmeraldCost = MINIMUM_EMERALD_COST.get();
        minimumEmeraldBlockCost = MINIMUM_EMERALD_BLOCK_COST.get();
        enforceEnchantmentLevelLimit = ENFORCE_ENCHANTMENT_LEVEL_LIMIT.get();
        incrementalSameLevelMerge = INCREMENTAL_SAME_LEVEL_MERGE.get();
        convertOnlyLevelOneBook = CONVERT_ONLY_LEVEL_ONE_BOOK.get();
        freeConversionTableCosts = FREE_CONVERSION_TABLE_COSTS.get();
    }
}
