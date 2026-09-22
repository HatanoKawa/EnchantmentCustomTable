package com.river_quinn.enchantment_custom_table.fabric.screen;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;

final class MinecraftTestBootstrap {
    private MinecraftTestBootstrap() {
    }

    static HolderLookup.Provider createLookup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        return VanillaRegistries.createLookup();
    }
}
