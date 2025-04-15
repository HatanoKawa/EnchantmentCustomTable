package com.river_quinn.enchantment_custom_table.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
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
}
