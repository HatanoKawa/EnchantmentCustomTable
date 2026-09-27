package com.river_quinn.enchantment_custom_table.network;

import com.river_quinn.enchantment_custom_table.core.config.TableConfigWireCodec;
import net.minecraft.network.FriendlyByteBuf;

public record TableConfigPayload(String json) {
    public static void encode(TableConfigPayload value, FriendlyByteBuf buffer) { buffer.writeUtf(value.json(), TableConfigWireCodec.MAX_LENGTH); }
    public static TableConfigPayload decode(FriendlyByteBuf buffer) { return new TableConfigPayload(buffer.readUtf(TableConfigWireCodec.MAX_LENGTH)); }
}
