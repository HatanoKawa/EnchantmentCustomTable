package com.river_quinn.enchantment_custom_table.init;

import com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table.EnchantmentConversionTableClientPayloadHandler;
import com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table.EnchantmentConversionTableNetData;
import com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table.EnchantmentConversionTableServerPayloadHandler;
import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableClientPayloadHandler;
import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableServerPayloadHandler;
import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableNetData;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPayloads {
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                EnchantingCustomTableNetData.TYPE,
                EnchantingCustomTableNetData.STREAM_CODEC,
                new MainThreadPayloadHandler<>(
                        EnchantingCustomTableServerPayloadHandler::handleDataOnMain
                ),
                new MainThreadPayloadHandler<>(
                        EnchantingCustomTableClientPayloadHandler::handleDataOnMain
                )
        );

        registrar.playBidirectional(
                EnchantmentConversionTableNetData.TYPE,
                EnchantmentConversionTableNetData.STREAM_CODEC,
                new MainThreadPayloadHandler<>(
                        EnchantmentConversionTableServerPayloadHandler::handleDataOnMain
                ),
                new MainThreadPayloadHandler<>(
                        EnchantmentConversionTableServerPayloadHandler::handleDataOnMain
                )
        );
    }
}
