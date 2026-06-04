package com.river_quinn.enchantment_custom_table.world.inventory;

//? if >=1.21.9 {
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

import java.util.Objects;

public class MenuItemStackHandler extends ItemStacksResourceHandler {
    private final int slotCapacity;

    public MenuItemStackHandler(int size) {
        this(size, 0);
    }

    public MenuItemStackHandler(int size, int slotCapacity) {
        super(size);
        this.slotCapacity = slotCapacity;
    }

    public int getSlots() {
        return size();
    }

    public ItemStack getStackInSlot(int slot) {
        Objects.checkIndex(slot, size());
        return stacks.get(slot);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        set(slot, ItemResource.of(stack), stack.getCount());
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !isItemValid(slot, stack)) {
            return stack;
        }

        ItemStack current = getStackInSlot(slot);
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
                setStackInSlot(slot, stack.copyWithCount(inserted));
            } else {
                ItemStack updated = current.copy();
                updated.grow(inserted);
                setStackInSlot(slot, updated);
            }
        }

        return stack.getCount() == inserted ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack current = getStackInSlot(slot);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int extracted = Math.min(amount, current.getCount());
        ItemStack result = current.copyWithCount(extracted);
        if (!simulate) {
            ItemStack updated = current.copy();
            updated.shrink(extracted);
            setStackInSlot(slot, updated.isEmpty() ? ItemStack.EMPTY : updated);
        }
        return result;
    }

    public int getSlotLimit(int slot) {
        return getCapacityAsInt(slot, ItemResource.EMPTY);
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return !stack.isEmpty() && isValid(slot, ItemResource.of(stack));
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        int capacity = super.getCapacity(index, resource);
        return slotCapacity > 0 ? Math.min(capacity, slotCapacity) : capacity;
    }
}
//?}
