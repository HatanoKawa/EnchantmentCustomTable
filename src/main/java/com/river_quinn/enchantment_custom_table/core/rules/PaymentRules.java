package com.river_quinn.enchantment_custom_table.core.rules;

public final class PaymentRules {
    private PaymentRules() {
    }

    public enum PaymentKind {
        EMERALD,
        EMERALD_BLOCK,
        UNSUPPORTED
    }

    public static boolean hasEnoughPayment(int availableCount, int configuredCost) {
        return configuredCost > 0 && availableCount >= configuredCost;
    }

    public static int remainingPaymentCount(int availableCount, int configuredCost) {
        if (!hasEnoughPayment(availableCount, configuredCost)) {
            return availableCount;
        }
        return availableCount - configuredCost;
    }

    public static int paymentCostForKind(PaymentKind paymentKind, int emeraldCost, int emeraldBlockCost) {
        if (paymentKind == PaymentKind.EMERALD && emeraldCost > 0) {
            return emeraldCost;
        }
        if (paymentKind == PaymentKind.EMERALD_BLOCK && emeraldBlockCost > 0) {
            return emeraldBlockCost;
        }
        return 0;
    }
}
