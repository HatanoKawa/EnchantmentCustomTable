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

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int minimumEmeraldCost;
    public static int minimumEmeraldBlockCost;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        minimumEmeraldCost = MINIMUM_LAPIS_COST.get();
        minimumEmeraldBlockCost = MINIMUM_LAPIS_BLOCK_COST.get();
    }
}
