package com.river_quinn.enchantment_custom_table.fabric.util;

import com.river_quinn.enchantment_custom_table.fabric.EnchantmentCustomTableFabric;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.Level;

import java.util.Optional;

public final class FabricVersionedMinecraft {
    private FabricVersionedMinecraft() {
    }

    public static InteractionResult sidedSuccess(Level level) {
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(EnchantmentCustomTableFabric.MODID, path);
    }

    public static <V, T extends V> T register(Registry<V> registry, String path, T value) {
        return Registry.register(registry, id(path), value);
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String path) {
        return new CustomPacketPayload.Type<>(id(path));
    }

    public static BlockBehaviour.Properties blockProperties(String path) {
        return BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id(path)));
    }

    public static Item.Properties itemProperties(String path) {
        return new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id(path)));
    }

    public static Registry<Enchantment> enchantmentRegistry(Level level) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    }

    public static Optional<Holder<Enchantment>> resolveEnchantmentHolder(Level level, ResourceKey<Enchantment> key) {
        return enchantmentRegistry(level).get(key.location()).map(holder -> holder);
    }

    public static String keyId(ResourceKey<?> key) {
        return key.location().toString();
    }

    public static String keyNamespace(ResourceKey<?> key) {
        return key.location().getNamespace();
    }

    public static String keyPath(ResourceKey<?> key) {
        return key.location().getPath();
    }
}
