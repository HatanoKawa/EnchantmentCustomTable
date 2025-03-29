package com.river_quinn.enchantment_custom_table.block.entity;

import com.river_quinn.enchantment_custom_table.init.ModBlockEntities;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentCustomMenu;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.*;

public class EnchantingCustomTableBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ENCHANTED_BOOK_SLOT_ROW_COUNT = 4;
    public static final int ENCHANTED_BOOK_SLOT_COLUMN_COUNT = 6;
    public static final int ENCHANTED_BOOK_SLOT_SIZE = ENCHANTED_BOOK_SLOT_ROW_COUNT * ENCHANTED_BOOK_SLOT_COLUMN_COUNT;
    public static final int ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE = ENCHANTED_BOOK_SLOT_SIZE + 2;
//    private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(ENCHANTED_BOOK_SLOT_SIZE + 2, ItemStack.EMPTY);
    /**
     * index 0: 待附魔工具槽
     * index 1: 附加槽，仅接受附魔，添加附魔书后将会立刻将附魔书的附魔添加到待附魔工具中并重新生成附魔书槽
     * index 2-22: 附魔书槽
     */
    private final ItemStackHandler itemHandler = new ItemStackHandler(ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE);

    public int currentPage = 0;
    public int totalPage = 0;

    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;
    private static final RandomSource RANDOM = RandomSource.create();

    public EnchantingCustomTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTING_CUSTOM_TABLE.get(), pos, state);
    }

