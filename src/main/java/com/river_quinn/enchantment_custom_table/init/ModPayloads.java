package com.river_quinn.enchantment_custom_table.init;

import com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table.EnchantmentConversionTableClientPayloadHandler;
import com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table.EnchantmentConversionTableNetData;
import com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table.EnchantmentConversionTableServerPayloadHandler;
import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableClientPayloadHandler;
import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableServerPayloadHandler;
import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableNetData;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
//? if <1.21.7 {
/*import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
*///?}
//? if >=1.21.7 {
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
//?}
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPayloads {
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        //? if >=1.21.7 {
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
        //?} else {
        /*registrar.playBidirectional(
                EnchantingCustomTableNetData.TYPE,
                EnchantingCustomTableNetData.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        EnchantingCustomTableClientPayloadHandler::handleDataOnMain,
                        EnchantingCustomTableServerPayloadHandler::handleDataOnMain
                )
        );
        *///?}

        //? if >=1.21.7 {
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
        //?} else {
        /*registrar.playBidirectional(
                EnchantmentConversionTableNetData.TYPE,
                EnchantmentConversionTableNetData.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        EnchantmentConversionTableClientPayloadHandler::handleDataOnMain,
                        EnchantmentConversionTableServerPayloadHandler::handleDataOnMain
                )
        );
        *///?}
    }
}
