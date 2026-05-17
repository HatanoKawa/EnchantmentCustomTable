package com.river_quinn.enchantment_custom_table.world.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

import java.util.Objects;

final class MenuItemStackHandler extends ItemStacksResourceHandler {
    private final int slotCapacity;

    MenuItemStackHandler(int size) {
        this(size, 0);
    }

    MenuItemStackHandler(int size, int slotCapacity) {
        super(size);
        this.slotCapacity = slotCapacity;
    }

    ItemStack getStackInSlot(int slot) {
        Objects.checkIndex(slot, size());
        return stacks.get(slot);
    }

    void setStackInSlot(int slot, ItemStack stack) {
        set(slot, ItemResource.of(stack), stack.getCount());
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        int capacity = super.getCapacity(index, resource);
        return slotCapacity > 0 ? Math.min(capacity, slotCapacity) : capacity;
    }
}
