package com.river_quinn.enchantment_custom_table.fabric.screen;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;

final class MinecraftTestBootstrap {
    private MinecraftTestBootstrap() {
    }

    static HolderLookup.Provider createLookup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var registries = VanillaRegistries.createWorldLookup();
        // 26.x binds default item components after loading the dynamic registries.
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(registries).forEach(pending -> pending.apply());
        return registries;
    }
}
