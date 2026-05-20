package com.river_quinn.enchantment_custom_table.gametest;

import io.netty.buffer.Unpooled;
import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.EnchantmentCustomTable;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantmentConversionTableBlockEntity;
import com.river_quinn.enchantment_custom_table.init.ModBlocks;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantingCustomMenu;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(EnchantmentCustomTable.MODID)
@PrefixGameTestTemplate(false)
public class BasicGameTests {
    private static final BlockPos ENCHANTING_CUSTOM_TABLE_POS = new BlockPos(1, 1, 1);
    private static final BlockPos ENCHANTMENT_CONVERSION_TABLE_POS = new BlockPos(3, 1, 1);

    @GameTest(template = "gametest/empty", timeoutTicks = 20)
    public static void functionalBlocksCreateTheirBlockEntities(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        helper.assertBlockPresent(ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get(), ENCHANTING_CUSTOM_TABLE_POS);
        helper.assertTrue(
                helper.getBlockEntity(ENCHANTING_CUSTOM_TABLE_POS) instanceof EnchantingCustomTableBlockEntity,
                "Enchanting Custom Table should create its block entity"
        );

        helper.assertBlockPresent(ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get(), ENCHANTMENT_CONVERSION_TABLE_POS);
        helper.assertTrue(
                helper.getBlockEntity(ENCHANTMENT_CONVERSION_TABLE_POS) instanceof EnchantmentConversionTableBlockEntity,
                "Enchantment Conversion Table should create its block entity"
        );

        helper.succeed();
    }

    @GameTest(template = "gametest/empty", timeoutTicks = 40)
    public static void functionalMenusBindToPlacedBlocks(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos enchantingCustomTablePos = helper.absolutePos(ENCHANTING_CUSTOM_TABLE_POS);
        BlockPos enchantmentConversionTablePos = helper.absolutePos(ENCHANTMENT_CONVERSION_TABLE_POS);

        movePlayerTo(player, enchantingCustomTablePos);
        EnchantingCustomMenu enchantingCustomMenu = new EnchantingCustomMenu(
                1,
                player.getInventory(),
                menuData(enchantingCustomTablePos)
        );
        helper.assertTrue(
                enchantingCustomMenu.boundBlockEntity instanceof EnchantingCustomTableBlockEntity,
                "Enchanting Custom Table menu should bind to its block entity"
        );
        helper.assertTrue(
                enchantingCustomMenu.stillValid(player),
                "Enchanting Custom Table menu should be valid near its block"
        );

        movePlayerTo(player, enchantmentConversionTablePos);
        EnchantmentConversionMenu enchantmentConversionMenu = new EnchantmentConversionMenu(
                2,
                player.getInventory(),
                menuData(enchantmentConversionTablePos)
        );
        helper.assertTrue(
                enchantmentConversionMenu.boundBlockEntity instanceof EnchantmentConversionTableBlockEntity,
                "Enchantment Conversion Table menu should bind to its block entity"
        );
        helper.assertTrue(
                enchantmentConversionMenu.stillValid(player),
                "Enchantment Conversion Table menu should be valid near its block"
        );

        helper.succeed();
    }

