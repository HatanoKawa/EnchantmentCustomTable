package com.river_quinn.enchantment_custom_table.init;

import com.river_quinn.enchantment_custom_table.client.gui.EnchantingCustomScreen;
import com.river_quinn.enchantment_custom_table.client.gui.EnchantmentConversionScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ModScreens {
    private ModScreens() {
    }

    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.ENCHANTING_CUSTOM.get(), EnchantingCustomScreen::new);
            MenuScreens.register(ModMenus.ENCHANTMENT_CONVERSION.get(), EnchantmentConversionScreen::new);
        });
    }
}
