package com.river_quinn.enchantment_custom_table.init;

import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentCustomMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<EnchantmentCustomMenu>> ENCHANTMENT_CUSTOM =
            REGISTRY.register("enchantment_custom", () -> IMenuTypeExtension.create(EnchantmentCustomMenu::new));

    public static void register(IEventBus eventBus){
        REGISTRY.register(eventBus);
    }
}
