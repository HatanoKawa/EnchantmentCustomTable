package com.river_quinn.enchantment_custom_table.core.rules;

public final class GeneratedSlotExchangeRules {
    private GeneratedSlotExchangeRules() {
    }

    public static Decision decide(boolean slotOccupied, boolean mergesExistingEnchantment) {
        if (!slotOccupied) {
            return Decision.INSERT;
        }
        return mergesExistingEnchantment ? Decision.MERGE : Decision.SWAP;
    }

    public enum Decision {
        INSERT(false, true),
        MERGE(false, true),
        SWAP(true, false);

        private final boolean removesExistingBook;
        private final boolean consumesCarriedBook;

        Decision(boolean removesExistingBook, boolean consumesCarriedBook) {
            this.removesExistingBook = removesExistingBook;
            this.consumesCarriedBook = consumesCarriedBook;
        }

        public boolean removesExistingBook() {
            return removesExistingBook;
        }

        public boolean consumesCarriedBook() {
            return consumesCarriedBook;
        }
    }
}
