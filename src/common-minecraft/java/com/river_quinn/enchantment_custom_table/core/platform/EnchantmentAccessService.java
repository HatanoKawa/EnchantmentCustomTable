package com.river_quinn.enchantment_custom_table.core.platform;

import com.river_quinn.enchantment_custom_table.core.access.EnchantmentKey;
import com.river_quinn.enchantment_custom_table.core.access.EnchantmentList;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface EnchantmentAccessService {
    List<Enchantment> allEnchantments(Level level);

    Optional<Enchantment> resolveEnchantment(Level level, EnchantmentKey key);

    EnchantmentList getEnchantments(Level level, ItemStack itemStack);

    EnchantmentKey getCoreEnchantmentKey(Level level, Enchantment enchantment);

    void setEnchantments(Level level, ItemStack itemStack, EnchantmentList enchantments);

    default List<EnchantmentTableRules.EnchantmentLevel> getEnchantmentLevels(Level level, ItemStack enchantedBookItemStack) {
        List<EnchantmentTableRules.EnchantmentLevel> enchantments = new ArrayList<>();
        for (var entry : getEnchantments(level, enchantedBookItemStack).entries()) {
            Optional<Enchantment> enchantment = resolveEnchantment(level, entry.key());
            if (enchantment.isEmpty()) {
                continue;
            }
            enchantments.add(new EnchantmentTableRules.EnchantmentLevel(
                    enchantment.get(),
                    entry.key(),
                    entry.level(),
                    enchantment.get().getMaxLevel()
            ));
        }
        return enchantments;
    }

    default ItemStack createEnchantedBook(Enchantment enchantment, int level) {
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        addEnchantmentToBook(enchantedBook, enchantment, level);
        return enchantedBook;
    }

    default void addEnchantmentToBook(ItemStack enchantedBook, Enchantment enchantment, int level) {
        EnchantedBookItem.addEnchantment(enchantedBook, new EnchantmentInstance(enchantment, level));
    }
}
