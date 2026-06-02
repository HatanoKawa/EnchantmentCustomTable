package com.river_quinn.enchantment_custom_table.fabric.util;

import com.river_quinn.enchantment_custom_table.core.access.EnchantmentKey;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
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

public final class FabricEnchantmentUtils {
    private FabricEnchantmentUtils() {
    }

    public static Optional<ResourceKey<Enchantment>> getEnchantmentKey(Level level, Holder<Enchantment> enchantment) {
        if (level == null || enchantment == null) {
            return Optional.empty();
        }
        Optional<ResourceKey<Enchantment>> holderKey = enchantment.unwrapKey();
        if (holderKey.isPresent()) {
            return holderKey;
        }
        var registry = FabricVersionedMinecraft.enchantmentRegistry(level);
        return registry.getResourceKey(enchantment.value());
    }

    public static Optional<Holder<Enchantment>> resolveEnchantmentHolder(Level level, Holder<Enchantment> enchantment) {
        if (level == null || enchantment == null) {
            return Optional.empty();
        }
        return getEnchantmentKey(level, enchantment)
                .flatMap(key -> FabricVersionedMinecraft.resolveEnchantmentHolder(level, key))
                .map(holder -> holder);
    }

    public static ItemEnchantments getEnchantments(ItemStack itemStack) {
        return EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
    }

    public static List<EnchantmentTableRules.EnchantmentLevel> getEnchantmentLevels(Level level, ItemStack enchantedBookItemStack) {
        List<EnchantmentTableRules.EnchantmentLevel> enchantments = new ArrayList<>();
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : getEnchantments(enchantedBookItemStack).entrySet()) {
            Holder<Enchantment> enchantment = resolveEnchantmentHolder(level, entry.getKey()).orElse(entry.getKey());
            enchantments.add(new EnchantmentTableRules.EnchantmentLevel(
                    enchantment,
                    getCoreEnchantmentKey(level, enchantment),
                    entry.getIntValue(),
                    enchantment.value().getMaxLevel()
            ));
        }
        return enchantments;
    }

    public static ItemStack createEnchantedBook(Holder<Enchantment> enchantment, int level) {
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchantedBook.enchant(enchantment, level);
        return enchantedBook;
    }

    public static EnchantmentKey getCoreEnchantmentKey(Level level, Holder<Enchantment> enchantment) {
        Optional<ResourceKey<Enchantment>> key = getEnchantmentKey(level, enchantment);
        if (key.isPresent()) {
            return EnchantmentKey.of(
                    FabricVersionedMinecraft.keyNamespace(key.get()),
                    FabricVersionedMinecraft.keyPath(key.get())
            );
        }
        return EnchantmentKey.of(
                "unregistered",
                enchantment.value().getClass().getName() + "_" + Integer.toHexString(System.identityHashCode(enchantment.value()))
        );
    }
}
