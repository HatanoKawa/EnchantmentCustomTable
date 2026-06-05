package com.river_quinn.enchantment_custom_table.gametest;

import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

final class GameTestAssertions {
    private GameTestAssertions() {
    }

    static void assertEnchantmentLevel(
            GameTestHelper helper,
            ItemStack stack,
            Holder<Enchantment> enchantment,
            int expectedLevel,
            String message
    ) {
        int actualLevel = EnchantmentUtils.getEnchantments(stack).getLevel(enchantment);
        assertTrue(helper, actualLevel == expectedLevel, message + " (expected " + expectedLevel + ", got " + actualLevel + ")");
    }

    static void assertEnchantmentIdLevel(
            GameTestHelper helper,
            ItemStack stack,
            String enchantmentId,
            int expectedLevel,
            String message
    ) {
        int actualLevel = 0;
        for (var entry : EnchantmentUtils.getEnchantments(stack).entrySet()) {
            var entryId = EnchantmentUtils.getCoreEnchantmentKey(helper.getLevel(), entry.getKey()).asString();
            if (enchantmentId.equals(entryId)) {
                actualLevel = entry.getIntValue();
                break;
            }
        }
        assertTrue(helper, actualLevel == expectedLevel, message + " (expected " + expectedLevel + ", got " + actualLevel + ")");
    }

    static void assertTrue(GameTestHelper helper, boolean condition, String message) {
        //? if >=1.21.5 {
        helper.assertTrue(condition, Component.literal(message));
        //?} else {
        /*helper.assertTrue(condition, message);
        *///?}
    }
}
