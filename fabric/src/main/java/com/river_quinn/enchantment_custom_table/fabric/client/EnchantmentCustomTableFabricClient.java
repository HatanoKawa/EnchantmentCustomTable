package com.river_quinn.enchantment_custom_table.fabric.client;

import com.river_quinn.enchantment_custom_table.fabric.client.gui.FabricEnchantingCustomScreen;
import com.river_quinn.enchantment_custom_table.fabric.client.gui.FabricEnchantmentConversionScreen;
import com.river_quinn.enchantment_custom_table.fabric.client.renderer.FabricEnchantingCustomTableRenderer;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModBlockEntities;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

public class EnchantmentCustomTableFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(FabricModMenus.ENCHANTING_CUSTOM, FabricEnchantingCustomScreen::new);
        MenuScreens.register(FabricModMenus.ENCHANTMENT_CONVERSION, FabricEnchantmentConversionScreen::new);
        BlockEntityRendererRegistry.register(FabricModBlockEntities.ENCHANTING_CUSTOM_TABLE, FabricEnchantingCustomTableRenderer::enchantingCustom);
        BlockEntityRendererRegistry.register(FabricModBlockEntities.ENCHANTMENT_CONVERSION_TABLE, FabricEnchantingCustomTableRenderer::enchantmentConversion);
    }
}
