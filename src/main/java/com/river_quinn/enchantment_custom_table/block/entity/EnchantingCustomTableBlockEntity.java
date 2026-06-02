package com.river_quinn.enchantment_custom_table.block.entity;

import com.river_quinn.enchantment_custom_table.init.ModBlockEntities;
import com.river_quinn.enchantment_custom_table.core.inventory.AutomationPort;
import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import com.river_quinn.enchantment_custom_table.core.inventory.SlotRole;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantingCustomMenu;
import com.river_quinn.enchantment_custom_table.world.inventory.ItemHandlerLogicalInventory;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnchantingCustomTableBlockEntity extends EnchantingTableLikeBlockEntity implements MenuProvider {
    public static final int TOOL_SLOT = 0;

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            markInventoryChanged();
        }
    };
    private final LogicalInventory logicalInventory = new ItemHandlerLogicalInventory(inventory);
    private final ResourceHandler<ItemResource> automationHandler = new EnchantingAutomationItemHandler();
    private int inventoryVersion = 0;

    public EnchantingCustomTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTING_CUSTOM_TABLE.get(), pos, state);
    }

    @Override
    public @org.jetbrains.annotations.Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new EnchantingCustomMenu(i, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
    }

    public BlockPos getWorldPosition() {
        return this.worldPosition;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public LogicalInventory getLogicalInventory() {
        return logicalInventory;
    }

    public int getInventoryVersion() {
        return inventoryVersion;
    }

    public @Nullable ResourceHandler<ItemResource> getAutomationItemHandler(@Nullable Direction direction) {
        return automationHandler;
    }

    public ItemStack getToolStack() {
        return inventory.getStackInSlot(TOOL_SLOT);
    }

    public void setToolStack(ItemStack stack) {
        inventory.setStackInSlot(TOOL_SLOT, stack);
    }

    public boolean replaceToolEnchantments(ItemEnchantments enchantments) {
        ItemStack toolStack = getToolStack();
        if (toolStack.isEmpty()) {
            return false;
        }
        toolStack.set(EnchantmentHelper.getComponentType(toolStack), enchantments);
        markInventoryChanged();
        return true;
    }

    public boolean canApplyEnchantedBook(ItemStack stack, EnchantmentTableRules.MergeOptions mergeOptions) {
        if (!stack.is(Items.ENCHANTED_BOOK) || getToolStack().isEmpty() || level == null) {
            return false;
        }
        return EnchantmentTableRules.tryMergeEnchantments(
                EnchantmentUtils.getEnchantments(getToolStack()),
                EnchantmentUtils.getEnchantmentLevels(level, stack),
                enchantment -> EnchantmentUtils.getCoreEnchantmentKey(level, enchantment),
                mergeOptions
        ).allowed();
    }

    public boolean tryApplyEnchantedBook(ItemStack stack, EnchantmentTableRules.MergeOptions mergeOptions, boolean simulate) {
        if (!stack.is(Items.ENCHANTED_BOOK) || getToolStack().isEmpty() || level == null) {
            return false;
        }

        List<EnchantmentTableRules.EnchantmentLevel> enchantmentLevels = EnchantmentUtils.getEnchantmentLevels(level, stack);
        if (enchantmentLevels.isEmpty()) {
            return false;
        }

        ItemStack toolStack = getToolStack();
        EnchantmentTableRules.MergeResult result = EnchantmentTableRules.tryMergeEnchantments(
                EnchantmentUtils.getEnchantments(toolStack),
                enchantmentLevels,
                enchantment -> EnchantmentUtils.getCoreEnchantmentKey(level, enchantment),
                mergeOptions
        );
        if (!result.allowed()) {
            return false;
        }

        if (!simulate) {
            replaceToolEnchantments(result.enchantments());
            playUseSound();
        }
        return true;
    }

    public void dropInventory() {
        if (level == null || level.isClientSide()) {
            return;
        }
        ItemStack stack = getToolStack();
        if (!stack.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack.copy());
            inventory.setStackInSlot(TOOL_SLOT, ItemStack.EMPTY);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output.child("Inventory"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("Inventory").ifPresent(inventory::deserialize);
    }

    private EnchantmentTableRules.MergeOptions mergeOptions() {
        return EnchantmentTableRules.MergeOptions.from(com.river_quinn.enchantment_custom_table.Config.snapshot());
    }

    private void playUseSound() {
        if (level != null && !level.isClientSide()) {
            level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private void markInventoryChanged() {
        inventoryVersion++;
        setChanged();
    }

    private class EnchantingAutomationItemHandler implements ResourceHandler<ItemResource>, AutomationPort {
        private final SnapshotJournal<ItemStack> snapshotJournal = new SnapshotJournal<>() {
            @Override
            protected ItemStack createSnapshot() {
                return getToolStack().copy();
            }

            @Override
            protected void revertToSnapshot(ItemStack snapshot) {
                setToolStack(snapshot);
            }
        };

        @Override
        public int size() {
            return 1;
        }

        @Override
        public int getSlots() {
            return size();
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            return 0;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return index == 0 && !resource.isEmpty() && isValid(index, resource) ? 1 : 0;
        }

        @Override
        public SlotRole getRole(int slot) {
            return SlotRole.ENCHANTED_BOOK_INPUT;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack) {
            return slot == 0 && canApplyEnchantedBook(stack, mergeOptions());
        }

        @Override
        public boolean canExtract(int slot) {
            return false;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return index == 0 && !resource.isEmpty() && canApplyEnchantedBook(resource.toStack(), mergeOptions());
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index != 0 || resource.isEmpty() || amount <= 0) {
                return 0;
            }
            ItemStack stack = resource.toStack(1);
            if (!canApplyEnchantedBook(stack, mergeOptions())) {
                return 0;
            }
            snapshotJournal.updateSnapshots(transaction);
            return tryApplyEnchantedBook(stack, mergeOptions(), false) ? 1 : 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }
}
