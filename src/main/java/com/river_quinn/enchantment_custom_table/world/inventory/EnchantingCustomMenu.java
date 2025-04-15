
package com.river_quinn.enchantment_custom_table.world.inventory;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.init.ModMenus;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class EnchantingCustomMenu extends AbstractContainerMenu {
	public static final int ENCHANTED_BOOK_SLOT_ROW_COUNT = 4;
	public static final int ENCHANTED_BOOK_SLOT_COLUMN_COUNT = 6;
	public static final int ENCHANTED_BOOK_SLOT_SIZE = ENCHANTED_BOOK_SLOT_ROW_COUNT * ENCHANTED_BOOK_SLOT_COLUMN_COUNT;
	public static final int ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE = ENCHANTED_BOOK_SLOT_SIZE + 2;
	/**
	 * index 0: 待附魔工具槽
	 * index 1: 附加槽，仅接受附魔，添加附魔书后将会立刻将附魔书的附魔添加到待附魔工具中并重新生成附魔书槽
	 * index 2-22: 附魔书槽
	 */
	private final ItemStackHandler itemHandler = new ItemStackHandler(ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE){
		@Override
		public int getStackLimit(int slot, ItemStack stack) {
			return 1;
		}
	};

	private static final Logger LOGGER = LogUtils.getLogger();
	public final static HashMap<String, Object> guistate = new HashMap<>();
	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access = ContainerLevelAccess.NULL;
	private final Map<Integer, Slot> enchantedBookSlots = new HashMap<>();
	public EnchantingCustomTableBlockEntity boundBlockEntity = null;

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
		}
		if (pos != null) {
			boundBlockEntity = (EnchantingCustomTableBlockEntity)this.world.getBlockEntity(pos);
		}

		this.addSlot(new SlotItemHandler(itemHandler, 0, 8, 8) {
			private final int slot = 0;
			private int x = EnchantingCustomMenu.this.x;
			private int y = EnchantingCustomMenu.this.y;

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
//					resetPage();
				}
			}
		});

		this.addSlot(new SlotItemHandler(itemHandler, 1, 42, 8) {
			private final int slot = 1;
			private int x = EnchantingCustomMenu.this.x;
			private int y = EnchantingCustomMenu.this.y;

			@Override
			public boolean mayPlace(ItemStack stack) {
				return Items.ENCHANTED_BOOK == stack.getItem()
						&& !getItemHandler().getStackInSlot(0).isEmpty()
						&& (Config.ignoreEnchantmentLevelLimit || checkCanPlaceEnchantedBook(stack));
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
					addEnchantment(getEnchantmentInstanceFromEnchantedBook(newStack), slot, true);
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
						private final int slot = final_enchanted_book_index + 2;
						private int x = EnchantingCustomMenu.this.x;
						private int y = EnchantingCustomMenu.this.y;

						@Override
						public boolean mayPlace(ItemStack stack) {
							return Items.ENCHANTED_BOOK == stack.getItem()
									&& !getItemHandler().getStackInSlot(0).isEmpty()
									&& (Config.ignoreEnchantmentLevelLimit || checkCanPlaceEnchantedBook(stack));
						}

						@Override
						public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
							return Pair.of(
									InventoryMenu.BLOCK_ATLAS,
									ResourceLocation.tryParse("enchantment_custom_table:item/empty_slot_book")
							);
						}

//						@Override
//						public void onQuickCraft(ItemStack newStack, ItemStack oldStack) {
//							super.onQuickCraft(newStack, oldStack);
//							if (!newStack.isEmpty() && oldStack.isEmpty()) {
//								// 合法情况下不应该存在这种状况
//								LOGGER.warn("stack 1, setByPlayer() called with newStack.isEmpty()");
//							} else if (newStack.isEmpty() && !oldStack.isEmpty()) {
//								// 移除附魔书，同步移除工具上的附魔
//								removeEnchantment(getEnchantmentInstanceFromEnchantedBook(oldStack));
//							}
//						}

						@Override
						public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
							super.setByPlayer(newStack, oldStack);
							if (!newStack.isEmpty() && oldStack.isEmpty()) {
								// 放置附魔书，同步添加工具上的附魔
								addEnchantment(getEnchantmentInstanceFromEnchantedBook(newStack), slot);
							} else if (newStack.isEmpty() && !oldStack.isEmpty()) {
								// 移除附魔书，同步移除工具上的附魔
								removeEnchantment(getEnchantmentInstanceFromEnchantedBook(oldStack));
							}
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
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
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
				if (!this.moveItemStackTo(itemstack1, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE, this.slots.size(), true))
					return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (!this.moveItemStackTo(itemstack1, 0, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE, false)) {
				if (index < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE + 27) {
					if (!this.moveItemStackTo(itemstack1, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE + 27, this.slots.size(), true))
						return ItemStack.EMPTY;
				} else {
					if (!this.moveItemStackTo(itemstack1, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE + 27, false))
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
			removeEnchantment(getEnchantmentInstanceFromEnchantedBook(itemStackToOperate));
		}
		return itemstack;
	}

	@Override
	protected boolean moveItemStackTo(ItemStack p_38904_, int p_38905_, int p_38906_, boolean p_38907_) {
		boolean flag = false;
		int i = p_38905_;
		if (p_38907_) {
			i = p_38906_ - 1;
		}
		if (p_38904_.isStackable()) {
			while (!p_38904_.isEmpty() && (p_38907_ ? i >= p_38905_ : i < p_38906_)) {
				Slot slot = this.slots.get(i);
				ItemStack itemstack = slot.getItem();
				if (slot.mayPlace(itemstack) && !itemstack.isEmpty() && ItemStack.isSameItemSameComponents(p_38904_, itemstack)) {
					int j = itemstack.getCount() + p_38904_.getCount();
					int k = slot.getMaxStackSize(itemstack);
					if (j <= k) {
						p_38904_.setCount(0);
						itemstack.setCount(j);
						slot.set(itemstack);
						flag = true;
					} else if (itemstack.getCount() < k) {
						p_38904_.shrink(k - itemstack.getCount());
						itemstack.setCount(k);
						slot.set(itemstack);
						flag = true;
					}
				}
				if (p_38907_) {
					i--;
				} else {
					i++;
				}
			}
		}
		if (!p_38904_.isEmpty()) {
			if (p_38907_) {
				i = p_38906_ - 1;
			} else {
				i = p_38905_;
			}
			while (p_38907_ ? i >= p_38905_ : i < p_38906_) {
				Slot slot1 = this.slots.get(i);
				ItemStack itemstack1 = slot1.getItem();
				if (itemstack1.isEmpty() && slot1.mayPlace(p_38904_)) {
					int l = slot1.getMaxStackSize(p_38904_);
					slot1.setByPlayer(p_38904_.split(Math.min(p_38904_.getCount(), l)));
					slot1.setChanged();
					flag = true;
					break;
				}
				if (p_38907_) {
					i--;
				} else {
					i++;
				}
			}
		}
		return flag;
	}

	@Override
	public void removed(@NotNull Player playerIn) {
		super.removed(playerIn);
		if (playerIn instanceof ServerPlayer) {
			playerIn.getInventory().placeItemBackInInventory(itemHandler.getStackInSlot(0));
		}
	}

	public List<EnchantmentInstance> getEnchantmentInstanceFromEnchantedBook(ItemStack enchantedBookItemStack) {

		DataComponentType<ItemEnchantments> componentType = EnchantmentHelper.getComponentType(enchantedBookItemStack);
		var componentMap = enchantedBookItemStack.getComponents().get(componentType);

		List<EnchantmentInstance> enchantmentOfBook = new ArrayList<>();
		for (Object2IntMap.Entry<Holder<Enchantment>> entry : componentMap.entrySet()) {
			Enchantment enchantment = entry.getKey().value();
			int enchantmentLevel = entry.getIntValue();
			enchantmentOfBook.add(new EnchantmentInstance(Holder.direct(enchantment), enchantmentLevel));
		}

		return enchantmentOfBook;
	}

	public boolean checkCanPlaceEnchantedBook(ItemStack stack) {
		var itemEnchantments = stack.get(EnchantmentHelper.getComponentType(stack));
		var itemToEnchant = itemHandler.getStackInSlot(0);
		var itemEnchantmentsOnTool = itemToEnchant.get(EnchantmentHelper.getComponentType(itemToEnchant));
		if (itemEnchantmentsOnTool == null) {
			// 待附魔物品槽中没有附魔
			return true;
		}
		for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
			Enchantment enchantment = entry.getKey().value();
			var enchantmentLevel = entry.getIntValue();
			var enchantmentLevelOnTool = itemEnchantmentsOnTool.getLevel(entry.getKey());
			var maxLevel = enchantment.getMaxLevel();
			if (enchantmentLevelOnTool + enchantmentLevel > maxLevel) {
				// 附魔等级超过最大等级
				return false;
			}
		}
		return true;
	}

	public int currentPage = 0;
	public int totalPage = 0;

	// 存储当前工具槽中的附魔，用于进行翻页操作
	// 列表的长度为 ENCHANTED_BOOK_SLOT_SIZE 的整数倍，对于空物品的长度为 ENCHANTED_BOOK_SLOT_SIZE
	private final List<ItemStack> enchantmentsOnCurrentTool = new ArrayList<>();

	public void exportAllEnchantments() {
        ItemStack toolItemStack = itemHandler.getStackInSlot(0);
		ItemEnchantments itemEnchantments = toolItemStack.get(EnchantmentHelper.getComponentType(toolItemStack));
		if (!toolItemStack.isEmpty() && itemEnchantments != null) {
			ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
			ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);

			for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
				Enchantment enchantment = entry.getKey().value();
				int enchantmentLevel = entry.getIntValue();

				var enchantmentReference = EnchantmentUtils.translateEnchantment(world, enchantment);

				assert enchantmentReference != null;
				// set 方法在 level 小于等于 0 时会移除对应附魔
				mutable.set(enchantmentReference, 0);
				enchantedBook.enchant(enchantmentReference, enchantmentLevel);
			}

			toolItemStack.set(EnchantmentHelper.getComponentType(toolItemStack), mutable.toImmutable());
			entity.getInventory().placeItemBackInInventory(enchantedBook);

			world.playSound(null, boundBlockEntity.getWorldPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
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
		int indexOffset = currentPage * ENCHANTED_BOOK_SLOT_SIZE;
		if (totalPage > 0) {
			// 将附魔书添加到附魔书槽
			for (int i = 0; i < ENCHANTED_BOOK_SLOT_SIZE; i++) {
				int indexOfFullList = i + indexOffset;
				int indexOfSlot = i + 2;
				itemHandler.setStackInSlot(indexOfSlot, enchantmentsOnCurrentTool.get(indexOfFullList));
			}
		}
	}

	public void clearCache() {
		enchantmentsOnCurrentTool.clear();
		for (int i = 2; i < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE; i++) {
			itemHandler.setStackInSlot(i, ItemStack.EMPTY);
		}
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
			ItemEnchantments enchantments = toolItemStack.get(EnchantmentHelper.getComponentType(toolItemStack));
			currentTotalPage = Math.max((int) Math.ceil((double) enchantments.entrySet().size() / ENCHANTED_BOOK_SLOT_SIZE), 1);

			if (!toolItemStack.is(Items.ENCHANTED_BOOK) || enchantments.entrySet().size() > 1) {
				// 若待附魔工具槽中的物品不是附魔书，或者附魔词条数量大于 1，那么继续生成附魔书槽
				// 根据待附魔工具槽中的附魔生成对应的附魔书，并添加到 fullEnchantmentList
				for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
					Enchantment enchantment = entry.getKey().value();
					Integer enchantmentLevel = entry.getValue();
					ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
					var enchantmentReference = EnchantmentUtils.translateEnchantment(world, enchantment);
					assert enchantmentReference != null;
					enchantedBook.enchant(enchantmentReference, enchantmentLevel);
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

	// 获取所有已注册的附魔
	public IdMap<Holder<Enchantment>> getAllRegisteredEnchantments() {
		Registry<Enchantment> fullEnchantmentList = world.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
		return fullEnchantmentList.asHolderIdMap();
	}

	public void addEnchantment(List<EnchantmentInstance> enchantmentInstances, int slotIndex) {
		addEnchantment(enchantmentInstances, slotIndex, false);
	}

	public void addEnchantment(List<EnchantmentInstance> enchantmentInstances, int slotIndex, boolean forceRegenerateEnchantedBookStore) {
		boolean regenerateEnchantedBookStore = false;

		ItemStack toolItemStack = itemHandler.getStackInSlot(0);
		ItemEnchantments enchantmentsOnTool = toolItemStack.getTagEnchantments();
		int sourceEnchantmentCount = enchantmentsOnTool.entrySet().size();

		IdMap<Holder<Enchantment>> allRegisteredEnchantments = getAllRegisteredEnchantments();
		HashMap<Integer, EnchantmentInstance> resultEnchantmentMap = new HashMap<>();

		// region 遍历待附魔物品槽中物品的附魔
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

		int resultEnchantmentsCount = resultEnchantmentMap.size();
		//endregion

		//region 将附魔应用到待附魔物品槽中的物品
		ItemEnchantments itemEnchantments = toolItemStack.get(EnchantmentHelper.getComponentType(toolItemStack));
		// 转换成可变形式
		ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
		for (EnchantmentInstance enchantmentInstance : resultEnchantmentMap.values().stream().toList()) {
			var enchantmentReference = EnchantmentUtils.translateEnchantment(world, enchantmentInstance.enchantment.value());
			assert enchantmentReference != null;
//			mutable.set(enchantmentReference, 0);
			// set 方法在 level 小于等于 0 时会移除对应附魔
			mutable.set(enchantmentReference, enchantmentInstance.level);
		}
		toolItemStack.set(EnchantmentHelper.getComponentType(toolItemStack), mutable.toImmutable());
		// endregion

		// 将新增的附魔书添加到缓存中
		if (slotIndex >1) {
			int enchantmentIndex = (slotIndex - 2) + currentPage * ENCHANTED_BOOK_SLOT_SIZE;
			enchantmentsOnCurrentTool.set(enchantmentIndex, itemHandler.getStackInSlot(slotIndex));
		}

		// 在以下情况重新生成附魔书槽：
		// 1. 放入了携带多附魔词条的附魔书
		// 2. 放入了携带相同附魔的附魔书
		// 3. 待附魔物品本身是附魔书，并且原本的附魔词条数量为 1
		// 4. 物品附魔前后的页数不同
		if (enchantmentInstances.size() > 1
				|| forceRegenerateEnchantedBookStore
				|| regenerateEnchantedBookStore
				|| (toolItemStack.is(Items.ENCHANTED_BOOK) && sourceEnchantmentCount == 1)
				|| (totalPage != (int) Math.ceil((double) resultEnchantmentsCount / ENCHANTED_BOOK_SLOT_SIZE))) {
			genEnchantedBookCache();
			updateEnchantedBookSlots();
		}

		itemHandler.setStackInSlot(1, ItemStack.EMPTY);

		world.playSound(null, boundBlockEntity.getWorldPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	public void removeEnchantment(List<EnchantmentInstance> enchantmentInstances) {

		//region 将附魔应用到待附魔物品槽中的物品
		ItemStack toolItemStack = itemHandler.getStackInSlot(0);
		ItemEnchantments itemEnchantments = toolItemStack.get(EnchantmentHelper.getComponentType(toolItemStack));
		// 转换成可变形式
		ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
		for (EnchantmentInstance enchantmentInstance : enchantmentInstances) {
			var enchantmentReference = EnchantmentUtils.translateEnchantment(world, enchantmentInstance.enchantment.value());
			assert enchantmentReference != null;
			// set 方法在 level 小于等于 0 时会移除对应附魔
			mutable.set(enchantmentReference, 0);
		}
		toolItemStack.set(EnchantmentHelper.getComponentType(toolItemStack), mutable.toImmutable());
		// endregion


		int resultPageSize = Math.max((int) Math.ceil((double) mutable.keySet().size() / ENCHANTED_BOOK_SLOT_SIZE), 1);
		// 在以下情况重新生成附魔书槽：
		// 1. 待附魔物品本身是附魔书，并且附魔后的附魔词条数量为 1
		// 2. 物品附魔前后的页数不同
		if (toolItemStack.is(Items.ENCHANTED_BOOK) && mutable.keySet().size() == 1
				|| totalPage != resultPageSize) {
			genEnchantedBookCache();
			currentPage = Math.min(currentPage, totalPage - 1);
			updateEnchantedBookSlots();
		}

		world.playSound(null, boundBlockEntity.getWorldPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	public void initMenu() {
		clearPage();
		clearCache();
		enchantmentsOnCurrentTool.clear();
	}
}
