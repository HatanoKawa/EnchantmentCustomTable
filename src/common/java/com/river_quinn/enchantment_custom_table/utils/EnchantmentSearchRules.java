package com.river_quinn.enchantment_custom_table.utils;

import java.util.Collection;
import java.util.Locale;

public final class EnchantmentSearchRules {
    public static final int MAX_SEARCH_QUERY_LENGTH = 64;
    public static final int MAX_CLIENT_LANGUAGE_LENGTH = 32;
    public static final int MAX_MATCHED_ENCHANTMENT_IDS = 1024;

    private EnchantmentSearchRules() {
    }

    public static String sanitizeSearchQuery(String query) {
        if (query == null || query.isEmpty()) {
            return "";
        }
        return query.length() > MAX_SEARCH_QUERY_LENGTH ? query.substring(0, MAX_SEARCH_QUERY_LENGTH) : query;
    }

    public static String sanitizeClientLanguage(String clientLanguage) {
        if (clientLanguage == null || clientLanguage.isEmpty()) {
            return "";
        }
        return clientLanguage.length() > MAX_CLIENT_LANGUAGE_LENGTH
                ? clientLanguage.substring(0, MAX_CLIENT_LANGUAGE_LENGTH)
                : clientLanguage;
    }

    public static String normalizeSearchText(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value
                .toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replace('-', ' ')
                .strip()
                .replaceAll("\\s+", " ");
    }

    public static boolean isBlankSearch(String query) {
        return normalizeSearchText(query).isEmpty();
    }

    public static boolean matchesAnyCandidate(String query, Collection<String> candidates) {
        String normalizedQuery = normalizeSearchText(query);
        if (normalizedQuery.isEmpty()) {
            return true;
        }

        String compactQuery = compactSearchText(normalizedQuery);
        for (String candidate : candidates) {
            String normalizedCandidate = normalizeSearchText(candidate);
            if (normalizedCandidate.contains(normalizedQuery)
                    || compactSearchText(normalizedCandidate).contains(compactQuery)) {
                return true;
            }
        }
        return false;
    }

    private static String compactSearchText(String value) {
        return value.replace(" ", "");
    }
}
