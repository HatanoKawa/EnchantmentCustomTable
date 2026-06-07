package com.river_quinn.enchantment_custom_table.fabric.block.entity;

import com.river_quinn.enchantment_custom_table.fabric.inventory.FabricTableInventory;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModBlockEntities;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantingCustomMenu;
import com.river_quinn.enchantment_custom_table.fabric.transfer.FabricEnchantingAutomationStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class FabricEnchantingCustomTableBlockEntity extends FabricEnchantingTableLikeBlockEntity {
    private final FabricTableInventory inventory = new FabricTableInventory(
            FabricEnchantingCustomMenu.ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE,
            slot -> slot == FabricEnchantingCustomMenu.TOOL_SLOT ? 1 : 1,
            (slot, stack) -> switch (slot) {
                case FabricEnchantingCustomMenu.TOOL_SLOT -> true;
                case FabricEnchantingCustomMenu.INPUT_SLOT -> false;
                default -> false;
            },
            this::markInventoryChanged
    );
    private final Storage<ItemVariant> automationStorage = new FabricEnchantingAutomationStorage(this);
    private int inventoryVersion = 0;

    public FabricEnchantingCustomTableBlockEntity(BlockPos pos, BlockState state) {
        super(FabricModBlockEntities.ENCHANTING_CUSTOM_TABLE, pos, state);
    }

    public FabricTableInventory getInventory() {
        return inventory;
    }

    public int getInventoryVersion() {
        return inventoryVersion;
    }

    public Storage<ItemVariant> getAutomationStorage() {
        return automationStorage;
    }

    private void markInventoryChanged(int slot) {
        if (slot != FabricEnchantingCustomMenu.TOOL_SLOT) {
            return;
        }
        inventoryVersion++;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        NonNullList<ItemStack> persistentItems = NonNullList.withSize(1, ItemStack.EMPTY);
        persistentItems.set(0, inventory.getStackInSlot(FabricEnchantingCustomMenu.TOOL_SLOT));
        tag.put("Inventory", ContainerHelper.saveAllItems(new CompoundTag(), persistentItems, registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        NonNullList<ItemStack> persistentItems = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag.getCompound("Inventory"), persistentItems, registries);
        inventory.setStackInSlot(FabricEnchantingCustomMenu.TOOL_SLOT, persistentItems.get(0));
    }
}
