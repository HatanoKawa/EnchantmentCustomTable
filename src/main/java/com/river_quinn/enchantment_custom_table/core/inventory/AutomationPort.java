package com.river_quinn.enchantment_custom_table.core.inventory;

import net.minecraft.world.item.ItemStack;

public interface AutomationPort {
    int getSlots();

    SlotRole getRole(int slot);

    boolean canInsert(int slot, ItemStack stack);

    boolean canExtract(int slot);
}
