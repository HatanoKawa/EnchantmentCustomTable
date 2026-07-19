package com.river_quinn.enchantment_custom_table.world.inventory;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import com.river_quinn.enchantment_custom_table.core.layout.TableMenuLayout;
import com.river_quinn.enchantment_custom_table.core.net.EnchantingTableActions;
import com.river_quinn.enchantment_custom_table.core.session.EnchantingTableSession;
import com.river_quinn.enchantment_custom_table.core.session.GeneratedSlotPage;
import com.river_quinn.enchantment_custom_table.init.ModBlocks;
import com.river_quinn.enchantment_custom_table.init.ModMenus;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

public class EnchantingCustomMenu extends AbstractContainerMenu implements EnchantingTableActions {
    public static final int ENCHANTED_BOOK_SLOT_ROW_COUNT = 4;
    public static final int ENCHANTED_BOOK_SLOT_COLUMN_COUNT = 6;
    public static final int ENCHANTED_BOOK_SLOT_SIZE = ENCHANTED_BOOK_SLOT_ROW_COUNT * ENCHANTED_BOOK_SLOT_COLUMN_COUNT;
    public static final int ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE = ENCHANTED_BOOK_SLOT_SIZE + 2;
    private static final int PLAYER_MAIN_INVENTORY_SIZE = 27;
    private static final int PLAYER_INVENTORY_START = ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + PLAYER_MAIN_INVENTORY_SIZE;
    private static final Pair<ResourceLocation, ResourceLocation> EMPTY_BOOK_SLOT_ICON = Pair.of(
            InventoryMenu.BLOCK_ATLAS,
            new ResourceLocation("enchantment_custom_table", "item/empty_slot_book")
    );

    private final LogicalInventory logicalInventory;
    private final LogicalInventoryItemHandler itemHandler;
    private final EnchantingTableSession session;

    private static final Logger LOGGER = LogUtils.getLogger();
    public final Level world;
    public final Player entity;
    public int x, y, z;
    private ContainerLevelAccess access = ContainerLevelAccess.NULL;
    private boolean hasValidPosition = false;
    public EnchantingCustomTableBlockEntity boundBlockEntity = null;
    private int lastInventoryVersion = -1;
    private boolean suppressGeneratedSlotTakeRemoval;
    private boolean suppressGeneratedSlotSetHandling;

