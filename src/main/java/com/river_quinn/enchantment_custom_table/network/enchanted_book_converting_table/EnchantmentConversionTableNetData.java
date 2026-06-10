package com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table;

import com.river_quinn.enchantment_custom_table.core.net.ConversionTableIntent;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record EnchantmentConversionTableNetData(
        ConversionTableIntent intent,
        String searchQuery,
        String clientLanguage,
        List<String> matchedEnchantments
) {
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
            List<String> matchedEnchantments
    ) {
        this(ConversionTableIntent.fromNetworkId(operateType), searchQuery, clientLanguage, matchedEnchantments);
    }

    public static EnchantmentConversionTableNetData decode(FriendlyByteBuf buffer) {
        ConversionTableIntent intent = ConversionTableIntent.fromNetworkId(buffer.readUtf(64));
        String searchQuery = buffer.readUtf(EnchantmentSearchRules.MAX_SEARCH_QUERY_LENGTH);
        String clientLanguage = buffer.readUtf(EnchantmentSearchRules.MAX_CLIENT_LANGUAGE_LENGTH);
        int count = buffer.readVarInt();
        List<String> matchedEnchantments = new ArrayList<>(Math.min(count, EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS));
        for (int i = 0; i < count; i++) {
            String matchedEnchantment = buffer.readUtf(256);
            if (matchedEnchantments.size() < EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS) {
                matchedEnchantments.add(matchedEnchantment);
            }
        }
        return new EnchantmentConversionTableNetData(intent, searchQuery, clientLanguage, matchedEnchantments);
    }

    public static void encode(EnchantmentConversionTableNetData data, FriendlyByteBuf buffer) {
        buffer.writeUtf(data.intent().networkId(), 64);
        buffer.writeUtf(data.searchQuery(), EnchantmentSearchRules.MAX_SEARCH_QUERY_LENGTH);
        buffer.writeUtf(data.clientLanguage(), EnchantmentSearchRules.MAX_CLIENT_LANGUAGE_LENGTH);
        buffer.writeVarInt(data.matchedEnchantments().size());
        for (String matchedEnchantment : data.matchedEnchantments()) {
            buffer.writeUtf(matchedEnchantment, 256);
        }
    }
}
