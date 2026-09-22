package com.river_quinn.enchantment_custom_table.fabric.screen;

import com.river_quinn.enchantment_custom_table.fabric.util.FabricEnchantmentUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

final class MinecraftBookTestData {
    static final Holder<Enchantment> LOOTING;
    static final Holder<Enchantment> SHARPNESS;

    static {
        var enchantments = MinecraftTestBootstrap.createLookup().lookupOrThrow(Registries.ENCHANTMENT);
        LOOTING = enchantments.getOrThrow(Enchantments.LOOTING);
        SHARPNESS = enchantments.getOrThrow(Enchantments.SHARPNESS);
    }

    private MinecraftBookTestData() {
    }

    static ItemStack book(int level) {
        return FabricEnchantmentUtils.createEnchantedBook(LOOTING, level);
    }

    static int level(ItemStack stack) {
        return FabricEnchantmentUtils.getEnchantments(stack).getLevel(LOOTING);
    }
}
