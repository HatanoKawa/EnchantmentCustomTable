package com.river_quinn.enchantment_custom_table.block.entity;

import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.core.config.TableConfigView;
import com.river_quinn.enchantment_custom_table.init.ModBlockEntities;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class EnchantmentConversionTableBlockEntity extends EnchantingTableLikeBlockEntity implements MenuProvider {
    public static final int BOOK_SLOT = 0;
    public static final int PAYMENT_SLOT = 1;
    public static final int TEMPLATE_SLOT = 2;
    public static final int COPY_RESULT_SLOT = 3;
    public static final int SLOT_COUNT = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case BOOK_SLOT -> stack.is(Items.BOOK);
                case PAYMENT_SLOT -> isPaymentItem(stack);
                case TEMPLATE_SLOT -> isValidCopyTemplate(stack);
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            markInventoryChanged();
            if (!updatingCopyResult) {
                refreshCopyResult();
            }
        }
    };
    private final IItemHandler automationHandler = new ConversionAutomationItemHandler();
    private boolean updatingCopyResult = false;
    private int inventoryVersion = 0;

    public EnchantmentConversionTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTMENT_CONVERSION_TABLE.get(), pos, state);
    }

    @Override
    public @org.jetbrains.annotations.Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new EnchantmentConversionMenu(i, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getInventoryVersion() {
        return inventoryVersion;
    }

    public @Nullable IItemHandler getAutomationItemHandler(@Nullable Direction direction) {
        return automationHandler;
    }

    public boolean isCopyMode() {
        return isValidCopyTemplate(inventory.getStackInSlot(TEMPLATE_SLOT));
    }

    public boolean isPaymentItem(ItemStack stack) {
        return EnchantmentTableRules.paymentCostFor(stack.getItem(), config()) > 0;
    }

    public boolean isValidCopyTemplate(ItemStack stack) {
        if (level == null || !stack.is(Items.ENCHANTED_BOOK)) {
            return false;
        }

        var enchantments = EnchantmentUtils.getEnchantments(stack);
        if (enchantments.size() != 1) {
            return false;
        }

        Object2IntMap.Entry<Holder<Enchantment>> entry = enchantments.entrySet().iterator().next();
        Holder<Enchantment> enchantment = EnchantmentUtils.resolveEnchantmentHolder(level, entry.getKey()).orElse(entry.getKey());
        int level = entry.getIntValue();
        return EnchantmentTableRules.isValidSingleEnchantmentTemplate(
                enchantments.size(),
                level,
                enchantment.value().getMaxLevel()
        );
    }

    public boolean hasEnoughMaterialsForCopy() {
        return inventory.getStackInSlot(BOOK_SLOT).is(Items.BOOK)
                && EnchantmentTableRules.hasEnoughPayment(inventory.getStackInSlot(PAYMENT_SLOT), config());
    }

    public void refreshCopyResult() {
        if (!EnchantmentTableRules.shouldGenerateCopyResult(
                isCopyMode(),
                inventory.getStackInSlot(COPY_RESULT_SLOT).isEmpty(),
                hasEnoughMaterialsForCopy()
        )) {
            return;
        }

        updatingCopyResult = true;
        try {
            inventory.getStackInSlot(BOOK_SLOT).shrink(1);
            EnchantmentTableRules.consumePayment(inventory.getStackInSlot(PAYMENT_SLOT), config());
            inventory.setStackInSlot(
                    COPY_RESULT_SLOT,
                    inventory.getStackInSlot(TEMPLATE_SLOT).copyWithCount(1)
            );
        } finally {
            updatingCopyResult = false;
            markInventoryChanged();
        }
    }

    public void dropInventory() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack.copy());
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        refreshCopyResult();
    }

    private void markInventoryChanged() {
        inventoryVersion++;
        setChanged();
    }

    private TableConfigView config() {
        return Config.snapshot();
    }

    private class ConversionAutomationItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 3;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 2 ? inventory.getStackInSlot(COPY_RESULT_SLOT) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int internalSlot = switch (slot) {
                case 0 -> BOOK_SLOT;
                case 1 -> PAYMENT_SLOT;
                default -> -1;
            };
            if (internalSlot < 0 || stack.isEmpty() || !inventory.isItemValid(internalSlot, stack)) {
                return stack;
            }
            ItemStack remainder = inventory.insertItem(internalSlot, stack, simulate);
            if (!simulate) {
                refreshCopyResult();
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 2) {
                return ItemStack.EMPTY;
            }
            ItemStack extracted = inventory.extractItem(COPY_RESULT_SLOT, amount, simulate);
            if (!simulate && !extracted.isEmpty()) {
                refreshCopyResult();
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 2 ? 1 : 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.is(Items.BOOK);
                case 1 -> isPaymentItem(stack);
                default -> false;
            };
        }
    }
}
