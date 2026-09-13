package com.river_quinn.enchantment_custom_table.core.inventory;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Predicate;

public final class GeneratedBookInsertion {
    private GeneratedBookInsertion() {
    }

    public static ItemStack insert(Slot slot, ItemStack carried, int amount, Predicate<ItemStack> applyBook) {
        if (amount <= 0 || !carried.is(Items.ENCHANTED_BOOK) || !slot.mayPlace(carried)) {
            return carried;
        }
        ItemStack displayed = slot.getItem();
        if (!displayed.isEmpty() && !ItemStack.isSameItemSameTags(displayed, carried)) {
            return carried;
        }

        // The displayed book is a generated preview, not a stack to grow via Slot.safeInsert.
        ItemStack input = carried.copy();
        input.setCount(1);
        if (applyBook.test(input)) {
            carried.shrink(1);
        }
        return carried;
    }
}
