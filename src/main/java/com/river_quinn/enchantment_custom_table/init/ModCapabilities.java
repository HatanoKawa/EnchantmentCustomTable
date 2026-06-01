package com.river_quinn.enchantment_custom_table.init;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModCapabilities {
    public static void register(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ENCHANTING_CUSTOM_TABLE.get(),
                (blockEntity, direction) -> blockEntity.getAutomationItemHandler(direction)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ENCHANTMENT_CONVERSION_TABLE.get(),
                (blockEntity, direction) -> blockEntity.getAutomationItemHandler(direction)
        );
    }
}
