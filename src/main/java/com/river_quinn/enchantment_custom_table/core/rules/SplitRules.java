package com.river_quinn.enchantment_custom_table.core.rules;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SplitRules {
    private SplitRules() {
    }

    public static List<Integer> splitSingleEnchantmentLevels(int sourceLevel, boolean incrementalSameLevelMerge) {
        if (sourceLevel <= 1) {
            return List.of();
        }

        if (incrementalSameLevelMerge) {
            return List.of(sourceLevel - 1, sourceLevel - 1);
        }

        List<Integer> levels = new ArrayList<>();
        Set<Integer> seenLevels = new LinkedHashSet<>();
        int remainingLevel = sourceLevel;

        while (remainingLevel > 1) {
            int levelToAdd = remainingLevel / 2;
            if (seenLevels.add(levelToAdd)) {
                levels.add(levelToAdd);
            }

            remainingLevel -= levelToAdd;
        }

        return List.copyOf(levels);
    }
}
