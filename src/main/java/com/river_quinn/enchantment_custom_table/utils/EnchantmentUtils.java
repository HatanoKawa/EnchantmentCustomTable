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
        if (level == null || enchantment == null)
            return null;
        Registry<Enchantment> fullEnchantmentRegistry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        Optional<ResourceKey<Enchantment>> resourceKey = fullEnchantmentRegistry.getResourceKey(enchantment);
        return resourceKey.flatMap(fullEnchantmentRegistry::getHolder).orElse(null);
    }

    public static Optional<ResourceKey<Enchantment>> getEnchantmentKey(Level level, Holder<Enchantment> enchantment) {
        if (level == null || enchantment == null) {
            return Optional.empty();
        }

        Optional<ResourceKey<Enchantment>> holderKey = enchantment.unwrapKey();
        if (holderKey.isPresent()) {
            return holderKey;
        }

        Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        return registry.getResourceKey(enchantment.value());
    }

    public static Optional<Holder<Enchantment>> resolveEnchantmentHolder(Level level, Holder<Enchantment> enchantment) {
        if (level == null || enchantment == null) {
            return Optional.empty();
        }

        Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        return getEnchantmentKey(level, enchantment)
                .flatMap(registry::getHolder)
                .map(holder -> holder);
    }

    public static ItemEnchantments getEnchantments(ItemStack itemStack) {
        return itemStack.getOrDefault(EnchantmentHelper.getComponentType(itemStack), ItemEnchantments.EMPTY);
    }

    public static int getEnchantCost(ItemStack toolItemStack) {
        if (!Config.enableXpRequirement)
            return 0;

        var xpLevelToCost = 0;
        var itemEnchantments = getEnchantments(toolItemStack);
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
