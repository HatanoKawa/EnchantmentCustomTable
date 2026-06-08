package com.river_quinn.enchantment_custom_table.network.enchanting_custom_table;

import com.river_quinn.enchantment_custom_table.core.net.EnchantingTableIntent;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public record EnchantingCustomTableNetData(EnchantingTableIntent intent) implements CustomPacketPayload {
    private static final StreamCodec<ByteBuf, EnchantingTableIntent> INTENT_CODEC =
            ByteBufCodecs.stringUtf8(64).map(EnchantingTableIntent::fromNetworkId, EnchantingTableIntent::networkId);

    public EnchantingCustomTableNetData {
        intent = intent == null ? EnchantingTableIntent.UNKNOWN : intent;
    }

    public EnchantingCustomTableNetData(String operateType) {
        this(EnchantingTableIntent.fromNetworkId(operateType));
    }

    public static final CustomPacketPayload.Type<EnchantingCustomTableNetData> TYPE =
            //? if >=1.21.11 {
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID, "enchanting_custom"));
            //?} else {
            /*new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "enchanting_custom"));
            *///?}

    public static final StreamCodec<ByteBuf, EnchantingCustomTableNetData> STREAM_CODEC = StreamCodec.composite(
            INTENT_CODEC,
            EnchantingCustomTableNetData::intent,
            EnchantingCustomTableNetData::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
