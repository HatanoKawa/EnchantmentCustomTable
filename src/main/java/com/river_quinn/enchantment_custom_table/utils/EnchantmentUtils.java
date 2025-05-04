package com.river_quinn.enchantment_custom_table.utils;

import com.river_quinn.enchantment_custom_table.Config;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class EnchantmentUtils {
    public static Holder.Reference<Enchantment> translateEnchantment(Level level, Enchantment enchantment) {
        if (level == null)
            return null;
        Registry<Enchantment> fullEnchantmentRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ResourceKey<Enchantment> resourceKey = fullEnchantmentRegistry.getResourceKey(enchantment).get();
        // 一些通过猜谜获得的逻辑，我不知道为什么要这么做，但是这么做能行
        Optional<Holder.Reference<Enchantment>> optional = level
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(resourceKey);
        return optional.get();
    }

    public static int getEnchantCost(ItemStack toolItemStack) {
        if (!Config.enableXpRequirement)
            return 0;

        var xpLevelToCost = 0;
        var itemEnchantments = toolItemStack.get(EnchantmentHelper.getComponentType(toolItemStack));
        for (var entry : itemEnchantments.entrySet()) {
            var enchantment = entry.getKey();
            var level = entry.getValue();

            xpLevelToCost += enchantment.value().getAnvilCost() * level;
        }
        return xpLevelToCost;
    }

    public static boolean checkSatisfyXpRequirement(ItemStack toolItemStack, Player player) {
        if (!Config.enableXpRequirement)
            return true;

        var xpLevelToCost = getEnchantCost(toolItemStack);
        return xpLevelToCost <= player.experienceLevel;
    }
}
