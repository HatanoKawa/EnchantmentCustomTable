package com.river_quinn.enchantment_custom_table.world.inventory;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

final class MenuStackMover {
    private MenuStackMover() {
    }

    static boolean moveItemStackTo(List<Slot> slots, ItemStack stack, int startIndex, int endIndex, boolean reverse) {
        boolean moved = false;
        int index = reverse ? endIndex - 1 : startIndex;

        if (stack.isStackable()) {
            while (!stack.isEmpty() && inRange(index, startIndex, endIndex, reverse)) {
                Slot slot = slots.get(index);
                ItemStack existingStack = slot.getItem();
                if (slot.mayPlace(stack) && !existingStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, existingStack)) {
                    int combinedCount = existingStack.getCount() + stack.getCount();
                    int maxStackSize = slot.getMaxStackSize(existingStack);
                    if (combinedCount <= maxStackSize) {
                        stack.setCount(0);
                        existingStack.setCount(combinedCount);
                        slot.set(existingStack);
                        moved = true;
                    } else if (existingStack.getCount() < maxStackSize) {
                        stack.shrink(maxStackSize - existingStack.getCount());
                        existingStack.setCount(maxStackSize);
                        slot.set(existingStack);
                        moved = true;
                    }
                }
                index = nextIndex(index, reverse);
            }
        }

        if (!stack.isEmpty()) {
            index = reverse ? endIndex - 1 : startIndex;
            while (inRange(index, startIndex, endIndex, reverse)) {
                Slot slot = slots.get(index);
                ItemStack existingStack = slot.getItem();
                if (existingStack.isEmpty() && slot.mayPlace(stack)) {
                    int maxStackSize = slot.getMaxStackSize(stack);
                    slot.setByPlayer(stack.split(Math.min(stack.getCount(), maxStackSize)));
                    slot.setChanged();
                    moved = true;
                    break;
                }
                index = nextIndex(index, reverse);
            }
        }

        return moved;
    }

    private static boolean inRange(int index, int startIndex, int endIndex, boolean reverse) {
        return reverse ? index >= startIndex : index < endIndex;
    }

    private static int nextIndex(int index, boolean reverse) {
        return reverse ? index - 1 : index + 1;
    }
}
