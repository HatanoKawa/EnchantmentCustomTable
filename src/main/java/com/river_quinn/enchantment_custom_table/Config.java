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
            .comment("Minimum lapis cost when using enchantment conversion table")
            .defineInRange("minimumLapisCost", 36, 0, 64);

    private static final ModConfigSpec.IntValue MINIMUM_LAPIS_BLOCK_COST = BUILDER
            .comment("Minimum lapis block cost when using enchantment conversion table")
            .defineInRange("minimumLapisBlockCost", 4, 0, 64);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int minimumLapisCost;
    public static int minimumLapisBlockCost;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        minimumLapisCost = MINIMUM_LAPIS_COST.get();
        minimumLapisBlockCost = MINIMUM_LAPIS_BLOCK_COST.get();
    }
}
