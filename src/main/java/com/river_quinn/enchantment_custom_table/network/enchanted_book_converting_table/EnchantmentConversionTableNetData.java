package com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table;

import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public record EnchantmentConversionTableNetData(
        String operateType,
        String searchQuery,
        String clientLanguage,
        List<Identifier> matchedEnchantments
) implements CustomPacketPayload {
    public enum OperateType {
        NEXT_PAGE,
        PREVIOUS_PAGE,
        SEARCH
    }

    public EnchantmentConversionTableNetData {
        operateType = operateType == null ? "" : operateType;
        searchQuery = EnchantmentSearchRules.sanitizeSearchQuery(searchQuery);
        clientLanguage = EnchantmentSearchRules.sanitizeClientLanguage(clientLanguage);
        matchedEnchantments = matchedEnchantments == null
                ? List.of()
                : matchedEnchantments.stream()
                        .filter(Objects::nonNull)
                        .limit(EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS)
                        .toList();
    }

    public EnchantmentConversionTableNetData(String operateType) {
        this(operateType, "", "", List.of());
    }

    public static final Type<EnchantmentConversionTableNetData> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MODID, "enchantment_conversion"));

    private static final StreamCodec<ByteBuf, List<Identifier>> MATCHED_ENCHANTMENTS_CODEC =
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list(EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS));

    public static final StreamCodec<ByteBuf, EnchantmentConversionTableNetData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32),
            EnchantmentConversionTableNetData::operateType,
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
