package com.river_quinn.enchantment_custom_table.block.entity;

import com.river_quinn.enchantment_custom_table.init.ModBlockEntities;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentConversionTableBlockEntity extends EnchantingTableLikeBlockEntity implements MenuProvider {
    public static final int ENCHANTED_BOOK_SLOT_ROW_COUNT = 4;
    public static final int ENCHANTED_BOOK_SLOT_COLUMN_COUNT = 7;
    public static final int ENCHANTED_BOOK_SLOT_SIZE = ENCHANTED_BOOK_SLOT_ROW_COUNT * ENCHANTED_BOOK_SLOT_COLUMN_COUNT;
    public static final int ENCHANTMENT_CONVERSION_SLOT_SIZE = ENCHANTED_BOOK_SLOT_SIZE + 2;
    /**
     * index 0: 待附魔工具槽
     * index 1: 附加槽，仅接受附魔，添加附魔书后将会立刻将附魔书的附魔添加到待附魔工具中并重新生成附魔书槽
     * index 2-29: 附魔书槽
     */
    private final ItemStackHandler itemHandler = new ItemStackHandler(ENCHANTMENT_CONVERSION_SLOT_SIZE);

    public EnchantmentConversionTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTMENT_CONVERSION_TABLE.get(), pos, state);
    }

//    @Override
//    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
//        super.saveAdditional(tag, registries);
//        // only save the first slot
//        ListTag nbtTagList = new ListTag();
//
//        // save book
//        if (!this.itemHandler.getStackInSlot(0).isEmpty()) {
//            CompoundTag bookItemTag = new CompoundTag();
//            bookItemTag.putInt("Slot", 0);
//            nbtTagList.add(this.itemHandler.getStackInSlot(0).save(registries, bookItemTag));
//        }
//
//        // save emerald or emerald block
//        if (!this.itemHandler.getStackInSlot(1).isEmpty()) {
//            CompoundTag emeraldItemTag = new CompoundTag();
//            emeraldItemTag.putInt("Slot", 1);
//            nbtTagList.add(this.itemHandler.getStackInSlot(1).save(registries, emeraldItemTag));
//        }
//
//        CompoundTag nbt = new CompoundTag();
//        nbt.put("Items", nbtTagList);
//        nbt.putInt("Size", itemHandler.getSlots());
//
//        tag.put("inventory", nbt);
//
//    }
//
//    @Override
//    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
//        super.loadAdditional(tag, registries);
//        CompoundTag inventory = tag.getCompound("inventory");
//        this.itemHandler.deserializeNBT(registries, inventory);
//    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public @org.jetbrains.annotations.Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new EnchantmentConversionMenu(i, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
    }

    public static final List<Integer> allEnchantments = new ArrayList<>();

    public void tryGetAllEnchantments() {
        if (allEnchantments.isEmpty()) {
            Registry<Enchantment> fullEnchantmentList = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            IdMap<Holder<Enchantment>> allRegisteredEnchantments = fullEnchantmentList.asHolderIdMap();
            allRegisteredEnchantments.forEach(enchantment ->
                    allEnchantments.add(fullEnchantmentList.getId(enchantment.value())));
        }
    }

    public ItemStack getEnchantedBook(int enchantmentId) {
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);

        Enchantment enchantment = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).byId(enchantmentId);
        int enchantmentLevel = enchantment.getMaxLevel();
        var enchantmentReference = EnchantmentUtils.translateEnchantment(level, enchantment);
        assert enchantmentReference != null;
        enchantedBook.enchant(enchantmentReference, enchantmentLevel);

        return enchantedBook;
    }

    public int currentPage = 0;
    public int totalPage = 0;

    public void nextPage() {
        if (currentPage < (totalPage - 1)) {
            turnPage(currentPage + 1);
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            turnPage(currentPage - 1);
        }
    }

    public void turnPage(int page) {
        currentPage = page;
        clearEnchantedBookSlot();
        genEnchantedBookSlot();
    }

    public void resetPage() {
        currentPage = 0;
        totalPage = 0;
    }

    public void clearEnchantedBookSlot() {
        for (int i = 2; i < ENCHANTMENT_CONVERSION_SLOT_SIZE; i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public void genEnchantedBookSlot() {
        tryGetAllEnchantments();
        boolean hasBook = itemHandler.getStackInSlot(0).is(Items.BOOK);
        boolean hasEnoughEmerald = false;
        if (itemHandler.getStackInSlot(1).is(Items.EMERALD)) {
            hasEnoughEmerald = itemHandler.getStackInSlot(1).getCount() >= 36;
        } else if (itemHandler.getStackInSlot(1).is(Items.EMERALD_BLOCK)) {
            hasEnoughEmerald = itemHandler.getStackInSlot(1).getCount() >= 4;
        }

        if (!hasBook || !hasEnoughEmerald) {
            resetPage();
            clearEnchantedBookSlot();
            return;
        }

        for (int i = 0; i < ENCHANTED_BOOK_SLOT_SIZE; i++) {
            int slotIndex = i + 2;
            int enchantmentIndex = i + currentPage * ENCHANTED_BOOK_SLOT_SIZE;

            if (enchantmentIndex < allEnchantments.size()) {
                if (itemHandler.getStackInSlot(slotIndex).isEmpty()) {
                    int enchantmentId = allEnchantments.get(enchantmentIndex);
                    itemHandler.setStackInSlot(slotIndex, getEnchantedBook(enchantmentId));
                }
            } else {
                itemHandler.setStackInSlot(slotIndex, ItemStack.EMPTY);
            }
        }
    }

    public void regenerateEnchantedBookSlot() {
        currentPage = 0;
        totalPage = (int) Math.ceil(allEnchantments.size() / (double) ENCHANTED_BOOK_SLOT_SIZE);
        genEnchantedBookSlot();
    }

    public void pickEnchantedBook() {
        if (itemHandler.getStackInSlot(1).is(Items.EMERALD)) {
            itemHandler.getStackInSlot(0).shrink(1);
            itemHandler.getStackInSlot(1).shrink(36);
        } else if (itemHandler.getStackInSlot(1).is(Items.EMERALD_BLOCK)) {
            itemHandler.getStackInSlot(0).shrink(1);
            itemHandler.getStackInSlot(1).shrink(4);
        }
        genEnchantedBookSlot();
    }
    
    public void dropBookAndEmerald() {
        ItemStack bookItemStack = this.itemHandler.getStackInSlot(0);
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), bookItemStack);
        ItemStack emeraldItemStack = this.itemHandler.getStackInSlot(1);
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), emeraldItemStack);
    }
}