//    @Override
//    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
////        super.saveAdditional(tag, registries);
////        // only save the first slot
////        ListTag nbtTagList = new ListTag();
////
////        if (!itemHandler.getStackInSlot(0).isEmpty()) {
////            CompoundTag itemTag = new CompoundTag();
////            itemTag.putInt("Slot", 0);
////            nbtTagList.add(this.itemHandler.getStackInSlot(0).save(registries, itemTag));
////        }
////
////        CompoundTag nbt = new CompoundTag();
////        nbt.put("Items", nbtTagList);
////        nbt.putInt("Size", itemHandler.getSlots());
////
////        tag.put("inventory", nbt);
//    }
//
//    @Override
//    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
////        super.loadAdditional(tag, registries);
////        CompoundTag inventory = tag.getCompound("inventory");
////        this.itemHandler.deserializeNBT(registries, inventory);
////
//////        generateEnchantmentStore();
//    }

    public static void bookAnimationTick(Level level, BlockPos pos, BlockState state, EnchantingCustomTableBlockEntity enchantingTable) {
        enchantingTable.oOpen = enchantingTable.open;
        enchantingTable.oRot = enchantingTable.rot;
        Player player = level.getNearestPlayer((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, (double)3.0F, false);
        if (player != null) {
            double d0 = player.getX() - ((double)pos.getX() + (double)0.5F);
            double d1 = player.getZ() - ((double)pos.getZ() + (double)0.5F);
            enchantingTable.tRot = (float)Mth.atan2(d1, d0);
            enchantingTable.open += 0.1F;
            if (enchantingTable.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float f1 = enchantingTable.flipT;

                do {
                    enchantingTable.flipT += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while(f1 == enchantingTable.flipT);
            }
        } else {
            enchantingTable.tRot += 0.02F;
            enchantingTable.open -= 0.1F;
        }

        while(enchantingTable.rot >= (float)Math.PI) {
            enchantingTable.rot -= ((float)Math.PI * 2F);
        }

        while(enchantingTable.rot < -(float)Math.PI) {
            enchantingTable.rot += ((float)Math.PI * 2F);
        }

        while(enchantingTable.tRot >= (float)Math.PI) {
            enchantingTable.tRot -= ((float)Math.PI * 2F);
        }

        while(enchantingTable.tRot < -(float)Math.PI) {
            enchantingTable.tRot += ((float)Math.PI * 2F);
        }

        float f2;
        for(f2 = enchantingTable.tRot - enchantingTable.rot; f2 >= (float)Math.PI; f2 -= ((float)Math.PI * 2F)) {
        }

        while(f2 < -(float)Math.PI) {
            f2 += ((float)Math.PI * 2F);
        }

        enchantingTable.rot += f2 * 0.4F;
        enchantingTable.open = Mth.clamp(enchantingTable.open, 0.0F, 1.0F);
        ++enchantingTable.time;
        enchantingTable.oFlip = enchantingTable.flip;
        float f = (enchantingTable.flipT - enchantingTable.flip) * 0.4F;
        float f3 = 0.2F;
        f = Mth.clamp(f, -0.2F, 0.2F);
        enchantingTable.flipA += (f - enchantingTable.flipA) * 0.9F;
        enchantingTable.flip += enchantingTable.flipA;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookupProvider) {
        return this.saveWithFullMetadata(lookupProvider);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public void resetPage() {
        currentPage = 0;
        totalPage = 0;
    }

    public void DropToolInFirstSlotOnRemove() {
        ItemStack toolItemStack = this.itemHandler.getStackInSlot(0);
//        ItemStack toolItemStack = this.getItem(0);
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), toolItemStack);
    }

    @Override
    public Component getDisplayName() {
        return null;
    }

    @Override
    public @org.jetbrains.annotations.Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new EnchantmentCustomMenu(i, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
    }

    // 存储当前工具槽中的附魔，用于进行翻页操作
    private final List<ItemStack> enchantmentsOnCurrentTool = new ArrayList<>();

    private Holder.Reference<Enchantment> translateEnchantment(Enchantment enchantment) {
        if (this.level == null)
            return null;
        Registry<Enchantment> fullEnchantmentRegistry = this.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        ResourceKey<Enchantment> resourceKey = fullEnchantmentRegistry.getResourceKey(enchantment).get();
        // 一些通过猜谜获得的逻辑，我不知道为什么要这么做，但是这么做能行
        Optional<Holder.Reference<Enchantment>> optional = this.level
                .registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(resourceKey);
        return optional.get();
    }

    public void exportAllEnchantments(Player player) {
        ItemStack toolItemStack = itemHandler.getStackInSlot(0);
        ItemEnchantments itemEnchantments = toolItemStack.get(EnchantmentHelper.getComponentType(toolItemStack));
        if (!toolItemStack.isEmpty() && itemEnchantments != null) {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);

            for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                Enchantment enchantment = entry.getKey().value();
                int enchantmentLevel = entry.getIntValue();

                var enchantmentReference = translateEnchantment(enchantment);

                assert enchantmentReference != null;
                // set 方法在 level 小于等于 0 时会移除对应附魔
                mutable.set(enchantmentReference, 0);
                enchantedBook.enchant(enchantmentReference, enchantmentLevel);
            }

            toolItemStack.set(EnchantmentHelper.getComponentType(toolItemStack), mutable.toImmutable());
            player.getInventory().placeItemBackInInventory(enchantedBook);

            clearEnchantedBookStore();

            level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

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

    // 保存当前页的附魔，设置新页面并更新附魔书槽
    public void turnPage(int targetPage) {
        if (targetPage < 0 || targetPage >= totalPage) {
            return;
        }
        int indexOffset = currentPage * ENCHANTED_BOOK_SLOT_SIZE;
        for (int i = 0; i < ENCHANTED_BOOK_SLOT_SIZE; i++) {
            int indexOfFullList = i + indexOffset;
            int indexOfSlot = i + 2;
            if (indexOfFullList < enchantmentsOnCurrentTool.size())
                enchantmentsOnCurrentTool.set(indexOfFullList, itemHandler.getStackInSlot(indexOfSlot));
        }
        currentPage = targetPage;
        updateEnchantedBookSlots();
    }

    public void updateEnchantedBookSlots() {
        int indexOffset = currentPage * ENCHANTED_BOOK_SLOT_SIZE;
        // 将附魔书添加到附魔书槽
        for (int i = 0; i < ENCHANTED_BOOK_SLOT_SIZE; i++) {
            int indexOfFullList = i + indexOffset;
            int indexOfSlot = i + 2;
            if (indexOfFullList < enchantmentsOnCurrentTool.size())
                itemHandler.setStackInSlot(indexOfSlot, enchantmentsOnCurrentTool.get(indexOfFullList));
            else
                itemHandler.setStackInSlot(indexOfSlot, ItemStack.EMPTY);
        }
    }

    // 根据待附魔工具槽中的物品生成附魔书槽
    public void genEnchantedBookStore() {
        ItemStack toolItemStack = itemHandler.getStackInSlot(0);

        enchantmentsOnCurrentTool.clear();
        if (!toolItemStack.isEmpty()) {
            ItemEnchantments enchantments = toolItemStack.get(EnchantmentHelper.getComponentType(toolItemStack));

            if (enchantments != null) {
                // 根据待附魔工具槽中的附魔生成对应的附魔书，并添加到 fullEnchantmentList
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                    Enchantment enchantment = entry.getKey().value();
                    Integer enchantmentLevel = entry.getValue();
                    ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);

                    var enchantmentReference = translateEnchantment(enchantment);
                    assert enchantmentReference != null;
                    enchantedBook.enchant(enchantmentReference, enchantmentLevel);

                    enchantmentsOnCurrentTool.add(enchantedBook);
                }
            }
        }

        currentPage = 0;

        totalPage = (int) Math.ceil((double) enchantmentsOnCurrentTool.size() / ENCHANTED_BOOK_SLOT_SIZE);

        updateEnchantedBookSlots();

        // 清除附加槽中的附魔书
        itemHandler.setStackInSlot(1, ItemStack.EMPTY);

        this.setChanged();
    }

    // 获取所有已注册的附魔
    public IdMap<Holder<Enchantment>> getAllRegisteredEnchantments() {
        Registry<Enchantment> fullEnchantmentList = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        return fullEnchantmentList.asHolderIdMap();
    }

    public void addEnchantment(List<EnchantmentInstance> enchantmentInstances) {
        addEnchantment(enchantmentInstances, false);
    }

    public void addEnchantment(List<EnchantmentInstance> enchantmentInstances, boolean forceRegenerateEnchantedBookStore) {
        boolean regenerateEnchantedBookStore = false;

        ItemStack toolItemStack = itemHandler.getStackInSlot(0);
        ItemEnchantments enchantmentsOnTool = toolItemStack.getTagEnchantments();

        IdMap<Holder<Enchantment>> allRegisteredEnchantments = getAllRegisteredEnchantments();
        HashMap<Integer, EnchantmentInstance> resultEnchantmentMap = new HashMap<>();

        // region 遍历带附魔物品槽中物品的附魔

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantmentsOnTool.entrySet()) {
            Enchantment enchantment = entry.getKey().value();
            int enchantmentId = allRegisteredEnchantments.getId(Holder.direct(enchantment));
            int enchantmentLevel = entry.getIntValue();

            EnchantmentInstance enchantmentInstance = new EnchantmentInstance(Holder.direct(enchantment), enchantmentLevel);
            resultEnchantmentMap.put(enchantmentId, enchantmentInstance);
        }

        // endregion

        //region 遍历放入的附魔书的附魔

        for (EnchantmentInstance enchantmentInstance : enchantmentInstances) {
            int enchantmentId = allRegisteredEnchantments.getId(enchantmentInstance.enchantment);
            if (resultEnchantmentMap.containsKey(enchantmentId)) {
                regenerateEnchantedBookStore = true;
                // 若附魔已经存在，直接相加两者的附魔等级
                resultEnchantmentMap.put(enchantmentId, new EnchantmentInstance(
                        enchantmentInstance.enchantment,
                        resultEnchantmentMap.get(enchantmentId).level + enchantmentInstance.level
                ));
            } else {
                // 若附魔不存在，直接生成同样附魔等级的附魔
                resultEnchantmentMap.put(enchantmentId, new EnchantmentInstance(enchantmentInstance.enchantment, enchantmentInstance.level));
            }
        }

        //endregion

        //region 将附魔应用到待附魔物品槽中的物品
        ItemEnchantments itemEnchantments = toolItemStack.get(EnchantmentHelper.getComponentType(toolItemStack));
        // 转换成可变形式
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
        for (EnchantmentInstance enchantmentInstance : resultEnchantmentMap.values().stream().toList()) {
            var enchantmentReference = translateEnchantment(enchantmentInstance.enchantment.value());
            assert enchantmentReference != null;
            mutable.set(enchantmentReference, 0);
            // set 方法在 level 小于等于 0 时会移除对应附魔
            mutable.set(enchantmentReference, enchantmentInstance.level);
        }
        toolItemStack.set(EnchantmentHelper.getComponentType(toolItemStack), mutable.toImmutable());
        // endregion

        // 如果放入了携带相同附魔的附魔书，或者携带多附魔词条的附魔书，那么需要重新生成附魔书槽
        if (enchantmentInstances.size() > 1 || forceRegenerateEnchantedBookStore || regenerateEnchantedBookStore) {
            genEnchantedBookStore();
        }

        level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (enchantmentsOnTool.entrySet().isEmpty()) {
            // 若待附魔工具中没有附魔，重新设置一个初始的页码
            currentPage = 0;
            totalPage = 1;
        }
    }

    public void removeEnchantment(List<EnchantmentInstance> enchantmentInstances) {

        //region 将附魔应用到待附魔物品槽中的物品
        ItemStack toolItemStack = itemHandler.getStackInSlot(0);
        ItemEnchantments itemEnchantments = toolItemStack.get(EnchantmentHelper.getComponentType(toolItemStack));
        // 转换成可变形式
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
        for (EnchantmentInstance enchantmentInstance : enchantmentInstances) {
            var enchantmentReference = translateEnchantment(enchantmentInstance.enchantment.value());
            assert enchantmentReference != null;
            // set 方法在 level 小于等于 0 时会移除对应附魔
            mutable.set(enchantmentReference, 0);
        }
        toolItemStack.set(EnchantmentHelper.getComponentType(toolItemStack), mutable.toImmutable());
        // endregion

        level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public void clearEnchantedBookStore() {
        for (int i = 2; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                itemHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    public void initMenu() {
        clearEnchantedBookStore();
        enchantmentsOnCurrentTool.clear();
        resetPage();
    }
}
