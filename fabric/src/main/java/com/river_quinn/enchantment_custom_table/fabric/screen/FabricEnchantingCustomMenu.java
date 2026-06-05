package com.river_quinn.enchantment_custom_table.fabric.screen;

import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.fabric.config.FabricTableConfig;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModBlocks;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModMenus;
import com.river_quinn.enchantment_custom_table.fabric.inventory.FabricTableInventory;
import com.river_quinn.enchantment_custom_table.core.layout.TableMenuLayout;
import com.river_quinn.enchantment_custom_table.core.session.EnchantingTableSession;
import com.river_quinn.enchantment_custom_table.core.session.TableOperationResult;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricEnchantmentUtils;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

public class FabricEnchantingCustomMenu extends AbstractContainerMenu {
    public static final int ENCHANTED_BOOK_SLOT_ROW_COUNT = 4;
    public static final int ENCHANTED_BOOK_SLOT_COLUMN_COUNT = 6;
    public static final int ENCHANTED_BOOK_SLOT_SIZE = ENCHANTED_BOOK_SLOT_ROW_COUNT * ENCHANTED_BOOK_SLOT_COLUMN_COUNT;
    public static final int TOOL_SLOT = 0;
    public static final int INPUT_SLOT = 1;
    public static final int GENERATED_SLOT_START = 2;
    public static final int ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE = GENERATED_SLOT_START + ENCHANTED_BOOK_SLOT_SIZE;
    private static final int PLAYER_MAIN_INVENTORY_SIZE = 27;
    private static final int PLAYER_INVENTORY_START = ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + PLAYER_MAIN_INVENTORY_SIZE;
    private static final int PLAYER_INVENTORY_END = PLAYER_HOTBAR_START + 9;
    private static final int PREVIOUS_PAGE_BUTTON = 0;
    private static final int NEXT_PAGE_BUTTON = 1;
    private static final int EXPORT_BUTTON = 2;

    public final Level world;
    public final Player entity;
    public final int x;
    public final int y;
    public final int z;
    private final ContainerLevelAccess access;
    private final FabricTableInventory inventory;
    private final EnchantingTableSession session;
    private boolean suppressGeneratedSlotTakeRemoval;

    public FabricEnchantingCustomMenu(int id, Inventory inventory, BlockPos pos) {
        super(FabricModMenus.ENCHANTING_CUSTOM, id);
        this.entity = inventory.player;
        this.world = inventory.player.level();
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.access = ContainerLevelAccess.create(world, pos);
        FabricEnchantingCustomTableBlockEntity blockEntity = world.getBlockEntity(pos) instanceof FabricEnchantingCustomTableBlockEntity table
                ? table
                : null;
        this.inventory = blockEntity != null
                ? blockEntity.getInventory()
                : new FabricTableInventory(ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE, slot -> 1, (slot, stack) -> false, () -> {});
        this.session = new EnchantingTableSession(
                world,
                this.inventory,
                FabricEnchantmentUtils.service(),
                () -> EnchantmentTableRules.MergeOptions.from(FabricTableConfig.snapshot()),
                TOOL_SLOT,
                INPUT_SLOT,
                GENERATED_SLOT_START,
                ENCHANTED_BOOK_SLOT_SIZE
        );
        addPageDataSlots();
        addTableSlots();
        session.refreshGeneratedSlotsFromTool();
        addPlayerInventory(inventory, TableMenuLayout.Enchanting.PLAYER_INVENTORY_X, TableMenuLayout.Enchanting.PLAYER_INVENTORY_Y);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index >= GENERATED_SLOT_START && index < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE) {
            int enchantmentIndexInCache = session.cacheIndexForGeneratedSlot(index);
            ItemStack generatedBook = stack.copy();
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            suppressGeneratedSlotTakeRemoval = true;
            try {
                slot.onTake(player, generatedBook);
            } finally {
                suppressGeneratedSlotTakeRemoval = false;
            }
            EnchantingTableSession.GeneratedBookRemovalResult removalResult = removeGeneratedBook(generatedBook);
            if (removalResult.success()
                    && !removalResult.regenerated()
                    && enchantmentIndexInCache < session.generatedItemCount()) {
                session.setGeneratedItem(enchantmentIndexInCache, ItemStack.EMPTY);
                session.updateGeneratedSlots();
            }
            return original;
        }

