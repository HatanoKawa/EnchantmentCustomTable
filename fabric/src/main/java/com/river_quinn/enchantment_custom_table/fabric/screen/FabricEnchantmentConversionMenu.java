package com.river_quinn.enchantment_custom_table.fabric.screen;

import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantmentConversionTableBlockEntity;
import com.river_quinn.enchantment_custom_table.fabric.config.FabricTableConfig;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModBlocks;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModMenus;
import com.river_quinn.enchantment_custom_table.fabric.inventory.FabricTableInventory;
import com.river_quinn.enchantment_custom_table.fabric.session.FabricConversionTableSession;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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

public class FabricEnchantmentConversionMenu extends AbstractContainerMenu {
    public static final int ENCHANTED_BOOK_SLOT_ROW_COUNT = 4;
    public static final int ENCHANTED_BOOK_SLOT_COLUMN_COUNT = 7;
    public static final int ENCHANTED_BOOK_SLOT_SIZE = ENCHANTED_BOOK_SLOT_ROW_COUNT * ENCHANTED_BOOK_SLOT_COLUMN_COUNT;
    public static final int BOOK_SLOT = 0;
    public static final int PAYMENT_SLOT = 1;
    public static final int ENCHANTED_BOOK_SLOT_START = 2;
    public static final int TEMPLATE_BOOK_SLOT = ENCHANTED_BOOK_SLOT_START + ENCHANTED_BOOK_SLOT_SIZE;
    public static final int COPY_RESULT_SLOT = TEMPLATE_BOOK_SLOT + 1;
    public static final int ENCHANTMENT_CONVERSION_SLOT_SIZE = COPY_RESULT_SLOT + 1;
    private static final int PREVIOUS_PAGE_BUTTON = 0;
    private static final int NEXT_PAGE_BUTTON = 1;

    public final Level world;
    public final Player entity;
    public final int x;
    public final int y;
    public final int z;
    private final ContainerLevelAccess access;
    private final FabricTableInventory inventory;
    private final FabricConversionTableSession session;
    private final FabricEnchantmentConversionTableBlockEntity blockEntity;

    public FabricEnchantmentConversionMenu(int id, Inventory inventory, BlockPos pos) {
        super(FabricModMenus.ENCHANTMENT_CONVERSION, id);
        this.entity = inventory.player;
        this.world = inventory.player.level();
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.access = ContainerLevelAccess.create(world, pos);
        this.blockEntity = world.getBlockEntity(pos) instanceof FabricEnchantmentConversionTableBlockEntity table ? table : null;
        this.inventory = blockEntity != null
                ? blockEntity.getInventory()
                : new FabricTableInventory(ENCHANTMENT_CONVERSION_SLOT_SIZE, slot -> 1, (slot, stack) -> false, () -> {});
        this.session = new FabricConversionTableSession(
                world,
                this.inventory,
                () -> blockEntity != null && blockEntity.isCopyMode(),
                FabricTableConfig::snapshot,
                BOOK_SLOT,
                PAYMENT_SLOT,
                ENCHANTED_BOOK_SLOT_START,
                ENCHANTED_BOOK_SLOT_SIZE
        );
        addPageDataSlots();
        addTableSlots();
        regenerateGeneratedSlots();
        addPlayerInventory(inventory, 8, 99);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, FabricModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        switch (id) {
            case PREVIOUS_PAGE_BUTTON -> session.previousPage();
            case NEXT_PAGE_BUTTON -> session.nextPage();
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

    public void setSearchQuery(String query, String clientLanguage, List<ResourceLocation> matchedEnchantments) {
        session.setSearchQuery(query, clientLanguage, matchedEnchantments);
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

    private void regenerateGeneratedSlots() {
        if (blockEntity != null) {
            blockEntity.refreshCopyResult();
        }
        session.regenerateGeneratedSlots();
    }

    private void addTableSlots() {
        addSlot(new TableSlot(BOOK_SLOT, 16, 23) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.BOOK);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                regenerateGeneratedSlots();
            }
        });
        addSlot(new TableSlot(PAYMENT_SLOT, 16, 41) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return EnchantmentTableRules.paymentCostFor(stack.getItem(), FabricTableConfig.snapshot()) > 0;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                regenerateGeneratedSlots();
            }
        });
        int generatedBookIndex = 0;
        for (int row = 0; row < ENCHANTED_BOOK_SLOT_ROW_COUNT; row++) {
            int yPos = 23 + row * 18;
            for (int column = 0; column < ENCHANTED_BOOK_SLOT_COLUMN_COUNT; column++) {
                int xPos = 43 + column * 18;
                addSlot(new TableSlot(ENCHANTED_BOOK_SLOT_START + generatedBookIndex, xPos, yPos) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return session.canPickGeneratedBook();
                    }

                    @Override
                    public void onTake(Player player, ItemStack stack) {
                        super.onTake(player, stack);
                        session.pickGeneratedBook();
                    }
                });
                generatedBookIndex++;
            }
        }
        addSlot(new TableSlot(TEMPLATE_BOOK_SLOT, 16, 59) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return blockEntity != null && blockEntity.isValidCopyTemplate(stack);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                regenerateGeneratedSlots();
            }
        });
        addSlot(new TableSlot(COPY_RESULT_SLOT, 16, 77) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                if (blockEntity != null) {
                    blockEntity.refreshCopyResult();
                }
            }
        });
    }

    private void addPlayerInventory(Inventory inventory, int xOffset, int yOffset) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, xOffset + column * 18, yOffset + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, xOffset + column * 18, yOffset + 58));
        }
    }

    private class TableSlot extends Slot {
        TableSlot(int index, int x, int y) {
            super(inventory, index, x, y);
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
