package com.river_quinn.enchantment_custom_table.fabric.init;

import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantingCustomMenu;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantmentConversionMenu;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public final class FabricModMenus {
    public static final MenuType<FabricEnchantingCustomMenu> ENCHANTING_CUSTOM =
            new ExtendedScreenHandlerType<>(FabricEnchantingCustomMenu::new, BlockPos.STREAM_CODEC);

    public static final MenuType<FabricEnchantmentConversionMenu> ENCHANTMENT_CONVERSION =
            new ExtendedScreenHandlerType<>(FabricEnchantmentConversionMenu::new, BlockPos.STREAM_CODEC);

    private FabricModMenus() {
    }

    public static void register() {
        FabricVersionedMinecraft.register(BuiltInRegistries.MENU, "enchanting_custom", ENCHANTING_CUSTOM);
        FabricVersionedMinecraft.register(BuiltInRegistries.MENU, "enchantment_conversion", ENCHANTMENT_CONVERSION);
    }
}
