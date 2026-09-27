package com.river_quinn.enchantment_custom_table.core.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class MinecraftPaymentConfig {
    private MinecraftPaymentConfig() {}

    /** Invoke only after registries and default item components are ready. */
    public static TableConfigSnapshot resolve(TableConfigSnapshot raw, Consumer<String> warning) {
        Map<String, Integer> limits = new HashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            if (raw.paymentOptions().containsKey(id) || PaymentOptions.DEFAULTS.containsKey(id)) {
                limits.put(id, item.getDefaultInstance().getMaxStackSize());
            }
        }
        return raw.withPayments(PaymentOptions.resolve(raw.paymentOptions(), id -> limits.getOrDefault(id, 0), warning));
    }

    public static net.minecraft.network.chat.Component describe(TableConfigView config) {
        if (config.freeConversionTableCosts()) return net.minecraft.network.chat.Component.translatable("enchantment_custom_table.payment_config.free_hint");
        if (config.paymentOptions().isEmpty()) return net.minecraft.network.chat.Component.translatable("enchantment_custom_table.payment_config.unavailable");
        var text = net.minecraft.network.chat.Component.translatable("enchantment_custom_table.payment_config.payments_hint");
        Map<String, Item> items = new HashMap<>();
        for (Item item : BuiltInRegistries.ITEM) items.put(itemId(item), item);
        config.paymentOptions().forEach((id, cost) -> {
            text.append("\n" + cost + " × ");
            Item item = items.get(id);
            text.append(item == null ? net.minecraft.network.chat.Component.literal(id) : item.getDefaultInstance().getHoverName());
        });
        return text;
    }

    public static String itemId(Item item) { return BuiltInRegistries.ITEM.getKey(item).toString(); }
}
