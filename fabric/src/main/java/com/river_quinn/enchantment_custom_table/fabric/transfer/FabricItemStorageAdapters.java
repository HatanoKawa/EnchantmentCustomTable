package com.river_quinn.enchantment_custom_table.fabric.transfer;

import com.river_quinn.enchantment_custom_table.fabric.init.FabricModBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

public final class FabricItemStorageAdapters {
    private FabricItemStorageAdapters() {
    }

    public static void register() {
        ItemStorage.SIDED.registerForBlockEntity(
                (blockEntity, direction) -> blockEntity.getAutomationStorage(),
                FabricModBlockEntities.ENCHANTING_CUSTOM_TABLE
        );
        ItemStorage.SIDED.registerForBlockEntity(
                (blockEntity, direction) -> blockEntity.getAutomationStorage(),
                FabricModBlockEntities.ENCHANTMENT_CONVERSION_TABLE
        );
    }
}
