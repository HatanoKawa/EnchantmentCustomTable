package com.river_quinn.enchantment_custom_table.init;

import com.river_quinn.enchantment_custom_table.client.gui.EnchantingCustomScreen;
import com.river_quinn.enchantment_custom_table.client.gui.EnchantmentConversionScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@OnlyIn(Dist.CLIENT)
public class ModScreens {
    @SubscribeEvent
    public static void register(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ENCHANTING_CUSTOM.get(), EnchantingCustomScreen::new);
        event.register(ModMenus.ENCHANTMENT_CONVERSION.get(), EnchantmentConversionScreen::new);
    }

}
