package com.river_quinn.enchantment_custom_table.fabric.util;

import com.river_quinn.enchantment_custom_table.core.access.EnchantmentEntry;
import com.river_quinn.enchantment_custom_table.core.access.EnchantmentKey;
import com.river_quinn.enchantment_custom_table.core.access.EnchantmentList;
import com.river_quinn.enchantment_custom_table.core.platform.EnchantmentAccessService;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class FabricEnchantmentUtils {
    private static final EnchantmentAccessService SERVICE = new EnchantmentAccessService() {
        @Override
        public List<Enchantment> allEnchantments(Level level) {
            return FabricEnchantmentUtils.allEnchantments();
        }

        @Override
        public Optional<Enchantment> resolveEnchantment(Level level, EnchantmentKey key) {
            return FabricEnchantmentUtils.resolveEnchantment(key);
        }

        @Override
        public EnchantmentList getEnchantments(Level level, ItemStack itemStack) {
            return FabricEnchantmentUtils.getEnchantments(itemStack);
        }

        @Override
        public EnchantmentKey getCoreEnchantmentKey(Level level, Enchantment enchantment) {
            return FabricEnchantmentUtils.getCoreEnchantmentKey(enchantment);
        }

        @Override
        public void setEnchantments(Level level, ItemStack itemStack, EnchantmentList enchantments) {
            FabricEnchantmentUtils.setEnchantments(itemStack, enchantments);
        }
    };

    private FabricEnchantmentUtils() {
    }

    public static EnchantmentAccessService service() {
        return SERVICE;
    }

    public static List<Enchantment> allEnchantments() {
        List<Enchantment> enchantments = new ArrayList<>();
        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            enchantments.add(enchantment);
        }
        return enchantments;
    }

    public static Optional<Enchantment> resolveEnchantment(EnchantmentKey key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BuiltInRegistries.ENCHANTMENT.get(new ResourceLocation(key.namespace(), key.path())));
    }

    public static EnchantmentList getEnchantments(ItemStack itemStack) {
        List<EnchantmentEntry> entries = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(itemStack).entrySet()) {
            entries.add(new EnchantmentEntry(
                    getCoreEnchantmentKey(entry.getKey()),
                    entry.getValue(),
                    entry.getKey().getMaxLevel()
            ));
        }
        return EnchantmentList.of(entries);
    }

    public static List<EnchantmentTableRules.EnchantmentLevel> getEnchantmentLevels(Level level, ItemStack enchantedBookItemStack) {
        List<EnchantmentTableRules.EnchantmentLevel> enchantments = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(enchantedBookItemStack).entrySet()) {
            Enchantment enchantment = entry.getKey();
            enchantments.add(new EnchantmentTableRules.EnchantmentLevel(
                    enchantment,
                    getCoreEnchantmentKey(enchantment),
                    entry.getValue(),
                    enchantment.getMaxLevel()
            ));
        }
        return enchantments;
    }

    public static void setEnchantments(ItemStack itemStack, EnchantmentList enchantments) {
        Map<Enchantment, Integer> minecraftEnchantments = new LinkedHashMap<>();
        for (EnchantmentEntry entry : enchantments.entries()) {
            resolveEnchantment(entry.key()).ifPresent(enchantment -> minecraftEnchantments.put(enchantment, entry.level()));
        }
        EnchantmentHelper.setEnchantments(minecraftEnchantments, itemStack);
    }

    public static EnchantmentKey getCoreEnchantmentKey(Enchantment enchantment) {
        ResourceLocation key = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        if (key != null) {
            return EnchantmentKey.of(key.getNamespace(), key.getPath());
        }
        return EnchantmentKey.of(
                "unregistered",
                enchantment.getClass().getName() + "_" + Integer.toHexString(System.identityHashCode(enchantment))
        );
    }
}
