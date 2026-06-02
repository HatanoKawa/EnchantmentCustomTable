package com.river_quinn.enchantment_custom_table.core.session;

public record TableOperationResult(boolean success, boolean changed) {
    public static TableOperationResult success(boolean changed) {
        return new TableOperationResult(true, changed);
    }

    public static TableOperationResult failed(boolean changed) {
        return new TableOperationResult(false, changed);
    }
}
