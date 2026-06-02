package com.river_quinn.enchantment_custom_table.core.rules;

import com.river_quinn.enchantment_custom_table.core.access.EnchantmentEntry;
import com.river_quinn.enchantment_custom_table.core.access.EnchantmentList;

import java.util.List;
import java.util.OptionalInt;

public final class EnchantmentMergeRules {
    private EnchantmentMergeRules() {
    }

    public record MergeOptions(boolean enforceLevelLimit, boolean incrementalSameLevelMerge) {
    }

    public record MergeResult(boolean allowed, EnchantmentList enchantments) {
    }

    public static MergeResult tryMerge(
            EnchantmentList baseEnchantments,
            List<EnchantmentEntry> additions,
            MergeOptions options
    ) {
        EnchantmentList result = baseEnchantments;

        for (EnchantmentEntry addition : additions) {
            int currentLevel = result.levelOf(addition.key());
            OptionalInt resultLevel = MergeRules.calculateMergedEnchantmentLevel(
                    currentLevel,
                    addition.level(),
                    addition.maxLevel(),
                    options.enforceLevelLimit(),
                    options.incrementalSameLevelMerge()
            );
            if (resultLevel.isEmpty()) {
                return new MergeResult(false, baseEnchantments);
            }

            result = result.withLevel(addition, resultLevel.getAsInt());
        }

        return new MergeResult(true, result);
    }

    public static EnchantmentList subtract(
            EnchantmentList baseEnchantments,
            List<EnchantmentEntry> removals,
            boolean incrementalSingleBookSplit
    ) {
        EnchantmentList result = baseEnchantments;

        for (EnchantmentEntry removal : removals) {
            OptionalInt resultLevel = MergeRules.calculateRemainingEnchantmentLevel(
                    result.levelOf(removal.key()),
                    removal.level(),
                    incrementalSingleBookSplit
            );
            if (resultLevel.isPresent()) {
                result = result.withLevel(removal, resultLevel.getAsInt());
            }
        }

        return result;
    }
}
