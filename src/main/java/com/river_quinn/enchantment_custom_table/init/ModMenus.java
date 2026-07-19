package com.river_quinn.enchantment_custom_table.init;

import com.river_quinn.enchantment_custom_table.world.inventory.EnchantingCustomMenu;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);

    public static final RegistryObject<MenuType<EnchantingCustomMenu>> ENCHANTING_CUSTOM =
            MENUS.register("enchanting_custom", () -> IForgeMenuType.create(EnchantingCustomMenu::new));

    public static final RegistryObject<MenuType<EnchantmentConversionMenu>> ENCHANTMENT_CONVERSION =
            MENUS.register("enchantment_conversion", () -> IForgeMenuType.create(EnchantmentConversionMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
