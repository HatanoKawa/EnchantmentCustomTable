package com.river_quinn.enchantment_custom_table.core.platform;

import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;

public final class LegacyEnchantmentStorage {
    private LegacyEnchantmentStorage() {
    }

    public static void replaceEnchantments(ItemStack itemStack, Map<Enchantment, Integer> enchantments) {
        if (itemStack.is(Items.ENCHANTED_BOOK)) {
            // In 1.20.1, EnchantmentHelper only adds stored enchantments and cannot lower existing levels.
            itemStack.removeTagKey(EnchantedBookItem.TAG_STORED_ENCHANTMENTS);
        }
        EnchantmentHelper.setEnchantments(enchantments, itemStack);
    }
}
