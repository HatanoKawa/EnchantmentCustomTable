package com.river_quinn.enchantment_custom_table.network.enchanting_custom_table;

import com.river_quinn.enchantment_custom_table.core.net.EnchantingTableIntent;
import net.minecraft.network.FriendlyByteBuf;

public record EnchantingCustomTableNetData(EnchantingTableIntent intent) {
    public EnchantingCustomTableNetData {
        intent = intent == null ? EnchantingTableIntent.UNKNOWN : intent;
    }

    public EnchantingCustomTableNetData(String operateType) {
        this(EnchantingTableIntent.fromNetworkId(operateType));
    }

    public static EnchantingCustomTableNetData decode(FriendlyByteBuf buffer) {
        return new EnchantingCustomTableNetData(buffer.readUtf(64));
    }

    public static void encode(EnchantingCustomTableNetData data, FriendlyByteBuf buffer) {
        buffer.writeUtf(data.intent().networkId(), 64);
    }
}
