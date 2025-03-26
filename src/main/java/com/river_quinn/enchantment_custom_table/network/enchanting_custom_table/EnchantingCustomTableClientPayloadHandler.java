package com.river_quinn.enchantment_custom_table.network.enchanting_custom_table;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EnchantingCustomTableClientPayloadHandler {

    public static void handleDataOnMain(final EnchantingCustomTableNetData data, final IPayloadContext context) {
        // Do something with the data, on the main thread
//        blah(data.age());
    }
}