    public EnchantingCustomMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModMenus.ENCHANTING_CUSTOM.get(), id);
        this.entity = inv.player;
        this.world = inv.player.level;

        BlockPos pos = null;
        if (extraData != null) {
            pos = extraData.readBlockPos();
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            access = ContainerLevelAccess.create(world, pos);
            hasValidPosition = true;
        }
        if (pos != null && this.world.getBlockEntity(pos) instanceof EnchantingCustomTableBlockEntity blockEntity) {
            boundBlockEntity = blockEntity;
        }

        this.logicalInventory = new EnchantingMenuInventory(boundBlockEntity);
        this.itemHandler = new LogicalInventoryItemHandler(logicalInventory);
        this.session = new EnchantingTableSession(
                world,
                logicalInventory,
                EnchantmentUtils.service(),
                this::mergeOptions,
                0,
                1,
                2,
                ENCHANTED_BOOK_SLOT_SIZE
        );

        addPageDataSlots();
        addTableSlots();
        addPlayerSlots(inv);

        initMenu();
        if (boundBlockEntity != null) {
            lastInventoryVersion = boundBlockEntity.getInventoryVersion();
        }
    }

    private void addPageDataSlots() {
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return session.currentPage();
            }

            @Override
            public void set(int value) {
                session.setCurrentPage(value);
                syncPageState();
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return session.totalPage();
            }

            @Override
            public void set(int value) {
                session.setTotalPage(value);
                syncPageState();
            }
        });
    }

    private void addTableSlots() {
        this.addSlot(new SlotItemHandler(itemHandler, 0, TableMenuLayout.Enchanting.TOOL_SLOT_X, TableMenuLayout.Enchanting.TOOL_SLOT_Y) {
            @Override
            public void onQuickCraft(ItemStack newStack, ItemStack oldStack) {
                super.onQuickCraft(newStack, oldStack);
                clearCache();
                clearPage();
            }

            @Override
            public void set(ItemStack stack) {
                ItemStack oldStack = getItem().copy();
                super.set(stack);
                if (!ItemStack.matches(stack, oldStack)) {
                    if (!stack.isEmpty()) {
                        genEnchantedBookCache();
                        session.setCurrentPage(0);
                        syncPageState();
                        updateEnchantedBookSlots();
                    } else {
                        clearCache();
                        clearPage();
                    }
                    acknowledgeBoundInventoryVersion();
                }
            }
        });

        this.addSlot(new SlotItemHandler(itemHandler, 1, TableMenuLayout.Enchanting.INPUT_SLOT_X, TableMenuLayout.Enchanting.INPUT_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.ENCHANTED_BOOK)
                        && !itemHandler.getStackInSlot(0).isEmpty()
                        && checkCanPlaceEnchantedBook(stack);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return EMPTY_BOOK_SLOT_ICON;
            }

            @Override
            public void set(ItemStack stack) {
                ItemStack oldStack = getItem().copy();
                super.set(stack);
                if (!stack.isEmpty()) {
                    addEnchantment(stack, 1, true);
                    getItem();
                } else if (!oldStack.isEmpty()) {
                    LOGGER.warn("stack 1, set() called with empty replacement");
                }
            }
        });

        int enchantedBookIndex = 0;
        for (int row = 0; row < ENCHANTED_BOOK_SLOT_ROW_COUNT; row++) {
            int yPos = TableMenuLayout.Enchanting.generatedSlotY(row);
            for (int col = 0; col < ENCHANTED_BOOK_SLOT_COLUMN_COUNT; col++) {
                int xPos = TableMenuLayout.Enchanting.generatedSlotX(col);
                int finalIndex = enchantedBookIndex;
                this.addSlot(new SlotItemHandler(itemHandler, finalIndex + 2, xPos, yPos) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return stack.is(Items.ENCHANTED_BOOK)
                                && !itemHandler.getStackInSlot(0).isEmpty()
                                && checkCanPlaceEnchantedBook(stack);
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return !getItem().isEmpty();
                    }

                    @Override
                    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                        return EMPTY_BOOK_SLOT_ICON;
                    }

                    @Override
                    public void onTake(Player player, ItemStack stack) {
                        super.onTake(player, stack);
                        if (!world.isClientSide && !suppressGeneratedSlotTakeRemoval) {
                            removeGeneratedBookFromGeneratedSlot(stack, getContainerSlot());
                        }
                    }

                    @Override
                    public ItemStack remove(int amount) {
                        if (!world.isClientSide) {
                            session.captureCurrentPageSlots();
                        }
                        return super.remove(amount);
                    }

                    @Override
                    public void set(ItemStack newStack) {
                        if (suppressGeneratedSlotSetHandling) {
                            super.set(newStack);
                            return;
                        }
                        handleGeneratedSlotSetByPlayer(getContainerSlot(), newStack, getItem().copy());
                    }
                });
                enchantedBookIndex++;
            }
        }
    }

    private void handleGeneratedSlotSetByPlayer(int slotIndex, ItemStack newStack, ItemStack oldStack) {
        if (world.isClientSide) {
            itemHandler.setStackInSlot(slotIndex, newStack);
            return;
        }

        if (newStack.isEmpty()) {
            if (!oldStack.isEmpty() && removeGeneratedBookFromGeneratedSlot(oldStack, slotIndex).success()) {
                return;
            }
            itemHandler.setStackInSlot(slotIndex, ItemStack.EMPTY);
            return;
        }

        session.captureCurrentPageSlots();
        boolean hasDuplicateEnchantment = hasDuplicateEnchantment(newStack, oldStack);
        if (!oldStack.isEmpty() && !hasDuplicateEnchantment) {
            EnchantingTableSession.GeneratedBookRemovalResult removalResult = removeGeneratedBookFromGeneratedSlot(oldStack, slotIndex);
            if (!removalResult.success()) {
                return;
            }
        }

        if (addEnchantment(copyWithCount(newStack, 1), slotIndex)) {
            entity.containerMenu.setCarried(ItemStack.EMPTY);
        }
    }

    private boolean hasDuplicateEnchantment(ItemStack newStack, ItemStack oldStack) {
        if (newStack.isEmpty() || oldStack.isEmpty()) {
            return false;
        }
        List<EnchantmentTableRules.EnchantmentLevel> newEnchantments = EnchantmentUtils.getEnchantmentLevels(world, newStack);
        List<EnchantmentTableRules.EnchantmentLevel> oldEnchantments = EnchantmentUtils.getEnchantmentLevels(world, oldStack);
        if (oldEnchantments.isEmpty()) {
            return false;
        }
        return EnchantmentTableRules.containsMatchingEnchantment(newEnchantments, oldEnchantments.get(0).key());
    }

    private void addPlayerSlots(Inventory inv) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + (row + 1) * 9,
                        TableMenuLayout.Enchanting.PLAYER_INVENTORY_X + col * TableMenuLayout.SLOT_SIZE,
                        TableMenuLayout.Enchanting.PLAYER_INVENTORY_Y + row * TableMenuLayout.SLOT_SIZE));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col,
                    TableMenuLayout.Enchanting.PLAYER_INVENTORY_X + col * TableMenuLayout.SLOT_SIZE,
                    TableMenuLayout.Enchanting.PLAYER_INVENTORY_Y + 58));
        }
    }

    @Override
    public void broadcastChanges() {
        if (boundBlockEntity != null && lastInventoryVersion != boundBlockEntity.getInventoryVersion()) {
            refreshGeneratedSlotsFromTool();
            acknowledgeBoundInventoryVersion();
        }
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return hasValidPosition
                && AbstractContainerMenu.stillValid(access, player, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (isGeneratedSlotIndex(index)) {
            session.captureCurrentPageSlots();
            ItemStack generatedBook = stack.copy();
            int cacheIndex = session.cacheIndexForGeneratedSlot(index);
            if (!session.isGeneratedItemAt(cacheIndex, generatedBook)) {
                return ItemStack.EMPTY;
            }
            if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            suppressGeneratedSlotSetHandling = true;
            try {
                if (stack.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
            } finally {
                suppressGeneratedSlotSetHandling = false;
            }
            suppressGeneratedSlotTakeRemoval = true;
            try {
                slot.onTake(playerIn, generatedBook);
            } finally {
                suppressGeneratedSlotTakeRemoval = false;
            }
            removeGeneratedBook(generatedBook, cacheIndex);
            return original;
        }

        if (index < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE) {
            if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (!this.moveItemStackTo(stack, 0, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE, false)) {
            if (index < PLAYER_HOTBAR_START) {
                if (!this.moveItemStackTo(stack, PLAYER_HOTBAR_START, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
                return ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(playerIn, stack);
        return original;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverse) {
        return MenuStackMover.moveItemStackTo(this.slots, stack, startIndex, endIndex, reverse);
    }

    @Override
    public void removed(@NotNull Player playerIn) {
        super.removed(playerIn);
        if (playerIn instanceof ServerPlayer) {
            playerIn.getInventory().placeItemBackInInventory(itemHandler.getStackInSlot(1));
            itemHandler.setStackInSlot(1, ItemStack.EMPTY);
        }
    }

    public boolean checkCanPlaceEnchantedBook(ItemStack stack) {
        return session.canApplyEnchantedBook(stack);
    }

    private EnchantmentTableRules.MergeOptions mergeOptions() {
        return EnchantmentTableRules.MergeOptions.from(Config.snapshot());
    }

    private void playUseSound() {
        BlockPos pos = boundBlockEntity != null ? boundBlockEntity.getBlockPos() : new BlockPos(x, y, z);
        world.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public int currentPage = 0;
    public int totalPage = 0;

    public void exportAllEnchantments() {
        EnchantingTableSession.ExportEnchantmentsResult result = session.exportAllEnchantments();
        syncPageState();
        acknowledgeBoundInventoryVersion();
        if (result.success()) {
            entity.getInventory().placeItemBackInInventory(result.exportedStack());
        }
        if (result.playSound()) {
            playUseSound();
        }
    }

    public void resetPage() {
        session.resetPage();
        syncPageState();
    }

    public void nextPage() {
        session.nextPage();
        syncPageState();
    }

    public void previousPage() {
        session.previousPage();
        syncPageState();
    }

    public void turnPage(int targetPage) {
        session.turnPage(targetPage);
        syncPageState();
    }

    public void updateEnchantedBookSlots() {
        session.updateGeneratedSlots();
        syncPageState();
    }

    public void clearCache() {
        session.clearCache();
        syncPageState();
    }

    public void clearPage() {
        session.clearPage();
        syncPageState();
    }

    public void genEnchantedBookCache() {
        session.generateCache();
        syncPageState();
    }

    public boolean addEnchantment(ItemStack itemStack, int slotIndex) {
        return addEnchantment(itemStack, slotIndex, false);
    }

    public boolean addEnchantment(ItemStack itemStackToPut, int slotIndex, boolean forceRegenerateEnchantedBookStore) {
        if (!session.applyEnchantedBook(itemStackToPut).success()) {
            syncPageState();
            return false;
        }
        syncPageState();
        acknowledgeBoundInventoryVersion();
        playUseSound();
        return true;
    }

    private static ItemStack copyWithCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    public boolean removeEnchantment(ItemStack itemStackToRemove) {
        return removeGeneratedBook(itemStackToRemove).regenerated();
    }

    private EnchantingTableSession.GeneratedBookRemovalResult removeGeneratedBook(ItemStack itemStackToRemove) {
        return removeGeneratedBook(itemStackToRemove, -1);
    }

    private EnchantingTableSession.GeneratedBookRemovalResult removeGeneratedBookFromGeneratedSlot(ItemStack itemStackToRemove, int slotIndex) {
        return removeGeneratedBook(itemStackToRemove, session.cacheIndexForGeneratedSlot(slotIndex));
    }

    private EnchantingTableSession.GeneratedBookRemovalResult removeGeneratedBook(ItemStack itemStackToRemove, int cacheIndex) {
        EnchantingTableSession.GeneratedBookRemovalResult result = cacheIndex >= 0
                ? session.removeGeneratedBookAtCacheIndex(itemStackToRemove, cacheIndex)
                : session.removeGeneratedBook(itemStackToRemove);
        syncPageState();
        if (result.success()) {
            acknowledgeBoundInventoryVersion();
            playUseSound();
        }
        return result;
    }

    public void initMenu() {
        session.setCurrentPage(0);
        refreshGeneratedSlotsFromTool();
    }

    private void refreshGeneratedSlotsFromTool() {
        session.refreshGeneratedSlotsFromTool();
        syncPageState();
    }

    private void syncPageState() {
        GeneratedSlotPage page = session.page();
        currentPage = page.currentPage();
        totalPage = page.totalPage();
    }

    private void acknowledgeBoundInventoryVersion() {
        if (boundBlockEntity != null) {
            lastInventoryVersion = boundBlockEntity.getInventoryVersion();
        }
    }

    private boolean isGeneratedSlotIndex(int slotId) {
        return slotId >= 2 && slotId < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE;
    }

    private static class EnchantingMenuInventory implements LogicalInventory {
        private final EnchantingCustomTableBlockEntity blockEntity;
        private final LogicalInventory fallbackToolInventory = new ItemHandlerLogicalInventory(new ItemStackHandler(1));
        private final LogicalInventory virtualInventory = new ItemHandlerLogicalInventory(new ItemStackHandler(ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE - 1));

        private EnchantingMenuInventory(EnchantingCustomTableBlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        public int getSlots() {
            return ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? persistentInventory().getStackInSlot(0) : virtualInventory.getStackInSlot(slot - 1);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (slot == 0) {
                persistentInventory().setStackInSlot(0, stack);
            } else {
                virtualInventory.setStackInSlot(slot - 1, stack);
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot == 0
                    ? persistentInventory().insertItem(0, stack, simulate)
                    : virtualInventory.insertItem(slot - 1, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 0
                    ? persistentInventory().extractItem(0, amount, simulate)
                    : virtualInventory.extractItem(slot - 1, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }

        private LogicalInventory persistentInventory() {
            return blockEntity != null ? blockEntity.getLogicalInventory() : fallbackToolInventory;
        }
    }
}
