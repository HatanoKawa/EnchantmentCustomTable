package com.river_quinn.enchantment_custom_table.init;

import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableClientPayloadHandler;
import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableServerPayloadHandler;
import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableNetData;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPayloads {
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                EnchantingCustomTableNetData.TYPE,
                EnchantingCustomTableNetData.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        EnchantingCustomTableClientPayloadHandler::handleDataOnMain,
                        EnchantingCustomTableServerPayloadHandler::handleDataOnMain
                )
        );
    }
}
