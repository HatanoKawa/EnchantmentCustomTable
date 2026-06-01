package com.river_quinn.enchantment_custom_table.core.rules;

import java.util.OptionalInt;

public final class MergeRules {
    private MergeRules() {
    }

    public static OptionalInt calculateMergedEnchantmentLevel(
            int currentLevel,
            int addedLevel,
            int maxLevel,
            boolean enforceLevelLimit,
            boolean incrementalSameLevelMerge
    ) {
        if (addedLevel <= 0) {
            return OptionalInt.empty();
        }

        boolean mergingDuplicate = currentLevel > 0;
        int resultLevel;
        if (!mergingDuplicate) {
            resultLevel = addedLevel;
        } else if (incrementalSameLevelMerge) {
            if (currentLevel != addedLevel) {
                return OptionalInt.empty();
            }
            resultLevel = currentLevel + 1;
        } else {
            resultLevel = currentLevel + addedLevel;
        }

        if (enforceLevelLimit && mergingDuplicate && resultLevel > maxLevel) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(resultLevel);
    }

    public static OptionalInt calculateRemainingEnchantmentLevel(
            int currentLevel,
            int removedLevel,
            boolean incrementalSingleBookSplit
    ) {
        if (currentLevel <= 0 || removedLevel <= 0) {
            return OptionalInt.empty();
        }

        if (incrementalSingleBookSplit) {
            return OptionalInt.of(currentLevel - 1);
        }

        return OptionalInt.of(currentLevel - removedLevel);
    }
}
