package com.river_quinn.enchantment_custom_table.fabric.inventory;

import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiPredicate;
import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;

public class FabricTableInventory extends SimpleContainer implements LogicalInventory {
    private final IntUnaryOperator slotLimit;
    private final BiPredicate<Integer, ItemStack> validator;
    private final IntConsumer changed;

    public FabricTableInventory(int size, IntUnaryOperator slotLimit, BiPredicate<Integer, ItemStack> validator, Runnable changed) {
        this(size, slotLimit, validator, slot -> changed.run());
    }

    public FabricTableInventory(int size, IntUnaryOperator slotLimit, BiPredicate<Integer, ItemStack> validator, IntConsumer changed) {
        super(size);
        this.slotLimit = slotLimit;
        this.validator = validator;
        this.changed = changed;
    }

    @Override
    public int getSlots() {
        return getContainerSize();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return getItem(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        setItem(slot, stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot < 0 || slot >= getSlots() || stack.isEmpty() || !isItemValid(slot, stack)) {
            return stack;
        }

        ItemStack existing = getStackInSlot(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(existing, stack)) {
                return stack;
            }
            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return stack;
        }

        int moved = Math.min(limit, stack.getCount());
        if (!simulate) {
            if (existing.isEmpty()) {
                setStackInSlot(slot, stack.copyWithCount(moved));
            } else {
                existing.grow(moved);
                setStackInSlot(slot, existing);
            }
        }
        return stack.getCount() == moved ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - moved);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < 0 || slot >= getSlots() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack existing = getStackInSlot(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = existing.copyWithCount(Math.min(amount, existing.getCount()));
        if (!simulate) {
            existing.shrink(extracted.getCount());
            setStackInSlot(slot, existing);
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slotLimit.applyAsInt(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return validator.test(slot, stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isItemValid(slot, stack);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack limitedStack = stack;
        int limit = getSlotLimit(slot);
        if (!stack.isEmpty() && stack.getCount() > limit) {
            limitedStack = stack.copyWithCount(limit);
        }
        super.setItem(slot, limitedStack);
        changed.accept(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = super.removeItem(slot, amount);
        if (!removed.isEmpty()) {
            changed.accept(slot);
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = super.removeItemNoUpdate(slot);
        if (!removed.isEmpty()) {
            changed.accept(slot);
        }
        return removed;
    }

    @Override
    public void clearContent() {
        boolean[] changedSlots = new boolean[getContainerSize()];
        for (int slot = 0; slot < getContainerSize(); slot++) {
            changedSlots[slot] = !getItem(slot).isEmpty();
        }
        super.clearContent();
        for (int slot = 0; slot < changedSlots.length; slot++) {
            if (changedSlots[slot]) {
                changed.accept(slot);
            }
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }
}
