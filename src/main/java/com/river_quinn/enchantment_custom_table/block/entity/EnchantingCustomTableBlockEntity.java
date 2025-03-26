package com.river_quinn.enchantment_custom_table.block.entity;

import com.river_quinn.enchantment_custom_table.init.ModBlockEntities;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentCustomMenu;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.EnchantedBookItem;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EnchantingCustomTableBlockEntity extends BlockEntity implements MenuProvider {
    private final int ENCHANTED_BOOK_SLOT_SIZE = 4 * 7;
//    private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(ENCHANTED_BOOK_SLOT_SIZE + 2, ItemStack.EMPTY);
    /**
     * index 0: 待附魔工具槽
     * index 1: 附加槽，仅接受附魔，添加附魔书后将会立刻将附魔书的附魔添加到待附魔工具中并重新生成附魔书槽
     * index 2-22: 附魔书槽
     */
    private final ItemStackHandler itemHandler = new ItemStackHandler(ENCHANTED_BOOK_SLOT_SIZE + 2);
//    private final ItemStackHandler itemHandler = new ItemStackHandler(ENCHANTED_BOOK_SLOT_SIZE + 2) {
//        @Override
//        protected void onContentsChanged(int slotIndex) {
//            super.onContentsChanged(slotIndex);
//            System.out.println("onContentsChanged event triggered, slot index: " + slotIndex);
//            if (slotIndex == 0) {
//                generateEnchantmentStore();
//            } else if (slotIndex == 1) {
//                return;
//            } else {
//                // 附魔书槽变为空，处理逻辑通过 Menu 类的 onTake 和 setChanged 事件触发
//                return;
//            }
//        }
//    };

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

    // 根据待附魔工具槽中的物品生成附魔书槽
    public void genEnchantedBookStore() {
        ItemStack toolItemStack = itemHandler.getStackInSlot(0);

        List<ItemStack> enchantmentsList = new ArrayList<>();
        if (!toolItemStack.isEmpty()) {
            ItemEnchantments enchantments = toolItemStack.get(EnchantmentHelper.getComponentType(toolItemStack));
            Registry<Enchantment> fullEnchantmentList = this.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);

            if (enchantments != null) {
                // 根据待附魔工具槽中的附魔生成对应的附魔书，并添加到 fullEnchantmentList
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                    Enchantment enchantment = entry.getKey().value();
                    Integer enchantmentLevel = entry.getValue();
                    ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                    ResourceKey<Enchantment> resourceKey = fullEnchantmentList.getResourceKey(enchantment).get();
                    Optional<Holder.Reference<Enchantment>> optional = this.level
                            .registryAccess()
                            .registryOrThrow(Registries.ENCHANTMENT)
                            .getHolder(resourceKey);
                    enchantedBook.enchant(optional.get(), enchantmentLevel);
                    enchantmentsList.add(enchantedBook);
                }
            }
        }

        // 将附魔书添加到附魔书槽
        for (int i = 0; i < ENCHANTED_BOOK_SLOT_SIZE; i++) {
            if (i < enchantmentsList.size())
                itemHandler.setStackInSlot(i + 2, enchantmentsList.get(i));
            else
                itemHandler.setStackInSlot(i + 2, ItemStack.EMPTY);
        }

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
        Registry<Enchantment> fullEnchantmentRegistry = this.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        for (EnchantmentInstance enchantmentInstance : resultEnchantmentMap.values().stream().toList()) {
            ResourceKey<Enchantment> resourceKey = fullEnchantmentRegistry.getResourceKey(enchantmentInstance.enchantment.value()).get();
            // 一些通过猜谜获得的逻辑，我不知道为什么要这么做，但是这么做能行
            Optional<Holder.Reference<Enchantment>> optional = this.level
                    .registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getHolder(resourceKey);
            // set 方法在 level 小于等于 0 时会移除对应附魔
            mutable.set(optional.get(), enchantmentInstance.level);
        }
        toolItemStack.set(EnchantmentHelper.getComponentType(toolItemStack), mutable.toImmutable());
        // endregion

        // 如果放入了携带相同附魔的附魔书，那么需要重新生成附魔书槽
        if (forceRegenerateEnchantedBookStore || regenerateEnchantedBookStore) {
            genEnchantedBookStore();
        }
    }

    public void removeEnchantment(List<EnchantmentInstance> enchantmentInstances) {

        //region 将附魔应用到待附魔物品槽中的物品
        ItemStack toolItemStack = itemHandler.getStackInSlot(0);
        ItemEnchantments itemEnchantments = toolItemStack.get(EnchantmentHelper.getComponentType(toolItemStack));
        // 转换成可变形式
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
        Registry<Enchantment> fullEnchantmentRegistry = this.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        for (EnchantmentInstance enchantmentInstance : enchantmentInstances) {
            ResourceKey<Enchantment> resourceKey = fullEnchantmentRegistry.getResourceKey(enchantmentInstance.enchantment.value()).get();
            // 一些通过猜谜获得的逻辑，我不知道为什么要这么做，但是这么做能行
            Optional<Holder.Reference<Enchantment>> optional = this.level
                    .registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getHolder(resourceKey);
            // set 方法在 level 小于等于 0 时会移除对应附魔
            mutable.set(optional.get(), 0);
        }
        toolItemStack.set(EnchantmentHelper.getComponentType(toolItemStack), mutable.toImmutable());
        // endregion

    }

    public void clearEnchantedBookStore() {
        for (int i = 2; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                itemHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }
}
