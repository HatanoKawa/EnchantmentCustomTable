package com.river_quinn.enchantment_custom_table.fabric.network;

import com.river_quinn.enchantment_custom_table.core.config.TableConfigWireCodec;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FabricTableConfigPayload(String json) implements CustomPacketPayload {
    public static final Type<FabricTableConfigPayload> TYPE = FabricVersionedMinecraft.payloadType("table_config_v2");
    public static final StreamCodec<RegistryFriendlyByteBuf, FabricTableConfigPayload> CODEC = CustomPacketPayload.codec(
            (payload, buffer) -> buffer.writeUtf(payload.json(), TableConfigWireCodec.MAX_LENGTH),
            buffer -> new FabricTableConfigPayload(buffer.readUtf(TableConfigWireCodec.MAX_LENGTH)));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
