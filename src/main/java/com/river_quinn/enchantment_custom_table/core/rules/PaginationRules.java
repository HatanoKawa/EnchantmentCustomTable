package com.river_quinn.enchantment_custom_table.core.rules;

public final class PaginationRules {
    private PaginationRules() {
    }

    public static int calculatePageCount(int entryCount, int pageSize, boolean keepEmptyPage) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }

        if (entryCount <= 0) {
            return keepEmptyPage ? 1 : 0;
        }

        return (entryCount + pageSize - 1) / pageSize;
    }

    public static int cacheIndexForGeneratedSlot(int slotIndex, int firstGeneratedSlotIndex, int currentPage, int pageSize) {
        return (slotIndex - firstGeneratedSlotIndex) + currentPage * pageSize;
    }
}
