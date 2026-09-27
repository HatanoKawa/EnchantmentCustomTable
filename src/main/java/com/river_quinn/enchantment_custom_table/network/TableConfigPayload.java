package com.river_quinn.enchantment_custom_table.network;

import com.river_quinn.enchantment_custom_table.EnchantmentCustomTable;
import com.river_quinn.enchantment_custom_table.core.config.TableConfigWireCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TableConfigPayload(String json) implements CustomPacketPayload {
    public static final Type<TableConfigPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(EnchantmentCustomTable.MODID, "table_config_v2"));
    public static final StreamCodec<ByteBuf, TableConfigPayload> CODEC = ByteBufCodecs.stringUtf8(TableConfigWireCodec.MAX_LENGTH)
            .map(TableConfigPayload::new, TableConfigPayload::json);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
