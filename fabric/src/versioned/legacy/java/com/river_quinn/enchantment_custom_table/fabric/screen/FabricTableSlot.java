package com.river_quinn.enchantment_custom_table.fabric.screen;

import com.mojang.datafixers.util.Pair;
import com.river_quinn.enchantment_custom_table.fabric.inventory.FabricTableInventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

public class FabricTableSlot extends Slot {
    private final FabricEmptySlotIcon emptyIcon;

    public FabricTableSlot(FabricTableInventory inventory, int index, int x, int y, FabricEmptySlotIcon emptyIcon) {
        super(inventory, index, x, y);
        this.emptyIcon = emptyIcon;
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return switch (emptyIcon) {
            case BOOK -> Pair.of(
                    InventoryMenu.BLOCK_ATLAS,
                    ResourceLocation.tryParse("enchantment_custom_table:item/empty_slot_book")
            );
            case EMERALD -> Pair.of(
                    InventoryMenu.BLOCK_ATLAS,
                    ResourceLocation.tryParse("minecraft:item/empty_slot_emerald")
            );
            case NONE -> null;
        };
    }
}
