package com.river_quinn.enchantment_custom_table.fabric.util;

import com.river_quinn.enchantment_custom_table.fabric.EnchantmentCustomTableFabric;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Optional;

public final class FabricVersionedMinecraft {
    private FabricVersionedMinecraft() {
    }

    @FunctionalInterface
    public interface BlockPosMenuFactory<T extends AbstractContainerMenu> {
        T create(int id, Inventory inventory, BlockPos pos);
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

    public static <T extends AbstractContainerMenu> MenuType<T> blockPosMenuType(BlockPosMenuFactory<T> factory) {
        return new ExtendedScreenHandlerType<>(factory::create, BlockPos.STREAM_CODEC);
    }

    public static void openBlockPosMenu(ServerPlayer serverPlayer, Component title, BlockPos pos, BlockPosMenuFactory<?> factory) {
        serverPlayer.openMenu(new ExtendedScreenHandlerFactory<BlockPos>() {
            @Override
            public BlockPos getScreenOpeningData(ServerPlayer player) {
                return pos;
            }

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                return factory.create(id, inventory, pos);
            }
        });
    }

    public static void registerFunctionalBlockItems(ItemLike... items) {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            for (ItemLike item : items) {
                entries.accept(item);
            }
        });
    }

    public static <T extends CustomPacketPayload> void registerServerboundPlayPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        PayloadTypeRegistry.playC2S().register(type, codec);
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
