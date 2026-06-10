package com.river_quinn.enchantment_custom_table.fabric.screen;

import com.mojang.datafixers.util.Pair;
import com.river_quinn.enchantment_custom_table.fabric.inventory.FabricTableInventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
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
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return switch (emptyIcon.get()) {
            case BOOK -> Pair.of(
                    InventoryMenu.BLOCK_ATLAS,
                    ResourceLocation.tryParse("enchantment_custom_table:item/empty_slot_book")
            );
            case EMERALD -> Pair.of(
                    InventoryMenu.BLOCK_ATLAS,
                    ResourceLocation.tryParse("minecraft:item/empty_slot_emerald")
            );
            case COPY_TEMPLATE -> Pair.of(
                    InventoryMenu.BLOCK_ATLAS,
                    ResourceLocation.tryParse("enchantment_custom_table:item/copy_template_slot")
            );
            case OUTPUT -> Pair.of(
                    InventoryMenu.BLOCK_ATLAS,
                    ResourceLocation.tryParse("enchantment_custom_table:item/output_slot")
            );
            case DISABLED -> Pair.of(
                    InventoryMenu.BLOCK_ATLAS,
                    ResourceLocation.tryParse("enchantment_custom_table:item/empty_slot_book_disabled")
            );
            case NONE -> null;
        };
    }
}
