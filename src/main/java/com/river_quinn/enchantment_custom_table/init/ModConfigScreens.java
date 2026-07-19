package com.river_quinn.enchantment_custom_table.init;

import com.river_quinn.enchantment_custom_table.client.gui.TableConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public final class ModConfigScreens {
    private ModConfigScreens() {
    }

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new TableConfigScreen(parent))
        );
    }
}
