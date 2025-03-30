package com.river_quinn.enchantment_custom_table.network.enchanting_custom_table;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public record EnchantingCustomTableNetData(int blockPosX, int blockPosY, int blockPosZ, String operateType) implements CustomPacketPayload {
    public enum OperateType {
        EXPORT_ALL_ENCHANTMENTS,
        NEXT_PAGE,
        PREVIOUS_PAGE
    }

    public static final CustomPacketPayload.Type<EnchantingCustomTableNetData> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "enchanting_custom"));

    public static final StreamCodec<ByteBuf, EnchantingCustomTableNetData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            EnchantingCustomTableNetData::blockPosX,
            ByteBufCodecs.INT,
            EnchantingCustomTableNetData::blockPosY,
            ByteBufCodecs.INT,
            EnchantingCustomTableNetData::blockPosZ,
            ByteBufCodecs.STRING_UTF8,
            EnchantingCustomTableNetData::operateType,
            EnchantingCustomTableNetData::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
