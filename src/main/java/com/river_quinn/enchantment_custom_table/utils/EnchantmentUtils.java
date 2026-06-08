package com.river_quinn.enchantment_custom_table.utils;

import com.river_quinn.enchantment_custom_table.core.access.EnchantmentKey;
import com.river_quinn.enchantment_custom_table.core.platform.EnchantmentAccessService;
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
    private static final EnchantmentAccessService SERVICE = new EnchantmentAccessService() {
        @Override
        public List<Holder<Enchantment>> allEnchantments(Level level) {
            List<Holder<Enchantment>> enchantments = new ArrayList<>();
            //? if >=1.21.2 {
            Registry<Enchantment> registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            //?} else {
            /*Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            *///?}
            registry.asHolderIdMap().forEach(enchantments::add);
            return enchantments;
        }

        @Override
        public Optional<Holder<Enchantment>> resolveEnchantmentHolder(Level level, Holder<Enchantment> enchantment) {
            return EnchantmentUtils.resolveEnchantmentHolder(level, enchantment);
        }

        @Override
        public ItemEnchantments getEnchantments(ItemStack itemStack) {
            return EnchantmentUtils.getEnchantments(itemStack);
        }

        @Override
        public EnchantmentKey getCoreEnchantmentKey(Level level, Holder<Enchantment> enchantment) {
            return EnchantmentUtils.getCoreEnchantmentKey(level, enchantment);
        }

        @Override
        public void setEnchantments(ItemStack itemStack, ItemEnchantments enchantments) {
            itemStack.set(EnchantmentHelper.getComponentType(itemStack), enchantments);
        }
    };

    public static EnchantmentAccessService service() {
        return SERVICE;
    }

    public static Holder.Reference<Enchantment> translateEnchantment(Level level, Enchantment enchantment) {
        if (level == null || enchantment == null)
            return null;
        //? if >=1.21.2 {
        Registry<Enchantment> fullEnchantmentRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Optional<ResourceKey<Enchantment>> resourceKey = fullEnchantmentRegistry.getResourceKey(enchantment);
        return resourceKey.flatMap(fullEnchantmentRegistry::get).orElse(null);
        //?} else {
        /*Registry<Enchantment> fullEnchantmentRegistry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        Optional<ResourceKey<Enchantment>> resourceKey = fullEnchantmentRegistry.getResourceKey(enchantment);
        return resourceKey.flatMap(fullEnchantmentRegistry::getHolder).orElse(null);
        *///?}
    }

    public static Optional<ResourceKey<Enchantment>> getEnchantmentKey(Level level, Holder<Enchantment> enchantment) {
        if (level == null || enchantment == null) {
            return Optional.empty();
        }

        Optional<ResourceKey<Enchantment>> holderKey = enchantment.unwrapKey();
        if (holderKey.isPresent()) {
            return holderKey;
        }

        //? if >=1.21.2 {
        Registry<Enchantment> registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        //?} else {
        /*Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        *///?}
        return registry.getResourceKey(enchantment.value());
    }

    public static Optional<Holder<Enchantment>> resolveEnchantmentHolder(Level level, Holder<Enchantment> enchantment) {
        if (level == null || enchantment == null) {
            return Optional.empty();
        }

        //? if >=1.21.2 {
        Registry<Enchantment> registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return getEnchantmentKey(level, enchantment)
                .flatMap(registry::get)
                .map(holder -> holder);
        //?} else {
        /*Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        return getEnchantmentKey(level, enchantment)
                .flatMap(registry::getHolder)
                .map(holder -> holder);
        *///?}
    }

    public static ItemEnchantments getEnchantments(ItemStack itemStack) {
        return itemStack.getOrDefault(EnchantmentHelper.getComponentType(itemStack), ItemEnchantments.EMPTY);
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

    public static boolean isSameEnchantment(Level level, Holder<Enchantment> first, Holder<Enchantment> second) {
        return getCoreEnchantmentKey(level, first).equals(getCoreEnchantmentKey(level, second));
    }

    public static ItemStack createEnchantedBook(Holder<Enchantment> enchantment, int level) {
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchantedBook.enchant(enchantment, level);
        return enchantedBook;
    }

    public static EnchantmentKey getCoreEnchantmentKey(Level level, Holder<Enchantment> enchantment) {
        Optional<ResourceKey<Enchantment>> key = getEnchantmentKey(level, enchantment);
        if (key.isPresent()) {
            return fromResourceKey(key.get());
        }
        return EnchantmentKey.of(
                "unregistered",
                enchantment.value().getClass().getName() + "_" + Integer.toHexString(System.identityHashCode(enchantment.value()))
        );
    }

    private static EnchantmentKey fromResourceKey(ResourceKey<Enchantment> key) {
        //? if >=1.21.11 {
        return EnchantmentKey.of(key.identifier().getNamespace(), key.identifier().getPath());
        //?} else {
        /*return EnchantmentKey.of(key.location().getNamespace(), key.location().getPath());
        *///?}
    }
}
