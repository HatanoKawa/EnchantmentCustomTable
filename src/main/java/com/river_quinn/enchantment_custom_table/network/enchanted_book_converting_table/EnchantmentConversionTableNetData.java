package com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table;

import com.river_quinn.enchantment_custom_table.core.net.ConversionTableIntent;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public record EnchantmentConversionTableNetData(
        ConversionTableIntent intent,
        String searchQuery,
        String clientLanguage,
        List<ResourceLocation> matchedEnchantments
) implements CustomPacketPayload {
    public EnchantmentConversionTableNetData {
        intent = intent == null ? ConversionTableIntent.UNKNOWN : intent;
        searchQuery = EnchantmentSearchRules.sanitizeSearchQuery(searchQuery);
        clientLanguage = EnchantmentSearchRules.sanitizeClientLanguage(clientLanguage);
        matchedEnchantments = matchedEnchantments == null
                ? List.of()
                : matchedEnchantments.stream()
                        .filter(Objects::nonNull)
                        .limit(EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS)
                        .toList();
    }

    public EnchantmentConversionTableNetData(ConversionTableIntent intent) {
        this(intent, "", "", List.of());
    }

    public EnchantmentConversionTableNetData(String operateType) {
        this(ConversionTableIntent.fromNetworkId(operateType), "", "", List.of());
    }

    public EnchantmentConversionTableNetData(
            String operateType,
            String searchQuery,
            String clientLanguage,
            List<ResourceLocation> matchedEnchantments
    ) {
        this(ConversionTableIntent.fromNetworkId(operateType), searchQuery, clientLanguage, matchedEnchantments);
    }

    public static final Type<EnchantmentConversionTableNetData> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "enchantment_conversion"));

    private static final StreamCodec<ByteBuf, List<ResourceLocation>> MATCHED_ENCHANTMENTS_CODEC =
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list(EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS));
    private static final StreamCodec<ByteBuf, ConversionTableIntent> INTENT_CODEC =
            ByteBufCodecs.stringUtf8(64).map(ConversionTableIntent::fromNetworkId, ConversionTableIntent::networkId);

    public static final StreamCodec<ByteBuf, EnchantmentConversionTableNetData> STREAM_CODEC = StreamCodec.composite(
            INTENT_CODEC,
            EnchantmentConversionTableNetData::intent,
            ByteBufCodecs.stringUtf8(EnchantmentSearchRules.MAX_SEARCH_QUERY_LENGTH),
            EnchantmentConversionTableNetData::searchQuery,
            ByteBufCodecs.stringUtf8(EnchantmentSearchRules.MAX_CLIENT_LANGUAGE_LENGTH),
            EnchantmentConversionTableNetData::clientLanguage,
            MATCHED_ENCHANTMENTS_CODEC,
            EnchantmentConversionTableNetData::matchedEnchantments,
            EnchantmentConversionTableNetData::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
