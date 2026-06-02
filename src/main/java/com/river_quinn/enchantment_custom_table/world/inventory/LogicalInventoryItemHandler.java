package com.river_quinn.enchantment_custom_table.world.inventory;

import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class LogicalInventoryItemHandler implements IItemHandlerModifiable {
    private final LogicalInventory inventory;

    public LogicalInventoryItemHandler(LogicalInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public int getSlots() {
        return inventory.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        inventory.setStackInSlot(slot, stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return inventory.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return inventory.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return inventory.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return inventory.isItemValid(slot, stack);
    }
}
