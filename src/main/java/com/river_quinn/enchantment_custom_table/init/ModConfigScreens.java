package com.river_quinn.enchantment_custom_table.init;

import com.river_quinn.enchantment_custom_table.client.gui.TableConfigScreen;
import net.minecraftforge.common.MinecraftForge;

public final class ModConfigScreens {
    private ModConfigScreens() {
    }

    public static void register() {
        MinecraftForge.registerConfigScreen(TableConfigScreen::new);
    }
}
