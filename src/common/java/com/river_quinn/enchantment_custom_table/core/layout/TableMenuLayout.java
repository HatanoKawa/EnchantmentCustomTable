package com.river_quinn.enchantment_custom_table.core.layout;

public final class TableMenuLayout {
    public static final int SLOT_SIZE = 18;

    private TableMenuLayout() {
    }

    public static final class Conversion {
        public static final int BOOK_SLOT_X = 8;
        public static final int BOOK_SLOT_Y = 23;
        public static final int PAYMENT_SLOT_X = 26;
        public static final int PAYMENT_SLOT_Y = 23;
        public static final int GENERATED_SLOT_START_X = 44;
        public static final int GENERATED_SLOT_START_Y = 23;
        public static final int TEMPLATE_SLOT_X = 8;
        public static final int TEMPLATE_SLOT_Y = 77;
        public static final int COPY_RESULT_SLOT_X = 26;
        public static final int COPY_RESULT_SLOT_Y = 77;
        public static final int PLAYER_INVENTORY_X = 8;
        public static final int PLAYER_INVENTORY_Y = 99;
        public static final int PAGE_LABEL_X = 25;
        public static final int PAGE_LABEL_Y = 60;
        public static final int PREVIOUS_PAGE_BUTTON_X = 7;
        public static final int NEXT_PAGE_BUTTON_X = 25;
        public static final int PAGE_BUTTON_Y = 40;
        public static final int PAGE_BUTTON_WIDTH = 18;
        public static final int PAGE_BUTTON_HEIGHT = 18;

        private Conversion() {
        }

        public static int generatedSlotX(int column) {
            return GENERATED_SLOT_START_X + column * SLOT_SIZE;
        }

        public static int generatedSlotY(int row) {
            return GENERATED_SLOT_START_Y + row * SLOT_SIZE;
        }
    }
}
