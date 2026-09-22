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
        if (!displayed.isEmpty() && !ItemStack.isSameItemSameComponents(displayed, carried)) {
            return carried;
        }

        // A generated preview is not a stored stack to grow via vanilla safeInsert.
        // Return the consumed input to vanilla instead of changing the menu's carried stack here.
        if (applyBook.test(carried.copyWithCount(1))) {
            carried.shrink(1);
        }
        return carried;
    }
}
