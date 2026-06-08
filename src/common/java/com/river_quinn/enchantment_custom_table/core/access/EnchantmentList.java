package com.river_quinn.enchantment_custom_table.core.access;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record EnchantmentList(List<EnchantmentEntry> entries) {
    public EnchantmentList {
        Objects.requireNonNull(entries, "entries");
        entries = List.copyOf(entries);
    }

    public static EnchantmentList empty() {
        return new EnchantmentList(List.of());
    }

    public static EnchantmentList of(List<EnchantmentEntry> entries) {
        return new EnchantmentList(entries);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    public Optional<EnchantmentEntry> find(EnchantmentKey key) {
        Objects.requireNonNull(key, "key");
        return entries.stream()
                .filter(entry -> entry.key().equals(key))
                .findFirst();
    }

    public int levelOf(EnchantmentKey key) {
        return find(key)
                .map(EnchantmentEntry::level)
                .orElse(0);
    }

    public EnchantmentList withLevel(EnchantmentEntry entry, int level) {
        Objects.requireNonNull(entry, "entry");

        List<EnchantmentEntry> updated = new ArrayList<>();
        boolean replaced = false;
        for (EnchantmentEntry current : entries) {
            if (current.key().equals(entry.key())) {
                replaced = true;
                if (level > 0) {
                    updated.add(new EnchantmentEntry(current.key(), level, entry.maxLevel()));
                }
            } else {
                updated.add(current);
            }
        }

        if (!replaced && level > 0) {
            updated.add(entry.withLevel(level));
        }
        return new EnchantmentList(updated);
    }
}
