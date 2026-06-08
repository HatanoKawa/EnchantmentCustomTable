package com.river_quinn.enchantment_custom_table.world.inventory;

//? if <1.21.9 {
/*import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class ItemHandlerLogicalInventory implements LogicalInventory {
    private final IItemHandlerModifiable itemHandler;

    public ItemHandlerLogicalInventory(IItemHandlerModifiable itemHandler) {
        this.itemHandler = itemHandler;
    }

    @Override
    public int getSlots() {
        return itemHandler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return itemHandler.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return itemHandler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return itemHandler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return itemHandler.isItemValid(slot, stack);
    }
}
*///?}
