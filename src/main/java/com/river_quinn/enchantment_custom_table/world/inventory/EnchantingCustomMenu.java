package com.river_quinn.enchantment_custom_table.world.inventory;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.init.ModBlocks;
import com.river_quinn.enchantment_custom_table.init.ModMenus;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class EnchantingCustomMenu extends AbstractContainerMenu {
	public static final int ENCHANTED_BOOK_SLOT_ROW_COUNT = 4;
	public static final int ENCHANTED_BOOK_SLOT_COLUMN_COUNT = 6;
	public static final int ENCHANTED_BOOK_SLOT_SIZE = ENCHANTED_BOOK_SLOT_ROW_COUNT * ENCHANTED_BOOK_SLOT_COLUMN_COUNT;
	public static final int ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE = ENCHANTED_BOOK_SLOT_SIZE + 2;
	private static final int PLAYER_MAIN_INVENTORY_SIZE = 27;
	private static final int PLAYER_INVENTORY_START = ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE;
	private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + PLAYER_MAIN_INVENTORY_SIZE;
	/**
	 * index 0: 待附魔工具槽
	 * index 1: 附加槽，仅接受附魔，添加附魔书后将会立刻将附魔书的附魔添加到待附魔工具中并重新生成附魔书槽
	 * index 2-22: 附魔书槽
	 */
	private final ItemStackHandler itemHandler;

	private static final Logger LOGGER = LogUtils.getLogger();
	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access = ContainerLevelAccess.NULL;
	private boolean hasValidPosition = false;
	private final Map<Integer, Slot> enchantedBookSlots = new HashMap<>();
	public EnchantingCustomTableBlockEntity boundBlockEntity = null;
	private int lastInventoryVersion = -1;

	@Override
	public void clicked(int slotId, int button, ClickType clickType, Player player) {
		// 在 1.21.2 版本及以上时，在尝试堆叠 isSameItemSameComponents 判定为 true 的附魔书时不会触发 setByPlayer 方法，
		// 因此将对于附魔书槽操作的逻辑迁移到更底层的 clicked 方法中

		// 仅额外处理部分情况，需要满足以下条件：
		// 1. 点击的槽位的下标在 2-22 之间
		// 2. 点击类型不是快速移动
		// 3. 附魔书槽对应的物品可以放置在该槽位上（主要是待附魔物品槽不能为空）
		var itemStackToPut = entity.containerMenu.getCarried();
		if (
				slotId >= 2 &&
				slotId < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE &&
				clickType != ClickType.QUICK_MOVE &&
				(itemStackToPut.isEmpty() || getSlot(slotId).mayPlace(entity.containerMenu.getCarried()))
		) {
			var itemStackToReplace = itemHandler.getStackInSlot(slotId);
			if (!itemStackToPut.isEmpty() && !itemStackToReplace.isEmpty()) {
				// 当尝试替换附魔书槽的附魔书时，存在以下两种情况：
				// 1. 新旧附魔书没有重复的附魔，此时去除工具上的旧附魔，添加新的附魔，返回旧的附魔书
				// 2. 新旧附魔书有重复的附魔，此时直接添加新的附魔书的附魔到工具上，不返回附魔书
				// 此段逻辑用于处理第二种情况

				// 新的物品槽对应的附魔书可能同时有多种附魔
				var enchantmentsOnNewStack = getEnchantmentLevelsFromEnchantedBook(itemStackToPut);
				// 旧的物品槽对应的附魔书最多只有一种附魔
				var enchantmentsOnOldStack = getEnchantmentLevelsFromEnchantedBook(itemStackToReplace);
				if (enchantmentsOnOldStack.isEmpty()) {
					super.clicked(slotId, button, clickType, player);
					return;
				}
				var enchantmentOnOldStack = enchantmentsOnOldStack.get(0);
				var hasDuplicateEnchantment = EnchantmentTableRules.containsMatchingEnchantment(
						enchantmentsOnNewStack,
						enchantmentOnOldStack.enchantment(),
						this::isSameEnchantment
				);
				if (hasDuplicateEnchantment) {
					// 如果新旧物品槽的对应的附魔书有重复的附魔，则直接添加到工具上，合并附魔并不返回旧的附魔书
					addEnchantment(itemStackToPut, slotId, true);
					entity.containerMenu.setCarried(ItemStack.EMPTY.copy());
					return;
				}
			}

			int enchantmentIndexInCache = EnchantmentTableRules.cacheIndexForGeneratedSlot(
					slotId,
					2,
					currentPage,
					ENCHANTED_BOOK_SLOT_SIZE
			);

			// 以下逻辑用于处理第一种情况
			if (!itemStackToReplace.isEmpty()) {
				entity.containerMenu.setCarried(itemStackToReplace.copy());
				// 移除旧的槽位对应附魔书的附魔
				var hasRegenerated = removeEnchantment(itemStackToReplace);
				// 在缓存中删除对应的附魔书
				// 如果移除附魔书导致了总页数变更，将会触发重新生成附魔书缓存，此时对应的附魔书槽下标可能会产生溢出，所以需要进行判断
				if (!hasRegenerated && enchantmentIndexInCache < enchantmentsOnCurrentTool.size()) {
					enchantmentsOnCurrentTool.set(enchantmentIndexInCache, ItemStack.EMPTY);
				}
			} else {
				// 如果没有待移除的附魔书，则将指针上的物品设置为 0
				entity.containerMenu.setCarried(ItemStack.EMPTY.copy());
			}
			if (!itemStackToPut.isEmpty()) {
				// 添加新的槽位对应附魔书的附魔
				addEnchantment(itemStackToPut, slotId);
			}
			updateEnchantedBookSlots();
		} else {
			super.clicked(slotId, button, clickType, player);
		}
	}

	public EnchantingCustomMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		super(ModMenus.ENCHANTING_CUSTOM.get(), id);
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
		if (pos != null) {
			if (this.world.getBlockEntity(pos) instanceof EnchantingCustomTableBlockEntity blockEntity) {
				boundBlockEntity = blockEntity;
			}
		}
		this.itemHandler = new EnchantingMenuItemHandler(boundBlockEntity);

		this.addDataSlot(new DataSlot() {
			@Override
			public int get() {
				return currentPage;
			}

			@Override
			public void set(int value) {
				currentPage = value;
			}
		});
		this.addDataSlot(new DataSlot() {
			@Override
			public int get() {
				return totalPage;
			}

			@Override
			public void set(int value) {
				totalPage = value;
			}
		});

		this.addSlot(new SlotItemHandler(itemHandler, 0, 8, 8) {
			@Override
			public void onQuickCraft(ItemStack newStack, ItemStack oldStack) {
				super.onQuickCraft(newStack, oldStack);
				clearCache();
				clearPage();
			}

			@Override
			public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
				super.setByPlayer(newStack, oldStack);
				if (!newStack.isEmpty()) {
					// 放置待附魔工具，重新生成附魔书槽
					genEnchantedBookCache();
					currentPage = 0;
					updateEnchantedBookSlots();
				} else {
					// 取出待附魔工具，清空附魔书槽
					clearCache();
					clearPage();
				}
			}
		});

		this.addSlot(new SlotItemHandler(itemHandler, 1, 42, 8) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return Items.ENCHANTED_BOOK == stack.getItem()
						&& !getItemHandler().getStackInSlot(0).isEmpty()
						&& checkCanPlaceEnchantedBook(stack);
			}

			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(
						InventoryMenu.BLOCK_ATLAS,
						ResourceLocation.tryParse("enchantment_custom_table:item/empty_slot_book")
				);
			}

			@Override
			public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
				super.setByPlayer(newStack, oldStack);
				if (!newStack.isEmpty()) {
					// 放置附魔书，同步添加工具上的附魔，并删除附加槽的附魔书，重新生成附魔书槽
					addEnchantment(newStack, 1, true);
				} else {
					// 合法情况下不应该存在这种状况
					LOGGER.warn("stack 1, setByPlayer() called with newStack.isEmpty()");
				}
			}
		});

		int enchanted_book_index = 0;
		for (int row = 0; row < ENCHANTED_BOOK_SLOT_ROW_COUNT; row++) {
			int yPos = 8 + row * 18;
			for (int col = 0; col < ENCHANTED_BOOK_SLOT_COLUMN_COUNT; col++) {
				int xPos = 61 + col * 18;
				int final_enchanted_book_index = enchanted_book_index;
				this.enchantedBookSlots.put(final_enchanted_book_index, this.addSlot(
					new SlotItemHandler(itemHandler, final_enchanted_book_index + 2, xPos, yPos) {
						@Override
						public boolean mayPlace(ItemStack stack) {
							return Items.ENCHANTED_BOOK == stack.getItem()
									&& !getItemHandler().getStackInSlot(0).isEmpty()
									&& checkCanPlaceEnchantedBook(stack);
						}

						@Override
						public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
							return Pair.of(
									InventoryMenu.BLOCK_ATLAS,
									ResourceLocation.tryParse("enchantment_custom_table:item/empty_slot_book")
							);
						}

					}
				));
				enchanted_book_index++;
			}
		}

		for (int si = 0; si < 3; ++si)
			for (int sj = 0; sj < 9; ++sj)
				this.addSlot(new Slot(inv, sj + (si + 1) * 9, 0 + 8 + sj * 18, 0 + 84 + si * 18));
		for (int si = 0; si < 9; ++si)
			this.addSlot(new Slot(inv, si, 0 + 8 + si * 18, 0 + 142));

		initMenu();
		if (boundBlockEntity != null) {
			lastInventoryVersion = boundBlockEntity.getInventoryVersion();
		}
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		if (boundBlockEntity != null && lastInventoryVersion != boundBlockEntity.getInventoryVersion()) {
			lastInventoryVersion = boundBlockEntity.getInventoryVersion();
			refreshGeneratedSlotsFromTool();
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return hasValidPosition
				&& AbstractContainerMenu.stillValid(access, player, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = (Slot) this.slots.get(index);
		ItemStack itemStackToOperate = slot.getItem().copy();
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE) {
				if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, this.slots.size(), true))
					return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (!this.moveItemStackTo(itemstack1, 0, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE, false)) {
				if (index < PLAYER_HOTBAR_START) {
					if (!this.moveItemStackTo(itemstack1, PLAYER_HOTBAR_START, this.slots.size(), true))
						return ItemStack.EMPTY;
				} else {
					if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false))
						return ItemStack.EMPTY;
				}
				return ItemStack.EMPTY;
			}
			if (itemstack1.getCount() == 0)
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
			if (itemstack1.getCount() == itemstack.getCount())
				return ItemStack.EMPTY;
			slot.onTake(playerIn, itemstack1);
		}

		if (index > 1 && index < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE) {
			removeEnchantment(itemStackToOperate);
		}
		return itemstack;
	}

	@Override
	protected boolean moveItemStackTo(ItemStack p_38904_, int p_38905_, int p_38906_, boolean p_38907_) {
		return MenuStackMover.moveItemStackTo(this.slots, p_38904_, p_38905_, p_38906_, p_38907_);
	}

	@Override
	public void removed(@NotNull Player playerIn) {
		super.removed(playerIn);
		if (playerIn instanceof ServerPlayer) {
			playerIn.getInventory().placeItemBackInInventory(itemHandler.getStackInSlot(1));
			itemHandler.setStackInSlot(1, ItemStack.EMPTY);
		}
	}

	private List<EnchantmentTableRules.EnchantmentLevel> getEnchantmentLevelsFromEnchantedBook(ItemStack enchantedBookItemStack) {
		List<EnchantmentTableRules.EnchantmentLevel> enchantmentOfBook = new ArrayList<>();
		for (Object2IntMap.Entry<Holder<Enchantment>> entry : EnchantmentUtils.getEnchantments(enchantedBookItemStack).entrySet()) {
			Holder<Enchantment> enchantment = EnchantmentUtils.resolveEnchantmentHolder(world, entry.getKey()).orElse(entry.getKey());
			enchantmentOfBook.add(new EnchantmentTableRules.EnchantmentLevel(enchantment, entry.getIntValue()));
		}

		return enchantmentOfBook;
	}

	public boolean checkCanPlaceEnchantedBook(ItemStack stack) {
		if (boundBlockEntity != null) {
			return boundBlockEntity.canApplyEnchantedBook(stack, mergeOptions());
		}
		var itemToEnchant = itemHandler.getStackInSlot(0);
		var itemEnchantmentsOnTool = EnchantmentUtils.getEnchantments(itemToEnchant);
		return EnchantmentTableRules.tryMergeEnchantments(
				itemEnchantmentsOnTool,
				getEnchantmentLevelsFromEnchantedBook(stack),
				this::isSameEnchantment,
				mergeOptions()
		).allowed();
	}

	private EnchantmentTableRules.MergeOptions mergeOptions() {
		return EnchantmentTableRules.MergeOptions.from(Config.snapshot());
	}

	private boolean isSameEnchantment(Holder<Enchantment> first, Holder<Enchantment> second) {
		Optional<ResourceKey<Enchantment>> firstKey = EnchantmentUtils.getEnchantmentKey(world, first);
		Optional<ResourceKey<Enchantment>> secondKey = EnchantmentUtils.getEnchantmentKey(world, second);
		if (firstKey.isPresent() && secondKey.isPresent()) {
			return firstKey.get().equals(secondKey.get());
		}
		return first.value().equals(second.value());
	}

	private void playUseSound() {
		BlockPos pos = boundBlockEntity != null ? boundBlockEntity.getBlockPos() : new BlockPos(x, y, z);
		world.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	public int currentPage = 0;
	public int totalPage = 0;

	// 存储当前工具槽中的附魔，用于进行翻页操作
	// 列表的长度为 ENCHANTED_BOOK_SLOT_SIZE 的整数倍，对于空物品的长度为 ENCHANTED_BOOK_SLOT_SIZE
	private final List<ItemStack> enchantmentsOnCurrentTool = new ArrayList<>();

	public void exportAllEnchantments() {
		if (world.isClientSide) {
			return;
		}

        ItemStack toolItemStack = itemHandler.getStackInSlot(0);
		ItemEnchantments itemEnchantments = EnchantmentUtils.getEnchantments(toolItemStack);
		if (toolItemStack.getItem() == Items.ENCHANTED_BOOK) {
			// 如果待附魔物品槽中的物品是附魔书，则直接返回
			entity.getInventory().placeItemBackInInventory(toolItemStack);
			itemHandler.setStackInSlot(0, ItemStack.EMPTY);

			playUseSound();
		} else if (!toolItemStack.isEmpty() && !itemEnchantments.isEmpty()) {
			ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
			ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);

			for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
				Holder<Enchantment> enchantment = EnchantmentUtils.resolveEnchantmentHolder(world, entry.getKey()).orElse(entry.getKey());
				int enchantmentLevel = entry.getIntValue();

				// set 方法在 level 小于等于 0 时会移除对应附魔
				mutable.set(enchantment, 0);
				enchantedBook.enchant(enchantment, enchantmentLevel);
			}

			if (!replaceToolEnchantments(toolItemStack, mutable.toImmutable())) {
				return;
			}
			entity.getInventory().placeItemBackInInventory(enchantedBook);

			playUseSound();
		}
		clearCache();
		clearPage();
	}

	public void resetPage() {
		currentPage = 0;
		totalPage = 0;
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
		itemHandler.setStackInSlot(1, ItemStack.EMPTY.copy());

		int indexOffset = currentPage * ENCHANTED_BOOK_SLOT_SIZE;
		if (totalPage > 0) {
			// 将附魔书添加到附魔书槽
			for (int i = 0; i < ENCHANTED_BOOK_SLOT_SIZE; i++) {
				int indexOfFullList = i + indexOffset;
				int indexOfSlot = i + 2;
				itemHandler.setStackInSlot(
						indexOfSlot,
						indexOfFullList < enchantmentsOnCurrentTool.size()
								? enchantmentsOnCurrentTool.get(indexOfFullList)
								: ItemStack.EMPTY
				);
			}
		}
	}

	public void clearCache() {
		for (int i = 2; i < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE; i++) {
			itemHandler.setStackInSlot(i, ItemStack.EMPTY);
		}
		genEnchantedBookCache();
	}

	public void clearPage() {
		currentPage = 0;
		totalPage = 0;
	}

	public void genEnchantedBookCache() {
		ItemStack toolItemStack = itemHandler.getStackInSlot(0);

		int currentTotalPage = 1;
		enchantmentsOnCurrentTool.clear();

		if (!toolItemStack.isEmpty()) {
			// 若待附魔物品槽不为空，则至少生成一页的附魔书槽
			ItemEnchantments enchantments = EnchantmentUtils.getEnchantments(toolItemStack);
			currentTotalPage = EnchantmentTableRules.calculatePageCount(enchantments.entrySet().size(), ENCHANTED_BOOK_SLOT_SIZE, true);

			if (toolItemStack.is(Items.ENCHANTED_BOOK) && enchantments.entrySet().size() == 1) {
				// 获取唯一附魔的附魔等级
				var enchantmentObj = enchantments.entrySet().iterator().next();
				var enchantment = EnchantmentUtils.resolveEnchantmentHolder(world, enchantmentObj.getKey()).orElse(enchantmentObj.getKey());
				var enchantmentLevel = enchantmentObj.getIntValue();
				// 如果附魔书上的唯一附魔等级大于 1，则需要拆分附魔等级
				// 如果附魔书上的唯一附魔等级等于 1，则不生成附魔书槽
				if (enchantmentLevel > 1) {
					for (Integer level : EnchantmentTableRules.splitSingleEnchantmentLevels(enchantmentLevel, mergeOptions())) {
						ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
						enchantedBook.enchant(enchantment, level);
						enchantmentsOnCurrentTool.add(enchantedBook);
					}
				}
			} else if (!toolItemStack.is(Items.ENCHANTED_BOOK) || enchantments.entrySet().size() > 1) {
				// 若待附魔工具槽中的物品不是附魔书，或者附魔词条数量大于 1，那么继续生成附魔书槽
				// 根据待附魔工具槽中的附魔生成对应的附魔书，并添加到 fullEnchantmentList
				for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
					Holder<Enchantment> enchantment = EnchantmentUtils.resolveEnchantmentHolder(world, entry.getKey()).orElse(entry.getKey());
					Integer enchantmentLevel = entry.getValue();
					ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
					enchantedBook.enchant(enchantment, enchantmentLevel);
					enchantmentsOnCurrentTool.add(enchantedBook);
				}
			}
		} else {
			// 仅当待附魔工具槽中没有物品时，将总页数设置为 0
			currentTotalPage = 0;
		}
		int totalSlots = currentTotalPage * ENCHANTED_BOOK_SLOT_SIZE;
		// 补全空附魔书槽
		while(enchantmentsOnCurrentTool.size() < totalSlots) {
			enchantmentsOnCurrentTool.add(ItemStack.EMPTY);
		}

		totalPage = currentTotalPage;
	}

	public boolean addEnchantment(ItemStack itemStack, int slotIndex) {
		return addEnchantment(itemStack, slotIndex, false);
	}

	public boolean addEnchantment(ItemStack itemStackToPut, int slotIndex, boolean forceRegenerateEnchantedBookStore) {
		if (boundBlockEntity != null) {
			if (!boundBlockEntity.tryApplyEnchantedBook(itemStackToPut, mergeOptions(), false)) {
				return false;
			}
			refreshGeneratedSlotsFromTool();
			return true;
		}

		var enchantmentInstances = getEnchantmentLevelsFromEnchantedBook(itemStackToPut);
		if (enchantmentInstances.isEmpty()) {
			return false;
		}

		ItemStack toolItemStack = itemHandler.getStackInSlot(0);
		if (toolItemStack.isEmpty()) {
			return false;
		}
		//region 将附魔应用到待附魔物品槽中的物品
		ItemEnchantments itemEnchantments = EnchantmentUtils.getEnchantments(toolItemStack);
		EnchantmentTableRules.MergeResult result = EnchantmentTableRules.tryMergeEnchantments(
				itemEnchantments,
				enchantmentInstances,
				this::isSameEnchantment,
				mergeOptions()
		);
		if (!result.allowed()) {
			return false;
		}
		if (!replaceToolEnchantments(toolItemStack, result.enchantments())) {
			return false;
		}
		// endregion

		// 新增附魔，重新生成所有附魔书缓存并更新附魔书槽
		genEnchantedBookCache();
		updateEnchantedBookSlots();

		playUseSound();
		return true;
	}

	public boolean removeEnchantment(ItemStack itemStackToRemove) {
		var enchantmentLevels = getEnchantmentLevelsFromEnchantedBook(itemStackToRemove);
		if (enchantmentLevels.isEmpty()) {
			return false;
		}

		//region 将附魔应用到待附魔物品槽中的物品
		ItemStack toolItemStack = itemHandler.getStackInSlot(0);
		if (toolItemStack.isEmpty()) {
			return false;
		}
		ItemEnchantments itemEnchantments = EnchantmentUtils.getEnchantments(toolItemStack);
		ItemEnchantments resultEnchantments = EnchantmentTableRules.subtractEnchantments(
				itemEnchantments,
				enchantmentLevels,
				this::isSameEnchantment,
				shouldUseIncrementalSingleBookSplitRemoval(toolItemStack, itemEnchantments, enchantmentLevels)
		);
		if (!replaceToolEnchantments(toolItemStack, resultEnchantments)) {
			return false;
		}
		// endregion

		int resultPageSize = EnchantmentTableRules.calculatePageCount(
				resultEnchantments.size(),
				ENCHANTED_BOOK_SLOT_SIZE,
				true
		);
		// 在以下情况重新生成附魔书槽：
		// 1. 待附魔物品本身是附魔书，并且附魔后的附魔词条数量为 1
		// 2. 物品附魔前后的页数不同
		var hasRegenerated = false;
		if (toolItemStack.is(Items.ENCHANTED_BOOK) && resultEnchantments.size() == 1
				|| totalPage != resultPageSize) {
			genEnchantedBookCache();
			currentPage = Math.max(0, Math.min(currentPage, totalPage - 1));
			hasRegenerated = true;
		}
		updateEnchantedBookSlots();

		playUseSound();
		return hasRegenerated;
	}

	private boolean shouldUseIncrementalSingleBookSplitRemoval(
			ItemStack toolItemStack,
			ItemEnchantments itemEnchantments,
			List<EnchantmentTableRules.EnchantmentLevel> removalEnchantments
	) {
		return Config.snapshot().incrementalSameLevelMerge()
				&& toolItemStack.is(Items.ENCHANTED_BOOK)
				&& itemEnchantments.size() == 1
				&& removalEnchantments.size() == 1;
	}

	private boolean replaceToolEnchantments(ItemStack toolItemStack, ItemEnchantments enchantments) {
		if (toolItemStack.isEmpty()) {
			return false;
		}
		if (boundBlockEntity != null) {
			return boundBlockEntity.replaceToolEnchantments(enchantments);
		}
		toolItemStack.set(EnchantmentHelper.getComponentType(toolItemStack), enchantments);
		return true;
	}

	public void initMenu() {
		currentPage = 0;
		refreshGeneratedSlotsFromTool();
	}

	private void refreshGeneratedSlotsFromTool() {
		for (int i = 2; i < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE; i++) {
			itemHandler.setStackInSlot(i, ItemStack.EMPTY);
		}
		genEnchantedBookCache();
		currentPage = totalPage == 0 ? 0 : Math.max(0, Math.min(currentPage, totalPage - 1));
		updateEnchantedBookSlots();
	}

	private static class EnchantingMenuItemHandler extends ItemStackHandler {
		private final EnchantingCustomTableBlockEntity blockEntity;
		private final ItemStackHandler fallbackToolHandler = new ItemStackHandler(1);
		private final ItemStackHandler virtualHandler = new ItemStackHandler(ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE - 1);

		private EnchantingMenuItemHandler(EnchantingCustomTableBlockEntity blockEntity) {
			super(ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE);
			this.blockEntity = blockEntity;
		}

		@Override
		public int getSlots() {
			return ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return slot == 0 ? persistentHandler().getStackInSlot(0) : virtualHandler.getStackInSlot(slot - 1);
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			if (slot == 0) {
				persistentHandler().setStackInSlot(0, stack);
			} else {
				virtualHandler.setStackInSlot(slot - 1, stack);
			}
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return slot == 0
					? persistentHandler().insertItem(0, stack, simulate)
					: virtualHandler.insertItem(slot - 1, stack, simulate);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return slot == 0
					? persistentHandler().extractItem(0, amount, simulate)
					: virtualHandler.extractItem(slot - 1, amount, simulate);
		}

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return true;
		}

		private IItemHandlerModifiable persistentHandler() {
			return blockEntity != null ? blockEntity.getInventory() : fallbackToolHandler;
		}
	}
}
