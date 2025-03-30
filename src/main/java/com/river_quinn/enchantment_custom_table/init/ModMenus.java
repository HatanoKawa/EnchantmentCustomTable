package com.river_quinn.enchantment_custom_table.init;

import com.river_quinn.enchantment_custom_table.world.inventory.EnchantingCustomMenu;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<EnchantingCustomMenu>> ENCHANTING_CUSTOM =
            REGISTRY.register("enchanting_custom", () -> IMenuTypeExtension.create(EnchantingCustomMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<EnchantmentConversionMenu>> ENCHANTMENT_CONVERSION =
            REGISTRY.register("enchantment_conversion", () -> IMenuTypeExtension.create(EnchantmentConversionMenu::new));

    public static void register(IEventBus eventBus){
        REGISTRY.register(eventBus);
    }
}
