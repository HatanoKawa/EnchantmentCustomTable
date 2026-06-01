package com.river_quinn.enchantment_custom_table.core.rules;

public final class CopyRules {
    private CopyRules() {
    }

    public static boolean isValidSingleEnchantmentTemplate(int enchantmentCount, int level, int maxLevel) {
        return enchantmentCount == 1 && level > 0 && level <= maxLevel;
    }

    public static boolean shouldGenerateCopyResult(boolean copyMode, boolean resultSlotEmpty, boolean hasEnoughMaterials) {
        return copyMode && resultSlotEmpty && hasEnoughMaterials;
    }
}
