package com.river_quinn.enchantment_custom_table.fabric.screen;

import com.river_quinn.enchantment_custom_table.fabric.inventory.FabricTableInventory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;

public class FabricTableSlot extends Slot {
    private final FabricEmptySlotIcon emptyIcon;

    public FabricTableSlot(FabricTableInventory inventory, int index, int x, int y, FabricEmptySlotIcon emptyIcon) {
        super(inventory, index, x, y);
        this.emptyIcon = emptyIcon;
    }

    @Override
    public Identifier getNoItemIcon() {
        return switch (emptyIcon) {
            case BOOK -> Identifier.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
            case EMERALD -> Identifier.withDefaultNamespace("container/slot/emerald");
            case NONE -> null;
        };
    }
}
