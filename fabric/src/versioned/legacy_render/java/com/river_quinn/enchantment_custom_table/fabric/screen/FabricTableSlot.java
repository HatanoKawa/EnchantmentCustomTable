package com.river_quinn.enchantment_custom_table.fabric.screen;

import com.river_quinn.enchantment_custom_table.fabric.inventory.FabricTableInventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

public class FabricTableSlot extends Slot {
    private final FabricEmptySlotIcon emptyIcon;

    public FabricTableSlot(FabricTableInventory inventory, int index, int x, int y, FabricEmptySlotIcon emptyIcon) {
        super(inventory, index, x, y);
        this.emptyIcon = emptyIcon;
    }

    @Override
    public ResourceLocation getNoItemIcon() {
        return switch (emptyIcon) {
            case BOOK -> ResourceLocation.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
            case EMERALD -> ResourceLocation.withDefaultNamespace("container/slot/emerald");
            case NONE -> null;
        };
    }
}
