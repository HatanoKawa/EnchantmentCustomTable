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
import net.minecraft.core.*;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.*;
//? if <1.21.9 {
/*import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
*///?}
//? if >=1.21.9 {
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
//?}

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
//? if >=1.21.9 {
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
//?}
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class EnchantingCustomMenu extends AbstractContainerMenu implements EnchantingTableActions {
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
	//? if >=1.21.9 {
	private final EnchantingMenuItemHandler itemHandler;
	//?} else {
	/*private final IItemHandlerModifiable itemHandler;
	*///?}
	private final EnchantingTableSession session;

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
	//? if >=26.1 {
	public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
	//?} else {
	/*public void clicked(int slotId, int button, ClickType clickType, Player player) {
	*///?}
		// 在 1.21.2 版本及以上时，在尝试堆叠 isSameItemSameComponents 判定为 true 的附魔书时不会触发 setByPlayer 方法，
		// 因此将对于附魔书槽操作的逻辑迁移到更底层的 clicked 方法中

		// 仅额外处理部分情况，需要满足以下条件：
		// 1. 点击的槽位的下标在 2-22 之间
		// 2. 点击类型不是快速移动
		// 3. 附魔书槽对应的物品可以放置在该槽位上（主要是待附魔物品槽不能为空）
		var itemStackToPut = entity.containerMenu.getCarried();
		if (
				isGeneratedSlotIndex(slotId) &&
				//? if >=26.1 {
				clickType != ContainerInput.QUICK_MOVE &&
				//?} else {
				/*clickType != ClickType.QUICK_MOVE &&
				*///?}
				itemStackToPut.isEmpty() &&
				getSlot(slotId).getItem().isEmpty()
		) {
			return;
		}
		if (
				isGeneratedSlotIndex(slotId) &&
				//? if >=26.1 {
				clickType != ContainerInput.QUICK_MOVE &&
				//?} else {
				/*clickType != ClickType.QUICK_MOVE &&
				*///?}
				(itemStackToPut.isEmpty() || getSlot(slotId).mayPlace(entity.containerMenu.getCarried()))
		) {
			var itemStackToReplace = itemHandler.getStackInSlot(slotId);
			if (!itemStackToPut.isEmpty() && !itemStackToReplace.isEmpty()) {
				// 当尝试替换附魔书槽的附魔书时，存在以下两种情况：
				// 1. 新旧附魔书没有重复的附魔，此时去除工具上的旧附魔，添加新的附魔，返回旧的附魔书
				// 2. 新旧附魔书有重复的附魔，此时直接添加新的附魔书的附魔到工具上，不返回附魔书
				// 此段逻辑用于处理第二种情况

				// 新的物品槽对应的附魔书可能同时有多种附魔
				var enchantmentsOnNewStack = EnchantmentUtils.getEnchantmentLevels(world, itemStackToPut);
				// 旧的物品槽对应的附魔书最多只有一种附魔
				var enchantmentsOnOldStack = EnchantmentUtils.getEnchantmentLevels(world, itemStackToReplace);
				if (enchantmentsOnOldStack.isEmpty()) {
					super.clicked(slotId, button, clickType, player);
					return;
				}
				var enchantmentOnOldStack = enchantmentsOnOldStack.get(0);
				var hasDuplicateEnchantment = EnchantmentTableRules.containsMatchingEnchantment(
						enchantmentsOnNewStack,
						enchantmentOnOldStack.key()
				);
				if (hasDuplicateEnchantment) {
					// 如果新旧物品槽的对应的附魔书有重复的附魔，则直接添加到工具上，合并附魔并不返回旧的附魔书
					addEnchantment(itemStackToPut, slotId, true);
					entity.containerMenu.setCarried(ItemStack.EMPTY.copy());
					return;
				}
			}

			int enchantmentIndexInCache = session.cacheIndexForGeneratedSlot(slotId);

			// 以下逻辑用于处理第一种情况
			if (!itemStackToReplace.isEmpty()) {
				// 移除旧的槽位对应附魔书的附魔
				var removalResult = removeGeneratedBook(itemStackToReplace, enchantmentIndexInCache);
				if (!removalResult.success()) {
					updateEnchantedBookSlots();
					return;
				}
				entity.containerMenu.setCarried(itemStackToReplace.copy());
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
		//? if >=1.21.9 {
		this.itemHandler = new EnchantingMenuItemHandler(boundBlockEntity);
		LogicalInventory logicalInventory = new ResourceHandlerLogicalInventory(itemHandler);
		//?} else {
		/*EnchantingMenuInventory logicalInventory = new EnchantingMenuInventory(boundBlockEntity);
		this.itemHandler = new LogicalInventoryItemHandler(logicalInventory);
		*///?}
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

		this.addSlot(
			//? if >=1.21.9 {
			new ResourceHandlerSlot(itemHandler, itemHandler::set, 0, TableMenuLayout.Enchanting.TOOL_SLOT_X, TableMenuLayout.Enchanting.TOOL_SLOT_Y) {
			//?} else {
			/*new SlotItemHandler(itemHandler, 0, TableMenuLayout.Enchanting.TOOL_SLOT_X, TableMenuLayout.Enchanting.TOOL_SLOT_Y) {
			*///?}
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
					session.setCurrentPage(0);
					syncPageState();
					updateEnchantedBookSlots();
				} else {
					// 取出待附魔工具，清空附魔书槽
					clearCache();
					clearPage();
				}
			}
		});

		this.addSlot(
			//? if >=1.21.9 {
			new ResourceHandlerSlot(itemHandler, itemHandler::set, 1, TableMenuLayout.Enchanting.INPUT_SLOT_X, TableMenuLayout.Enchanting.INPUT_SLOT_Y) {
			//?} else {
			/*new SlotItemHandler(itemHandler, 1, TableMenuLayout.Enchanting.INPUT_SLOT_X, TableMenuLayout.Enchanting.INPUT_SLOT_Y) {
			*///?}
			@Override
			public boolean mayPlace(ItemStack stack) {
				return Items.ENCHANTED_BOOK == stack.getItem()
						&& !itemHandler.getStackInSlot(0).isEmpty()
						&& checkCanPlaceEnchantedBook(stack);
			}

			@Override
			//? if >=1.21.11 {
			public Identifier getNoItemIcon() {
				return Identifier.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
			}
			//?} else if >=1.21.4 {
			/*public ResourceLocation getNoItemIcon() {
				return ResourceLocation.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
			}
			*///?} else {
			/*public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(
						InventoryMenu.BLOCK_ATLAS,
						ResourceLocation.tryParse("enchantment_custom_table:item/empty_slot_book")
				);
			}
			*///?}

			@Override
			public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
				super.setByPlayer(newStack, oldStack);
				if (!newStack.isEmpty()) {
					// 放置附魔书，同步添加工具上的附魔，并删除附加槽的附魔书，重新生成附魔书槽
					addEnchantment(newStack, 1, true);
					getItem();
				} else {
					// 合法情况下不应该存在这种状况
					LOGGER.warn("stack 1, setByPlayer() called with newStack.isEmpty()");
				}
			}
		});

		int enchanted_book_index = 0;
		for (int row = 0; row < ENCHANTED_BOOK_SLOT_ROW_COUNT; row++) {
			int yPos = TableMenuLayout.Enchanting.generatedSlotY(row);
			for (int col = 0; col < ENCHANTED_BOOK_SLOT_COLUMN_COUNT; col++) {
				int xPos = TableMenuLayout.Enchanting.generatedSlotX(col);
				int final_enchanted_book_index = enchanted_book_index;
				this.enchantedBookSlots.put(final_enchanted_book_index, this.addSlot(
					//? if >=1.21.9 {
					new ResourceHandlerSlot(itemHandler, itemHandler::set, final_enchanted_book_index + 2, xPos, yPos) {
					//?} else {
					/*new SlotItemHandler(itemHandler, final_enchanted_book_index + 2, xPos, yPos) {
					*///?}
						@Override
						public boolean mayPlace(ItemStack stack) {
							return Items.ENCHANTED_BOOK == stack.getItem()
									&& !itemHandler.getStackInSlot(0).isEmpty()
									&& checkCanPlaceEnchantedBook(stack);
						}

						@Override
						public boolean mayPickup(Player player) {
							return !getItem().isEmpty();
						}

						@Override
						//? if >=1.21.11 {
						public Identifier getNoItemIcon() {
							return Identifier.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
						}
						//?} else if >=1.21.4 {
						/*public ResourceLocation getNoItemIcon() {
							return ResourceLocation.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
						}
						*///?} else {
						/*public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
							return Pair.of(
									InventoryMenu.BLOCK_ATLAS,
									ResourceLocation.tryParse("enchantment_custom_table:item/empty_slot_book")
							);
						}
						*///?}

						@Override
						public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
							super.setByPlayer(newStack, oldStack);
							if (!newStack.isEmpty()) {
								addEnchantment(newStack, final_enchanted_book_index + 2);
								getItem();
							}
						}

					}
				));
				enchanted_book_index++;
			}
		}

		for (int si = 0; si < 3; ++si)
			for (int sj = 0; sj < 9; ++sj)
				this.addSlot(new Slot(inv, sj + (si + 1) * 9, TableMenuLayout.Enchanting.PLAYER_INVENTORY_X + sj * TableMenuLayout.SLOT_SIZE, TableMenuLayout.Enchanting.PLAYER_INVENTORY_Y + si * TableMenuLayout.SLOT_SIZE));
		for (int si = 0; si < 9; ++si)
			this.addSlot(new Slot(inv, si, TableMenuLayout.Enchanting.PLAYER_INVENTORY_X + si * TableMenuLayout.SLOT_SIZE, TableMenuLayout.Enchanting.PLAYER_INVENTORY_Y + 58));

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
		int enchantmentIndexInCache = isGeneratedSlotIndex(index) ? session.cacheIndexForGeneratedSlot(index) : -1;
		if (isGeneratedSlotIndex(index)
				&& (itemStackToOperate.isEmpty() || !session.isGeneratedItemAt(enchantmentIndexInCache, itemStackToOperate))) {
			return ItemStack.EMPTY;
		}
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

		if (isGeneratedSlotIndex(index)) {
			removeGeneratedBook(itemStackToOperate, enchantmentIndexInCache);
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

	// 保存当前页的附魔，设置新页面并更新附魔书槽
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
		playUseSound();
		return true;
	}

	public boolean removeEnchantment(ItemStack itemStackToRemove) {
		return removeGeneratedBook(itemStackToRemove).regenerated();
	}

	private EnchantingTableSession.GeneratedBookRemovalResult removeGeneratedBook(ItemStack itemStackToRemove) {
		return removeGeneratedBook(itemStackToRemove, -1);
	}

	private EnchantingTableSession.GeneratedBookRemovalResult removeGeneratedBook(ItemStack itemStackToRemove, int cacheIndex) {
		EnchantingTableSession.GeneratedBookRemovalResult result = cacheIndex >= 0
				? session.removeGeneratedBookAtCacheIndex(itemStackToRemove, cacheIndex)
				: session.removeGeneratedBook(itemStackToRemove);
		syncPageState();
		if (result.success()) {
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

	private boolean isGeneratedSlotIndex(int slotId) {
		return slotId >= 2 && slotId < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE;
	}

	//? if >=1.21.9 {
	private static class EnchantingMenuItemHandler extends MenuItemStackHandler {
		private final EnchantingCustomTableBlockEntity blockEntity;
		private final MenuItemStackHandler fallbackToolHandler = new MenuItemStackHandler(1, 1);

		private EnchantingMenuItemHandler(EnchantingCustomTableBlockEntity blockEntity) {
			super(ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE, 1);
			this.blockEntity = blockEntity;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return slot == 0 ? persistentHandler().getStackInSlot(0) : super.getStackInSlot(slot);
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			if (slot == 0) {
				persistentHandler().setStackInSlot(0, stack);
			} else {
				super.setStackInSlot(slot, stack);
			}
		}

		@Override
		public void set(int index, ItemResource resource, int amount) {
			if (index == 0) {
				persistentHandler().setStackInSlot(0, resource.toStack(amount));
			} else {
				super.set(index, resource, amount);
			}
		}

		@Override
		public ItemResource getResource(int index) {
			return index == 0 ? ItemResource.of(getStackInSlot(index)) : super.getResource(index);
		}

		@Override
		public long getAmountAsLong(int index) {
			return index == 0 ? getStackInSlot(index).getCount() : super.getAmountAsLong(index);
		}

		@Override
		public boolean isValid(int index, ItemResource resource) {
			return index != 0 || persistentHandler().isItemValid(0, resource.toStack());
		}

		@Override
		public long getCapacityAsLong(int index, ItemResource resource) {
			return index == 0 ? 1 : super.getCapacityAsLong(index, resource);
		}

		@Override
		public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
			if (index != 0) {
				return super.insert(index, resource, amount, transaction);
			}

			ItemStack current = getStackInSlot(index);
			if (resource.isEmpty() || (!current.isEmpty() && !resource.matches(current)) || !isValid(index, resource)) {
				return 0;
			}
			return Math.min(amount, Math.max(0, 1 - current.getCount()));
		}

		@Override
		public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
			if (index != 0) {
				return super.extract(index, resource, amount, transaction);
			}

			ItemStack current = getStackInSlot(index);
			if (resource.isEmpty() || !resource.matches(current)) {
				return 0;
			}
			return Math.min(amount, current.getCount());
		}

		private MenuItemStackHandler persistentHandler() {
			return blockEntity != null ? blockEntity.getInventory() : fallbackToolHandler;
		}
	}
	//?} else {
	/*private static class EnchantingMenuInventory implements LogicalInventory {
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
	*///?}
}
