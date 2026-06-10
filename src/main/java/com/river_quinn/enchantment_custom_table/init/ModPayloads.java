package com.river_quinn.enchantment_custom_table.init;

import com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table.EnchantmentConversionTableNetData;
import com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table.EnchantmentConversionTableServerPayloadHandler;
import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableNetData;
import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableServerPayloadHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public final class ModPayloads {
    private static final String PROTOCOL_VERSION = "1";
    private static int nextPacketId = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private ModPayloads() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                nextPacketId++,
                EnchantingCustomTableNetData.class,
                EnchantingCustomTableNetData::encode,
                EnchantingCustomTableNetData::decode,
                ModPayloads::handleEnchantingCustomTable
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                EnchantmentConversionTableNetData.class,
                EnchantmentConversionTableNetData::encode,
                EnchantmentConversionTableNetData::decode,
                ModPayloads::handleEnchantmentConversionTable
        );
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    private static void handleEnchantingCustomTable(
            EnchantingCustomTableNetData data,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> EnchantingCustomTableServerPayloadHandler.handleDataOnMain(data, context.getSender()));
        context.setPacketHandled(true);
    }

    private static void handleEnchantmentConversionTable(
            EnchantmentConversionTableNetData data,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> EnchantmentConversionTableServerPayloadHandler.handleDataOnMain(data, context.getSender()));
        context.setPacketHandled(true);
    }
}
