package com.river_quinn.enchantment_custom_table.fabric.client;

import com.river_quinn.enchantment_custom_table.fabric.client.gui.FabricEnchantingCustomScreen;
import com.river_quinn.enchantment_custom_table.fabric.client.gui.FabricEnchantmentConversionScreen;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class EnchantmentCustomTableFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(FabricModMenus.ENCHANTING_CUSTOM, FabricEnchantingCustomScreen::new);
        MenuScreens.register(FabricModMenus.ENCHANTMENT_CONVERSION, FabricEnchantmentConversionScreen::new);
    }
}