        if (index < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(Items.ENCHANTED_BOOK) && session.canApplyEnchantedBook(stack)) {
            if (!moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, TOOL_SLOT, TOOL_SLOT + 1, false)) {
            if (index < PLAYER_HOTBAR_START) {
                if (!moveItemStackTo(stack, PLAYER_HOTBAR_START, PLAYER_INVENTORY_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, FabricModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        switch (id) {
            case PREVIOUS_PAGE_BUTTON -> session.previousPage();
            case NEXT_PAGE_BUTTON -> session.nextPage();
            case EXPORT_BUTTON -> {
                EnchantingTableSession.ExportEnchantmentsResult result = session.exportAllEnchantments();
                if (result.success() && !player.getInventory().add(result.exportedStack())) {
                    player.drop(result.exportedStack(), false);
                }
                if (result.playSound()) {
                    playUseSound();
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    public int currentPage() {
        return session.currentPage();
    }

    public int totalPage() {
        return session.totalPage();
    }

    private void addPageDataSlots() {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return session.currentPage();
            }

            @Override
            public void set(int value) {
                session.setCurrentPage(value);
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return session.totalPage();
            }

            @Override
            public void set(int value) {
                session.setTotalPage(value);
            }
        });
    }

    private void addTableSlots() {
        addSlot(new TableSlot(TOOL_SLOT, TableMenuLayout.Enchanting.TOOL_SLOT_X, TableMenuLayout.Enchanting.TOOL_SLOT_Y) {
            @Override
            public void setChanged() {
                super.setChanged();
                if (getItem().isEmpty()) {
                    session.clearAll();
                } else {
                    session.refreshGeneratedSlotsFromTool();
                }
            }
        });
        addSlot(new TableSlot(INPUT_SLOT, TableMenuLayout.Enchanting.INPUT_SLOT_X, TableMenuLayout.Enchanting.INPUT_SLOT_Y, FabricEmptySlotIcon.BOOK) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.ENCHANTED_BOOK) && session.canApplyEnchantedBook(stack);
            }

            @Override
            public void setChanged() {
                ItemStack stack = getItem();
                super.setChanged();
                if (!stack.isEmpty() && applyEnchantedBook(stack.copyWithCount(1))) {
                    inventory.setStackInSlot(INPUT_SLOT, ItemStack.EMPTY);
                }
            }
        });
        int generatedBookIndex = 0;
        for (int row = 0; row < ENCHANTED_BOOK_SLOT_ROW_COUNT; row++) {
            int yPos = TableMenuLayout.Enchanting.generatedSlotY(row);
            for (int column = 0; column < ENCHANTED_BOOK_SLOT_COLUMN_COUNT; column++) {
                int xPos = TableMenuLayout.Enchanting.generatedSlotX(column);
                addSlot(new TableSlot(GENERATED_SLOT_START + generatedBookIndex, xPos, yPos, FabricEmptySlotIcon.BOOK) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return stack.is(Items.ENCHANTED_BOOK) && session.canApplyEnchantedBook(stack);
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return !getItem().isEmpty();
                    }

                    @Override
                    public void onTake(Player player, ItemStack stack) {
                        super.onTake(player, stack);
                        if (!suppressGeneratedSlotTakeRemoval) {
                            removeGeneratedBook(stack);
                        }
                    }

                    @Override
                    public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
                        handleGeneratedSlotSetByPlayer(getContainerSlot(), newStack, oldStack);
                    }
                });
                generatedBookIndex++;
            }
        }
    }

    private void handleGeneratedSlotSetByPlayer(int slotIndex, ItemStack newStack, ItemStack oldStack) {
        if (newStack.isEmpty()) {
            inventory.setStackInSlot(slotIndex, ItemStack.EMPTY);
            return;
        }

        boolean hasDuplicateEnchantment = hasDuplicateEnchantment(newStack, oldStack);
        if (!oldStack.isEmpty() && !hasDuplicateEnchantment) {
            removeGeneratedBook(oldStack);
        }

        if (applyEnchantedBook(newStack.copyWithCount(1)) && hasDuplicateEnchantment) {
            entity.containerMenu.setCarried(ItemStack.EMPTY.copy());
        }
    }

    private boolean hasDuplicateEnchantment(ItemStack newStack, ItemStack oldStack) {
        if (newStack.isEmpty() || oldStack.isEmpty()) {
            return false;
        }
        List<EnchantmentTableRules.EnchantmentLevel> newEnchantments = FabricEnchantmentUtils.getEnchantmentLevels(world, newStack);
        List<EnchantmentTableRules.EnchantmentLevel> oldEnchantments = FabricEnchantmentUtils.getEnchantmentLevels(world, oldStack);
        if (oldEnchantments.isEmpty()) {
            return false;
        }
        return EnchantmentTableRules.containsMatchingEnchantment(newEnchantments, oldEnchantments.get(0).key());
    }

    private boolean applyEnchantedBook(ItemStack stack) {
        TableOperationResult result = session.applyEnchantedBook(stack);
        if (result.success() && result.changed()) {
            playUseSound();
        }
        return result.success();
    }

    private EnchantingTableSession.GeneratedBookRemovalResult removeGeneratedBook(ItemStack stack) {
        EnchantingTableSession.GeneratedBookRemovalResult result = session.removeGeneratedBook(stack);
        if (result.success()) {
            playUseSound();
        }
        return result;
    }

    private void playUseSound() {
        if (!world.isClientSide()) {
            world.playSound(null, new BlockPos(x, y, z), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private void addPlayerInventory(Inventory inventory, int xOffset, int yOffset) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, xOffset + column * TableMenuLayout.SLOT_SIZE, yOffset + row * TableMenuLayout.SLOT_SIZE));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, xOffset + column * TableMenuLayout.SLOT_SIZE, yOffset + 58));
        }
    }

    private class TableSlot extends FabricTableSlot {
        TableSlot(int index, int x, int y) {
            this(index, x, y, FabricEmptySlotIcon.NONE);
        }

        TableSlot(int index, int x, int y, FabricEmptySlotIcon emptyIcon) {
            super(inventory, index, x, y, emptyIcon);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return inventory.isItemValid(getContainerSlot(), stack);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return inventory.getSlotLimit(getContainerSlot());
        }
    }
}
