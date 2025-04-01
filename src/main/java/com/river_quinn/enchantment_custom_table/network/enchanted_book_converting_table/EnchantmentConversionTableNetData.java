package com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public record EnchantmentConversionTableNetData(String operateType) implements CustomPacketPayload {
    public enum OperateType {
        NEXT_PAGE,
        PREVIOUS_PAGE
    }

    public static final Type<EnchantmentConversionTableNetData> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "enchantment_conversion"));

    public static final StreamCodec<ByteBuf, EnchantmentConversionTableNetData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            EnchantmentConversionTableNetData::operateType,
            EnchantmentConversionTableNetData::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
