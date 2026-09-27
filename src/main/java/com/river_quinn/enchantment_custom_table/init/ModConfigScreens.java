package com.river_quinn.enchantment_custom_table.init;

import net.neoforged.fml.ModContainer;
import com.river_quinn.enchantment_custom_table.client.gui.TableConfigScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ModConfigScreens {
    public static void register(ModContainer modContainer) {
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (container, parent) -> new TableConfigScreen(parent)
        );
    }
}