    @GameTest(template = "gametest/empty", timeoutTicks = 40)
    public static void conversionTableConsumesConfiguredPaymentAndClearsResultsWhenExhausted(GameTestHelper helper) {
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        int originalEmeraldCost = Config.minimumEmeraldCost;
        int originalEmeraldBlockCost = Config.minimumEmeraldBlockCost;
        boolean originalConvertMaxLevelBook = Config.convertMaxLevelBook;
        Config.minimumEmeraldCost = 3;
        Config.minimumEmeraldBlockCost = 0;
        Config.convertMaxLevelBook = true;

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantmentConversionMenu menu = conversionMenu(helper, player);

            menu.getSlot(0).setByPlayer(new ItemStack(Items.BOOK));
            menu.getSlot(1).setByPlayer(new ItemStack(Items.EMERALD, 3));

            helper.assertTrue(menu.totalPage > 0, "Conversion table should generate at least one page when funded");
            helper.assertTrue(
                    menu.getSlot(2).getItem().is(Items.ENCHANTED_BOOK),
                    "Conversion table should generate selectable enchanted books"
            );
            helper.assertTrue(menu.pickEnchantedBook(), "Picking a generated book should consume configured resources");
            helper.assertTrue(menu.getSlot(0).getItem().isEmpty(), "One normal book should be consumed");
            helper.assertTrue(menu.getSlot(1).getItem().isEmpty(), "Configured emerald payment should be consumed");
            helper.assertTrue(menu.totalPage == 0, "Generated results should clear when resources are exhausted");
            helper.assertTrue(menu.getSlot(2).getItem().isEmpty(), "Generated result slots should be empty after resources run out");

            helper.succeed();
        } finally {
            Config.minimumEmeraldCost = originalEmeraldCost;
            Config.minimumEmeraldBlockCost = originalEmeraldBlockCost;
            Config.convertMaxLevelBook = originalConvertMaxLevelBook;
        }
    }

    @GameTest(template = "gametest/empty", timeoutTicks = 40)
    public static void conversionTableSearchFiltersResultsByClientMatchedIds(GameTestHelper helper) {
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        int originalEmeraldCost = Config.minimumEmeraldCost;
        int originalEmeraldBlockCost = Config.minimumEmeraldBlockCost;
        boolean originalConvertMaxLevelBook = Config.convertMaxLevelBook;
        Config.minimumEmeraldCost = 1;
        Config.minimumEmeraldBlockCost = 0;
        Config.convertMaxLevelBook = true;

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantmentConversionMenu menu = conversionMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ResourceLocation sharpnessId = enchantmentId(helper, sharpness);

            menu.getSlot(0).setByPlayer(new ItemStack(Items.BOOK, 2));
            menu.getSlot(1).setByPlayer(new ItemStack(Items.EMERALD, 2));

            menu.setSearchQuery("localized-sharpness", "zh_cn", List.of(sharpnessId));

            helper.assertTrue(menu.totalPage == 1, "Client matched ids should narrow the conversion list to one page");
            assertEnchantmentIdLevel(
                    helper,
                    menu.getSlot(2).getItem(),
                    sharpnessId,
                    sharpness.value().getMaxLevel(),
                    "Filtered conversion result should be the client-matched enchantment"
            );
            helper.assertTrue(menu.getSlot(3).getItem().isEmpty(), "Only the matched enchantment should be shown");

            menu.setSearchQuery("definitely-missing-enchantment", "en_us", List.of());

            helper.assertTrue(menu.totalPage == 0, "No-match search should clear conversion pages");
            helper.assertTrue(menu.getSlot(2).getItem().isEmpty(), "No-match search should clear visible result slots");

            helper.succeed();
        } finally {
            Config.minimumEmeraldCost = originalEmeraldCost;
            Config.minimumEmeraldBlockCost = originalEmeraldBlockCost;
            Config.convertMaxLevelBook = originalConvertMaxLevelBook;
        }
    }

    @GameTest(template = "gametest/empty", timeoutTicks = 40)
    public static void customTableSplitsSingleHighLevelBookByDesign(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(true, false);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);

            menu.getSlot(0).setByPlayer(enchantedBook(sharpness, 8));

            helper.assertTrue(menu.totalPage == 1, "High-level single-enchantment books should generate one result page");
            assertEnchantmentLevel(helper, menu.getSlot(2).getItem(), sharpness, 4, "First split book should contain half the source level");
            assertEnchantmentLevel(helper, menu.getSlot(3).getItem(), sharpness, 2, "Second split book should contain the next binary level");
            assertEnchantmentLevel(helper, menu.getSlot(4).getItem(), sharpness, 1, "Third split book should contain the final unique level");
            helper.assertTrue(menu.getSlot(5).getItem().isEmpty(), "No duplicate split book should be generated");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    @GameTest(template = "gametest/empty", timeoutTicks = 40)
    public static void customTableMergesDuplicateBookLevelsWithoutVanillaCap(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(true, false);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 4);

            menu.getSlot(0).setByPlayer(sword);
            helper.assertTrue(
                    menu.addEnchantment(enchantedBook(sharpness, 4), 1, true),
                    "Default merge mode should allow direct over-cap merges"
            );

            assertEnchantmentLevel(
                    helper,
                    menu.getSlot(0).getItem(),
                    sharpness,
                    8,
                    "Custom table should add duplicate enchantment levels instead of enforcing vanilla max level"
            );

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    @GameTest(template = "gametest/empty", timeoutTicks = 40)
    public static void customTableRejectsOvercapMergeWhenLevelLimitIsEnforced(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(false, false);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 4);
            ItemStack book = enchantedBook(sharpness, 4);

            menu.getSlot(0).setByPlayer(sword);

            helper.assertTrue(!menu.getSlot(1).mayPlace(book), "Input slot should reject over-cap duplicate books when level limits are enforced");
            helper.assertTrue(!menu.addEnchantment(book, 1, true), "Core add path should reject over-cap duplicate books when level limits are enforced");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 4, "Rejected merge should leave the tool unchanged");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    @GameTest(template = "gametest/empty", timeoutTicks = 40)
    public static void customTableIncrementalMergeAddsOneForSameLevel(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(true, true);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 5);

            menu.getSlot(0).setByPlayer(sword);

            helper.assertTrue(menu.addEnchantment(enchantedBook(sharpness, 5), 1, true), "Incremental merge should accept same-level duplicate books");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 6, "Incremental merge should add one level");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    @GameTest(template = "gametest/empty", timeoutTicks = 40)
    public static void customTableIncrementalMergeRejectsDifferentLevel(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(true, true);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 5);
            ItemStack book = enchantedBook(sharpness, 4);

            menu.getSlot(0).setByPlayer(sword);

            helper.assertTrue(!menu.getSlot(1).mayPlace(book), "Input slot should reject different-level duplicate books in incremental mode");
            helper.assertTrue(!menu.addEnchantment(book, 1, true), "Core add path should reject different-level duplicate books in incremental mode");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 5, "Rejected incremental merge should leave the tool unchanged");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    @GameTest(template = "gametest/empty", timeoutTicks = 40)
    public static void customTableIncrementalMergeObeysVanillaCapWhenLevelLimitIsEnforced(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(false, true);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 5);
            ItemStack book = enchantedBook(sharpness, 5);

            menu.getSlot(0).setByPlayer(sword);

            helper.assertTrue(!menu.getSlot(1).mayPlace(book), "Input slot should reject incremental merges above the vanilla cap when limits are enforced");
            helper.assertTrue(!menu.addEnchantment(book, 1, true), "Core add path should reject incremental merges above the vanilla cap when limits are enforced");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 5, "Rejected over-cap incremental merge should leave the tool unchanged");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    @GameTest(template = "gametest/empty", timeoutTicks = 40)
    public static void customTableIncrementalMergeAllowsNewEnchantments(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(false, true);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            Holder<Enchantment> unbreaking = enchantment(helper, Enchantments.UNBREAKING);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 5);

            menu.getSlot(0).setByPlayer(sword);

            helper.assertTrue(menu.addEnchantment(enchantedBook(unbreaking, 3), 1, true), "Incremental mode should not block new enchantments");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 5, "Existing enchantment should remain unchanged");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), unbreaking, 3, "New enchantment should be added normally");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    @GameTest(template = "gametest/empty", timeoutTicks = 40)
    public static void customTableRejectsWholeMultiEnchantmentBookWhenOneEntryIsInvalid(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(true, true);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            Holder<Enchantment> unbreaking = enchantment(helper, Enchantments.UNBREAKING);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 5);
            ItemStack book = enchantedBook(sharpness, 4);
            book.enchant(unbreaking, 3);

            menu.getSlot(0).setByPlayer(sword);

            helper.assertTrue(!menu.addEnchantment(book, 1, true), "A multi-enchantment book should be rejected atomically if any entry is invalid");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 5, "Invalid multi-book should not change existing enchantments");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), unbreaking, 0, "Invalid multi-book should not add otherwise valid new enchantments");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    @GameTest(template = "gametest/empty", timeoutTicks = 40)
    public static void customTableIncrementalModeSplitsSingleBookIntoMinusOnePair(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(true, true);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);

            menu.getSlot(0).setByPlayer(enchantedBook(sharpness, 8));

            helper.assertTrue(menu.totalPage == 1, "High-level single-enchantment books should generate one result page");
            assertEnchantmentLevel(helper, menu.getSlot(2).getItem(), sharpness, 7, "First incremental split book should be one level lower");
            assertEnchantmentLevel(helper, menu.getSlot(3).getItem(), sharpness, 7, "Second incremental split book should be one level lower");
            helper.assertTrue(menu.getSlot(4).getItem().isEmpty(), "Incremental split should only generate the matching pair");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    @GameTest(template = "gametest/empty", timeoutTicks = 40)
    public static void customTableIncrementalModeTakingSplitBookOnlyDropsSourceBookOneLevel(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(true, true);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);

            menu.getSlot(0).setByPlayer(enchantedBook(sharpness, 5));
            player.containerMenu = menu;

            menu.clicked(2, 0, ClickType.PICKUP, player);

            helper.assertTrue(player.containerMenu.getCarried().is(Items.ENCHANTED_BOOK), "Taking a split book should put that book on the cursor");
            assertEnchantmentLevel(helper, player.containerMenu.getCarried(), sharpness, 4, "Taken split book should be one level lower than the source");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 4, "Source book should only drop by one level in incremental split mode");
            assertEnchantmentLevel(helper, menu.getSlot(2).getItem(), sharpness, 3, "Generated split books should refresh from the new source level");
            assertEnchantmentLevel(helper, menu.getSlot(3).getItem(), sharpness, 3, "Generated split pair should refresh from the new source level");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    @GameTest(template = "gametest/empty", timeoutTicks = 40)
    public static void customTableRemovingGeneratedBookSubtractsFromTool(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        EnchantingCustomMenu menu = enchantingMenu(helper, player);
        Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(sharpness, 5);

        menu.getSlot(0).setByPlayer(sword);
        player.containerMenu = menu;

        menu.clicked(2, 0, ClickType.PICKUP, player);

        helper.assertTrue(player.containerMenu.getCarried().is(Items.ENCHANTED_BOOK), "Taking a generated book should put that book on the cursor");
        assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 0, "Removing a generated book should remove the matching tool enchantment");
        helper.assertTrue(menu.getSlot(2).getItem().isEmpty(), "Generated books should clear after the source enchantment is removed");

        helper.succeed();
    }

    private static FriendlyByteBuf menuData(BlockPos pos) {
        return new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(pos);
    }

    private static EnchantingCustomMenu enchantingMenu(GameTestHelper helper, Player player) {
        BlockPos pos = helper.absolutePos(ENCHANTING_CUSTOM_TABLE_POS);
        movePlayerTo(player, pos);
        return new EnchantingCustomMenu(1, player.getInventory(), menuData(pos));
    }

    private static EnchantmentConversionMenu conversionMenu(GameTestHelper helper, Player player) {
        BlockPos pos = helper.absolutePos(ENCHANTMENT_CONVERSION_TABLE_POS);
        movePlayerTo(player, pos);
        return new EnchantmentConversionMenu(2, player.getInventory(), menuData(pos));
    }

    private static Holder<Enchantment> enchantment(GameTestHelper helper, net.minecraft.resources.ResourceKey<Enchantment> key) {
        return helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
    }

    private static ResourceLocation enchantmentId(GameTestHelper helper, Holder<Enchantment> enchantment) {
        return EnchantmentUtils.getEnchantmentKey(helper.getLevel(), enchantment)
                .orElseThrow()
                .location();
    }

    private static ItemStack enchantedBook(Holder<Enchantment> enchantment, int level) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.enchant(enchantment, level);
        return book;
    }

    private static ConfigSnapshot useMergeConfig(boolean ignoreEnchantmentLevelLimit, boolean incrementalSameLevelMerge) {
        ConfigSnapshot snapshot = new ConfigSnapshot(
                Config.ignoreEnchantmentLevelLimit,
                Config.incrementalSameLevelMerge
        );
        Config.ignoreEnchantmentLevelLimit = ignoreEnchantmentLevelLimit;
        Config.incrementalSameLevelMerge = incrementalSameLevelMerge;
        return snapshot;
    }

    private record ConfigSnapshot(boolean ignoreEnchantmentLevelLimit, boolean incrementalSameLevelMerge) {
        private void restore() {
            Config.ignoreEnchantmentLevelLimit = ignoreEnchantmentLevelLimit;
            Config.incrementalSameLevelMerge = incrementalSameLevelMerge;
        }
    }

    private static void assertEnchantmentLevel(
            GameTestHelper helper,
            ItemStack stack,
            Holder<Enchantment> enchantment,
            int expectedLevel,
            String message
    ) {
        int actualLevel = EnchantmentUtils.getEnchantments(stack).getLevel(enchantment);
        helper.assertTrue(actualLevel == expectedLevel, message + " (expected " + expectedLevel + ", got " + actualLevel + ")");
    }

    private static void assertEnchantmentIdLevel(
            GameTestHelper helper,
            ItemStack stack,
            ResourceLocation enchantmentId,
            int expectedLevel,
            String message
    ) {
        int actualLevel = 0;
        for (var entry : EnchantmentUtils.getEnchantments(stack).entrySet()) {
            ResourceLocation entryId = enchantmentId(helper, entry.getKey());
            if (enchantmentId.equals(entryId)) {
                actualLevel = entry.getIntValue();
                break;
            }
        }
        helper.assertTrue(actualLevel == expectedLevel, message + " (expected " + expectedLevel + ", got " + actualLevel + ")");
    }

    private static void movePlayerTo(Player player, BlockPos pos) {
        player.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0F, 0.0F);
    }
}
