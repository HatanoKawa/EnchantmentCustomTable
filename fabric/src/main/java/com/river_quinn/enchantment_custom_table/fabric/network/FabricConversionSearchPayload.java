package com.river_quinn.enchantment_custom_table.fabric.network;

import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record FabricConversionSearchPayload(
        String query,
        String clientLanguage,
        List<String> matchedEnchantments
) {
    public static final ResourceLocation ID = FabricVersionedMinecraft.id("conversion_search");

    public FabricConversionSearchPayload {
        query = EnchantmentSearchRules.sanitizeSearchQuery(query);
        clientLanguage = EnchantmentSearchRules.sanitizeClientLanguage(clientLanguage);
        matchedEnchantments = matchedEnchantments == null
                ? List.of()
                : matchedEnchantments.stream()
                        .limit(EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS)
                        .toList();
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(query, EnchantmentSearchRules.MAX_SEARCH_QUERY_LENGTH);
        buffer.writeUtf(clientLanguage, EnchantmentSearchRules.MAX_CLIENT_LANGUAGE_LENGTH);
        buffer.writeVarInt(matchedEnchantments.size());
        for (String matchedEnchantment : matchedEnchantments) {
            buffer.writeUtf(matchedEnchantment, 256);
        }
    }

    public static FabricConversionSearchPayload read(FriendlyByteBuf buffer) {
        String query = buffer.readUtf(EnchantmentSearchRules.MAX_SEARCH_QUERY_LENGTH);
        String clientLanguage = buffer.readUtf(EnchantmentSearchRules.MAX_CLIENT_LANGUAGE_LENGTH);
        int matchedCount = Math.min(buffer.readVarInt(), EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS);
        List<String> matchedEnchantments = new ArrayList<>(matchedCount);
        for (int i = 0; i < matchedCount; i++) {
            matchedEnchantments.add(buffer.readUtf(256));
        }
        return new FabricConversionSearchPayload(query, clientLanguage, matchedEnchantments);
    }
}
