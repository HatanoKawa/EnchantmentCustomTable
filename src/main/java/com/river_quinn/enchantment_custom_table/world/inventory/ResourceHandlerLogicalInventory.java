package com.river_quinn.enchantment_custom_table.world.inventory;

import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

class ResourceHandlerLogicalInventory implements LogicalInventory {
    private final MenuItemStackHandler handler;

    ResourceHandlerLogicalInventory(MenuItemStackHandler handler) {
        this.handler = handler;
    }

    @Override
    public int getSlots() {
        return handler.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return handler.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        handler.setStackInSlot(slot, stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !isItemValid(slot, stack)) {
            return stack;
        }

        ItemStack current = handler.getStackInSlot(slot);
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) {
            return stack;
        }

        int capacity = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        int inserted = Math.min(stack.getCount(), Math.max(0, capacity - current.getCount()));
        if (inserted <= 0) {
            return stack;
        }

        if (!simulate) {
            if (current.isEmpty()) {
                handler.setStackInSlot(slot, stack.copyWithCount(inserted));
            } else {
                ItemStack updated = current.copy();
                updated.grow(inserted);
                handler.setStackInSlot(slot, updated);
            }
        }

        return stack.getCount() == inserted ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack current = handler.getStackInSlot(slot);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int extracted = Math.min(amount, current.getCount());
        ItemStack result = current.copyWithCount(extracted);
        if (!simulate) {
            ItemStack updated = current.copy();
            updated.shrink(extracted);
            handler.setStackInSlot(slot, updated.isEmpty() ? ItemStack.EMPTY : updated);
        }
        return result;
    }

    @Override
    public int getSlotLimit(int slot) {
        return handler.getCapacityAsInt(slot, ItemResource.EMPTY);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !stack.isEmpty() && handler.isValid(slot, ItemResource.of(stack));
    }
}
