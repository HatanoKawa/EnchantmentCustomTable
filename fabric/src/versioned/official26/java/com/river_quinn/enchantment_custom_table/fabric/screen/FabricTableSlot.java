package com.river_quinn.enchantment_custom_table.fabric.screen;

import com.river_quinn.enchantment_custom_table.fabric.inventory.FabricTableInventory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;

import java.util.function.Supplier;

public class FabricTableSlot extends Slot {
    private final Supplier<FabricEmptySlotIcon> emptyIcon;

    public FabricTableSlot(FabricTableInventory inventory, int index, int x, int y, FabricEmptySlotIcon emptyIcon) {
        this(inventory, index, x, y, () -> emptyIcon);
    }

    public FabricTableSlot(FabricTableInventory inventory, int index, int x, int y, Supplier<FabricEmptySlotIcon> emptyIcon) {
        super(inventory, index, x, y);
        this.emptyIcon = emptyIcon;
    }

    @Override
    public Identifier getNoItemIcon() {
        return switch (emptyIcon.get()) {
            case BOOK -> Identifier.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
            case EMERALD -> Identifier.withDefaultNamespace("container/slot/emerald");
            case COPY_TEMPLATE -> Identifier.fromNamespaceAndPath("enchantment_custom_table", "container/slot/copy_template_slot");
            case OUTPUT -> Identifier.fromNamespaceAndPath("enchantment_custom_table", "container/slot/output_slot");
            case DISABLED -> Identifier.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book_disabled");
            case NONE -> null;
        };
    }
}
