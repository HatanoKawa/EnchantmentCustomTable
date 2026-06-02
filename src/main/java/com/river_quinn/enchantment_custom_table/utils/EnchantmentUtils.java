package com.river_quinn.enchantment_custom_table.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
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

    public static List<EnchantmentTableRules.EnchantmentLevel> getEnchantmentLevels(Level level, ItemStack enchantedBookItemStack) {
        List<EnchantmentTableRules.EnchantmentLevel> enchantments = new ArrayList<>();
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : getEnchantments(enchantedBookItemStack).entrySet()) {
            Holder<Enchantment> enchantment = resolveEnchantmentHolder(level, entry.getKey()).orElse(entry.getKey());
            enchantments.add(new EnchantmentTableRules.EnchantmentLevel(enchantment, entry.getIntValue()));
        }
        return enchantments;
    }

    public static boolean isSameEnchantment(Level level, Holder<Enchantment> first, Holder<Enchantment> second) {
        Optional<ResourceKey<Enchantment>> firstKey = getEnchantmentKey(level, first);
        Optional<ResourceKey<Enchantment>> secondKey = getEnchantmentKey(level, second);
        if (firstKey.isPresent() && secondKey.isPresent()) {
            return firstKey.get().equals(secondKey.get());
        }
        return first.value().equals(second.value());
    }

    public static ItemStack createEnchantedBook(Holder<Enchantment> enchantment, int level) {
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchantedBook.enchant(enchantment, level);
        return enchantedBook;
    }
}
