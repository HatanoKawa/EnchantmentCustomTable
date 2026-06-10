package com.river_quinn.enchantment_custom_table.world.inventory;

import com.mojang.datafixers.util.Pair;
import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantmentConversionTableBlockEntity;
import com.river_quinn.enchantment_custom_table.core.config.TableConfigView;
import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import com.river_quinn.enchantment_custom_table.core.layout.TableMenuLayout;
import com.river_quinn.enchantment_custom_table.core.net.ConversionTableActions;
import com.river_quinn.enchantment_custom_table.core.session.ConversionTableSession;
import com.river_quinn.enchantment_custom_table.core.session.GeneratedSlotPage;
import com.river_quinn.enchantment_custom_table.init.ModBlocks;
import com.river_quinn.enchantment_custom_table.init.ModMenus;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentConversionMenu extends AbstractContainerMenu implements ConversionTableActions {
    public static final int ENCHANTED_BOOK_SLOT_ROW_COUNT = 4;
    public static final int ENCHANTED_BOOK_SLOT_COLUMN_COUNT = 7;
    public static final int ENCHANTED_BOOK_SLOT_SIZE = ENCHANTED_BOOK_SLOT_ROW_COUNT * ENCHANTED_BOOK_SLOT_COLUMN_COUNT;
    public static final int ENCHANTED_BOOK_SLOT_START = 2;
    public static final int TEMPLATE_BOOK_SLOT = ENCHANTED_BOOK_SLOT_START + ENCHANTED_BOOK_SLOT_SIZE;
    public static final int COPY_RESULT_SLOT = TEMPLATE_BOOK_SLOT + 1;
    public static final int ENCHANTMENT_CONVERSION_SLOT_SIZE = COPY_RESULT_SLOT + 1;
    private static final int PLAYER_MAIN_INVENTORY_SIZE = 27;
    private static final int PLAYER_INVENTORY_START = ENCHANTMENT_CONVERSION_SLOT_SIZE;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + PLAYER_MAIN_INVENTORY_SIZE;
    private static final Pair<ResourceLocation, ResourceLocation> EMPTY_BOOK_SLOT_ICON = Pair.of(
            InventoryMenu.BLOCK_ATLAS,
            new ResourceLocation("enchantment_custom_table", "item/empty_slot_book")
    );
    private static final Pair<ResourceLocation, ResourceLocation> DISABLED_BOOK_SLOT_ICON = Pair.of(
            InventoryMenu.BLOCK_ATLAS,
            new ResourceLocation("enchantment_custom_table", "item/empty_slot_book_disabled")
    );
    private static final Pair<ResourceLocation, ResourceLocation> EMERALD_SLOT_ICON = Pair.of(
            InventoryMenu.BLOCK_ATLAS,
            new ResourceLocation("minecraft", "item/empty_slot_emerald")
    );
    private static final Pair<ResourceLocation, ResourceLocation> TEMPLATE_SLOT_ICON = Pair.of(
            InventoryMenu.BLOCK_ATLAS,
            new ResourceLocation("enchantment_custom_table", "item/copy_template_slot")
    );
    private static final Pair<ResourceLocation, ResourceLocation> OUTPUT_SLOT_ICON = Pair.of(
            InventoryMenu.BLOCK_ATLAS,
            new ResourceLocation("enchantment_custom_table", "item/output_slot")
    );

    private final LogicalInventory logicalInventory;
    private final LogicalInventoryItemHandler itemHandler;
    private final ConversionTableSession session;

    public final Level world;
    public final Player entity;
    public int x, y, z;
    private ContainerLevelAccess access = ContainerLevelAccess.NULL;
    private boolean hasValidPosition = false;
    private final Map<Integer, Slot> enchantedBookSlots = new HashMap<>();
    public EnchantmentConversionTableBlockEntity boundBlockEntity = null;
    private int lastInventoryVersion = -1;

    public EnchantmentConversionMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModMenus.ENCHANTMENT_CONVERSION.get(), id);
        this.entity = inv.player;
        this.world = inv.player.level();

        BlockPos pos = null;
        if (extraData != null) {
            pos = extraData.readBlockPos();
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            access = ContainerLevelAccess.create(world, pos);
            hasValidPosition = true;
        }
        if (pos != null && this.world.getBlockEntity(pos) instanceof EnchantmentConversionTableBlockEntity blockEntity) {
            boundBlockEntity = blockEntity;
        }

        this.logicalInventory = new ConversionMenuInventory(boundBlockEntity);
        this.itemHandler = new LogicalInventoryItemHandler(logicalInventory);
        this.session = new ConversionTableSession(
                world,
                logicalInventory,
                EnchantmentUtils.service(),
                this::isCopyMode,
                this::config,
                0,
                1,
                ENCHANTED_BOOK_SLOT_START,
                ENCHANTED_BOOK_SLOT_SIZE
        );

        addPageDataSlots();
        addTableSlots();
        addPlayerSlots(inv);

        regenerateEnchantedBookSlot();
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
        this.addSlot(new SlotItemHandler(itemHandler, 0, TableMenuLayout.Conversion.BOOK_SLOT_X, TableMenuLayout.Conversion.BOOK_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.BOOK);
            }

            @Override
            public void set(ItemStack stack) {
                ItemStack oldStack = getItem().copy();
                super.set(stack);
                if (!ItemStack.matches(stack, oldStack)) {
                    refreshEnchantedBookSlotsAfterInputsChanged();
                }
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return EMPTY_BOOK_SLOT_ICON;
            }
        });

        this.addSlot(new SlotItemHandler(itemHandler, 1, TableMenuLayout.Conversion.PAYMENT_SLOT_X, TableMenuLayout.Conversion.PAYMENT_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return boundBlockEntity != null
                        ? boundBlockEntity.isPaymentItem(stack)
                        : EnchantmentTableRules.paymentCostFor(stack.getItem(), config()) > 0;
            }

            @Override
            public void set(ItemStack stack) {
                ItemStack oldStack = getItem().copy();
                super.set(stack);
                if (!ItemStack.matches(stack, oldStack)) {
                    refreshEnchantedBookSlotsAfterInputsChanged();
                }
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return EMERALD_SLOT_ICON;
            }
        });

        int enchantedBookIndex = 0;
        for (int row = 0; row < ENCHANTED_BOOK_SLOT_ROW_COUNT; row++) {
            int yPos = TableMenuLayout.Conversion.generatedSlotY(row);
            for (int col = 0; col < ENCHANTED_BOOK_SLOT_COLUMN_COUNT; col++) {
                int xPos = TableMenuLayout.Conversion.generatedSlotX(col);
                int finalIndex = enchantedBookIndex;
                this.enchantedBookSlots.put(finalIndex, this.addSlot(
                        new SlotItemHandler(itemHandler, finalIndex + ENCHANTED_BOOK_SLOT_START, xPos, yPos) {
                            @Override
                            public boolean mayPlace(ItemStack stack) {
                                return false;
                            }

                            @Override
                            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                                return isCopyMode() ? DISABLED_BOOK_SLOT_ICON : EMPTY_BOOK_SLOT_ICON;
                            }

                            @Override
                            public boolean mayPickup(Player player) {
                                return canPickEnchantedBook();
                            }

                            @Override
                            public void onTake(Player player, ItemStack stack) {
                                if (!world.isClientSide) {
                                    pickEnchantedBook();
                                    getItem();
                                }
                            }
                        }
                ));
                enchantedBookIndex++;
            }
        }

        this.addSlot(new SlotItemHandler(itemHandler, TEMPLATE_BOOK_SLOT, TableMenuLayout.Conversion.TEMPLATE_SLOT_X, TableMenuLayout.Conversion.TEMPLATE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return boundBlockEntity != null && boundBlockEntity.isValidCopyTemplate(stack);
            }

            @Override
            public void set(ItemStack stack) {
                ItemStack oldStack = getItem().copy();
                super.set(stack);
                if (!ItemStack.matches(stack, oldStack)) {
                    regenerateEnchantedBookSlot();
                }
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return TEMPLATE_SLOT_ICON;
            }
        });

        this.addSlot(new SlotItemHandler(itemHandler, COPY_RESULT_SLOT, TableMenuLayout.Conversion.COPY_RESULT_SLOT_X, TableMenuLayout.Conversion.COPY_RESULT_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                if (boundBlockEntity != null) {
                    boundBlockEntity.refreshCopyResult();
                }
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return OUTPUT_SLOT_ICON;
            }
        });
    }

    private void addPlayerSlots(Inventory inv) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + (row + 1) * 9,
                        TableMenuLayout.Conversion.PLAYER_INVENTORY_X + col * TableMenuLayout.SLOT_SIZE,
                        TableMenuLayout.Conversion.PLAYER_INVENTORY_Y + row * TableMenuLayout.SLOT_SIZE));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col,
                    TableMenuLayout.Conversion.PLAYER_INVENTORY_X + col * TableMenuLayout.SLOT_SIZE,
                    TableMenuLayout.Conversion.PLAYER_INVENTORY_Y + 58));
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (boundBlockEntity != null && lastInventoryVersion != boundBlockEntity.getInventoryVersion()) {
            lastInventoryVersion = boundBlockEntity.getInventoryVersion();
            refreshEnchantedBookSlotsAfterInputsChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return hasValidPosition
                && AbstractContainerMenu.stillValid(access, player, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < ENCHANTMENT_CONVERSION_SLOT_SIZE) {
                if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else if (!this.moveItemStackTo(itemstack1, 0, ENCHANTMENT_CONVERSION_SLOT_SIZE, false)) {
                if (index < PLAYER_HOTBAR_START) {
                    if (!this.moveItemStackTo(itemstack1, PLAYER_HOTBAR_START, this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
                    return ItemStack.EMPTY;
                }
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn, itemstack1);
        }

        if (index < 2 || index == TEMPLATE_BOOK_SLOT || index == COPY_RESULT_SLOT) {
            refreshEnchantedBookSlotsAfterInputsChanged();
        }

        return itemstack;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverse) {
        return MenuStackMover.moveItemStackTo(this.slots, stack, startIndex, endIndex, reverse);
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
    }

    public void setSearchQuery(String query, String clientLanguage, List<String> matchedEnchantments) {
        session.setSearchQuery(query, clientLanguage, matchedEnchantments);
        syncPageState();
    }

    public String getSearchQuery() {
        return session.searchQuery();
    }

    public String getSearchClientLanguage() {
        return session.searchClientLanguage();
    }

    public int currentPage = 0;
    public int totalPage = 0;

    public void nextPage() {
        session.nextPage();
        syncPageState();
    }

    public void previousPage() {
        session.previousPage();
        syncPageState();
    }

    public void turnPage(int page) {
        session.turnPage(page);
        syncPageState();
    }

    public void resetPage() {
        session.resetPage();
        syncPageState();
    }

    public void clearEnchantedBookSlot() {
        session.clearGeneratedSlots();
        syncPageState();
    }

    public void genEnchantedBookSlot() {
        session.generateGeneratedSlots();
        syncPageState();
    }

    public void regenerateEnchantedBookSlot() {
        session.regenerateGeneratedSlots();
        syncPageState();
    }

    private void refreshEnchantedBookSlotsAfterInputsChanged() {
        session.refreshGeneratedSlotsAfterInputsChanged();
        syncPageState();
    }

    public boolean canPickEnchantedBook() {
        return session.canPickGeneratedBook();
    }

    public boolean pickEnchantedBook() {
        var result = session.pickGeneratedBook();
        syncPageState();
        return result.success();
    }

    private boolean isCopyMode() {
        return boundBlockEntity != null && boundBlockEntity.isCopyMode();
    }

    private TableConfigView config() {
        return Config.snapshot();
    }

    private void syncPageState() {
        GeneratedSlotPage page = session.page();
        currentPage = page.currentPage();
        totalPage = page.totalPage();
    }

    private static class ConversionMenuInventory implements LogicalInventory {
        private final EnchantmentConversionTableBlockEntity blockEntity;
        private final LogicalInventory fallbackPersistentInventory =
                new ItemHandlerLogicalInventory(new ItemStackHandler(EnchantmentConversionTableBlockEntity.SLOT_COUNT));
        private final LogicalInventory virtualInventory = new ItemHandlerLogicalInventory(new ItemStackHandler(ENCHANTED_BOOK_SLOT_SIZE));

        private ConversionMenuInventory(EnchantmentConversionTableBlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        public int getSlots() {
            return ENCHANTMENT_CONVERSION_SLOT_SIZE;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot < ENCHANTED_BOOK_SLOT_START) {
                return persistentInventory().getStackInSlot(slot);
            }
            if (slot < TEMPLATE_BOOK_SLOT) {
                return virtualInventory.getStackInSlot(slot - ENCHANTED_BOOK_SLOT_START);
            }
            return persistentInventory().getStackInSlot(toPersistentSlot(slot));
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (slot < ENCHANTED_BOOK_SLOT_START) {
                persistentInventory().setStackInSlot(slot, stack);
            } else if (slot < TEMPLATE_BOOK_SLOT) {
                virtualInventory.setStackInSlot(slot - ENCHANTED_BOOK_SLOT_START, stack);
            } else {
                persistentInventory().setStackInSlot(toPersistentSlot(slot), stack);
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot < ENCHANTED_BOOK_SLOT_START) {
                return persistentInventory().insertItem(slot, stack, simulate);
            }
            if (slot < TEMPLATE_BOOK_SLOT) {
                return virtualInventory.insertItem(slot - ENCHANTED_BOOK_SLOT_START, stack, simulate);
            }
            return persistentInventory().insertItem(toPersistentSlot(slot), stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < ENCHANTED_BOOK_SLOT_START) {
                return persistentInventory().extractItem(slot, amount, simulate);
            }
            if (slot < TEMPLATE_BOOK_SLOT) {
                return virtualInventory.extractItem(slot - ENCHANTED_BOOK_SLOT_START, amount, simulate);
            }
            return persistentInventory().extractItem(toPersistentSlot(slot), amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot < ENCHANTED_BOOK_SLOT_START) {
                return persistentInventory().getSlotLimit(slot);
            }
            if (slot < TEMPLATE_BOOK_SLOT) {
                return virtualInventory.getSlotLimit(slot - ENCHANTED_BOOK_SLOT_START);
            }
            return persistentInventory().getSlotLimit(toPersistentSlot(slot));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot < ENCHANTED_BOOK_SLOT_START) {
                return persistentInventory().isItemValid(slot, stack);
            }
            if (slot < TEMPLATE_BOOK_SLOT) {
                return false;
            }
            return persistentInventory().isItemValid(toPersistentSlot(slot), stack);
        }

        private LogicalInventory persistentInventory() {
            return blockEntity != null ? blockEntity.getLogicalInventory() : fallbackPersistentInventory;
        }

        private int toPersistentSlot(int slot) {
            return switch (slot) {
                case TEMPLATE_BOOK_SLOT -> EnchantmentConversionTableBlockEntity.TEMPLATE_SLOT;
                case COPY_RESULT_SLOT -> EnchantmentConversionTableBlockEntity.COPY_RESULT_SLOT;
                default -> throw new IndexOutOfBoundsException(slot);
            };
        }
    }
}
