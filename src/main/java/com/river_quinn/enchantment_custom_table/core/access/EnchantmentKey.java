package com.river_quinn.enchantment_custom_table.core.access;

import java.util.Objects;

public record EnchantmentKey(String namespace, String path) {
    public EnchantmentKey {
        namespace = requirePart(namespace, "namespace");
        path = requirePart(path, "path");
    }

    public static EnchantmentKey of(String namespace, String path) {
        return new EnchantmentKey(namespace, path);
    }

    public static EnchantmentKey parse(String id) {
        Objects.requireNonNull(id, "id");
        int separatorIndex = id.indexOf(':');
        if (separatorIndex < 0) {
            return new EnchantmentKey("minecraft", id);
        }
        return new EnchantmentKey(id.substring(0, separatorIndex), id.substring(separatorIndex + 1));
    }

    public String asString() {
        return namespace + ":" + path;
    }

    @Override
    public String toString() {
        return asString();
    }

    private static String requirePart(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Enchantment key " + name + " cannot be blank");
        }
        return value;
    }
}
