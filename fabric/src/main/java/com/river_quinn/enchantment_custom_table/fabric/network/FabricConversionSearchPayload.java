package com.river_quinn.enchantment_custom_table.fabric.network;

import com.river_quinn.enchantment_custom_table.fabric.EnchantmentCustomTableFabric;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record FabricConversionSearchPayload(
        String query,
        String clientLanguage,
        List<ResourceLocation> matchedEnchantments
) implements CustomPacketPayload {
    public static final Type<FabricConversionSearchPayload> TYPE = new Type<>(EnchantmentCustomTableFabric.id("conversion_search"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FabricConversionSearchPayload> CODEC =
            CustomPacketPayload.codec(FabricConversionSearchPayload::write, FabricConversionSearchPayload::read);

    public FabricConversionSearchPayload {
        query = EnchantmentSearchRules.sanitizeSearchQuery(query);
        clientLanguage = EnchantmentSearchRules.sanitizeClientLanguage(clientLanguage);
        matchedEnchantments = matchedEnchantments == null
                ? List.of()
                : matchedEnchantments.stream()
                .limit(EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS)
                .toList();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(query, EnchantmentSearchRules.MAX_SEARCH_QUERY_LENGTH);
        buffer.writeUtf(clientLanguage, 64);
        buffer.writeVarInt(matchedEnchantments.size());
        for (ResourceLocation matchedEnchantment : matchedEnchantments) {
            buffer.writeResourceLocation(matchedEnchantment);
        }
    }

    private static FabricConversionSearchPayload read(RegistryFriendlyByteBuf buffer) {
        String query = buffer.readUtf(EnchantmentSearchRules.MAX_SEARCH_QUERY_LENGTH);
        String clientLanguage = buffer.readUtf(64);
        int matchedCount = Math.min(buffer.readVarInt(), EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS);
        List<ResourceLocation> matchedEnchantments = new ArrayList<>(matchedCount);
        for (int i = 0; i < matchedCount; i++) {
            matchedEnchantments.add(buffer.readResourceLocation());
        }
        return new FabricConversionSearchPayload(query, clientLanguage, matchedEnchantments);
    }
}
