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
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.function.Consumer;

@EventBusSubscriber(modid = EnchantmentCustomTable.MODID)
public class BasicGameTests {
    private static final BlockPos ENCHANTING_CUSTOM_TABLE_POS = new BlockPos(1, 1, 1);
    private static final BlockPos ENCHANTMENT_CONVERSION_TABLE_POS = new BlockPos(3, 1, 1);
    private static final Identifier EMPTY_TEMPLATE =
            Identifier.fromNamespaceAndPath(EnchantmentCustomTable.MODID, "gametest/empty");
    private static final List<TestRegistration> TESTS = List.of(
            new TestRegistration("functional_blocks_create_their_block_entities", 20, BasicGameTests::functionalBlocksCreateTheirBlockEntities),
            new TestRegistration("functional_menus_bind_to_placed_blocks", 40, BasicGameTests::functionalMenusBindToPlacedBlocks),
            new TestRegistration("conversion_table_consumes_configured_payment_and_clears_results_when_exhausted", 40, BasicGameTests::conversionTableConsumesConfiguredPaymentAndClearsResultsWhenExhausted),
            new TestRegistration("conversion_table_search_filters_results_by_client_matched_ids", 40, BasicGameTests::conversionTableSearchFiltersResultsByClientMatchedIds),
            new TestRegistration("conversion_table_can_be_limited_to_level_one_books", 40, BasicGameTests::conversionTableCanBeLimitedToLevelOneBooks),
            new TestRegistration("conversion_table_refills_taken_result_slot_after_pick", 40, BasicGameTests::conversionTableRefillsTakenResultSlotAfterPick),
            new TestRegistration("conversion_table_preserves_search_filter_after_pick", 40, BasicGameTests::conversionTablePreservesSearchFilterAfterPick),
            new TestRegistration("conversion_table_copy_mode_generates_result_and_disables_candidates", 40, BasicGameTests::conversionTableCopyModeGeneratesResultAndDisablesCandidates),
            new TestRegistration("conversion_table_rejects_invalid_copy_templates", 40, BasicGameTests::conversionTableRejectsInvalidCopyTemplates),
            new TestRegistration("conversion_table_automation_inputs_materials_and_extracts_copies_only", 40, BasicGameTests::conversionTableAutomationInputsMaterialsAndExtractsCopiesOnly),
            new TestRegistration("custom_table_splits_single_high_level_book_by_design", 40, BasicGameTests::customTableSplitsSingleHighLevelBookByDesign),
            new TestRegistration("custom_table_merges_duplicate_book_levels_without_vanilla_cap", 40, BasicGameTests::customTableMergesDuplicateBookLevelsWithoutVanillaCap),
            new TestRegistration("custom_table_rejects_overcap_merge_when_level_limit_is_enforced", 40, BasicGameTests::customTableRejectsOvercapMergeWhenLevelLimitIsEnforced),
            new TestRegistration("custom_table_level_limit_allows_new_overcap_enchantments", 40, BasicGameTests::customTableLevelLimitAllowsNewOvercapEnchantments),
            new TestRegistration("custom_table_incremental_merge_adds_one_for_same_level", 40, BasicGameTests::customTableIncrementalMergeAddsOneForSameLevel),
            new TestRegistration("custom_table_incremental_merge_rejects_different_level", 40, BasicGameTests::customTableIncrementalMergeRejectsDifferentLevel),
            new TestRegistration("custom_table_incremental_merge_obeys_vanilla_cap_when_level_limit_is_enforced", 40, BasicGameTests::customTableIncrementalMergeObeysVanillaCapWhenLevelLimitIsEnforced),
            new TestRegistration("custom_table_incremental_merge_allows_new_enchantments", 40, BasicGameTests::customTableIncrementalMergeAllowsNewEnchantments),
            new TestRegistration("custom_table_rejects_whole_multi_enchantment_book_when_one_entry_is_invalid", 40, BasicGameTests::customTableRejectsWholeMultiEnchantmentBookWhenOneEntryIsInvalid),
            new TestRegistration("custom_table_incremental_mode_splits_single_book_into_minus_one_pair", 40, BasicGameTests::customTableIncrementalModeSplitsSingleBookIntoMinusOnePair),
            new TestRegistration("custom_table_incremental_mode_taking_split_book_only_drops_source_book_one_level", 40, BasicGameTests::customTableIncrementalModeTakingSplitBookOnlyDropsSourceBookOneLevel),
            new TestRegistration("custom_table_quick_move_book_into_input_updates_tool", 40, BasicGameTests::customTableQuickMoveBookIntoInputUpdatesTool),
            new TestRegistration("custom_table_drag_insert_book_into_generated_slot_updates_tool", 40, BasicGameTests::customTableDragInsertBookIntoGeneratedSlotUpdatesTool),
            new TestRegistration("custom_table_keeps_tool_in_block_storage_after_menu_close", 40, BasicGameTests::customTableKeepsToolInBlockStorageAfterMenuClose),
            new TestRegistration("custom_table_automation_applies_accepted_book", 40, BasicGameTests::customTableAutomationAppliesAcceptedBook),
            new TestRegistration("custom_table_automation_rejects_invalid_book", 40, BasicGameTests::customTableAutomationRejectsInvalidBook),
            new TestRegistration("custom_table_removing_generated_book_subtracts_from_tool", 40, BasicGameTests::customTableRemovingGeneratedBookSubtractsFromTool),
            new TestRegistration("custom_table_quick_move_generated_book_subtracts_from_tool", 40, BasicGameTests::customTableQuickMoveGeneratedBookSubtractsFromTool)
    );

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        if (!isGameTestServerLaunch()) {
            return;
        }

        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Identifier.fromNamespaceAndPath(EnchantmentCustomTable.MODID, "default")
        );

        for (TestRegistration test : TESTS) {
            registerTest(event, environment, test);
        }
    }

    private static void registerTest(
            RegisterGameTestsEvent event,
            Holder<TestEnvironmentDefinition<?>> environment,
            TestRegistration test
    ) {
        event.registerTest(
                testId(test.name()),
                new DirectGameTestInstance(
                        new TestData<>(environment, EMPTY_TEMPLATE, test.timeoutTicks(), 0, true),
                        test.function()
                )
        );
    }

    private static boolean isGameTestServerLaunch() {
        Boolean serverModLoaderResult = invokeBoolean(
                "net.neoforged.neoforge.server.loading.ServerModLoader",
                "isGameTestServer"
        );
        if (serverModLoaderResult != null) {
            return serverModLoaderResult;
        }

        return Boolean.TRUE.equals(invokeBoolean(
                "net.neoforged.neoforge.gametest.GameTestHooks",
                "isGametestServer"
        ));
    }

    private static Boolean invokeBoolean(String className, String methodName) {
        try {
            Object result = Class.forName(className, false, BasicGameTests.class.getClassLoader())
                    .getMethod(methodName)
                    .invoke(null);
            return result instanceof Boolean value ? value : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static Identifier testId(String name) {
        return Identifier.fromNamespaceAndPath(EnchantmentCustomTable.MODID, name);
    }

    private record TestRegistration(String name, int timeoutTicks, Consumer<GameTestHelper> function) {
    }

    private static final class DirectGameTestInstance extends GameTestInstance {
        private final Consumer<GameTestHelper> function;

        private DirectGameTestInstance(TestData<Holder<TestEnvironmentDefinition<?>>> testData, Consumer<GameTestHelper> function) {
            super(testData);
            this.function = function;
        }

        @Override
        public void run(GameTestHelper helper) {
            function.accept(helper);
        }

        @Override
        public MapCodec<? extends GameTestInstance> codec() {
            return FunctionGameTestInstance.CODEC;
        }

        @Override
        protected MutableComponent typeDescription() {
            return Component.literal("direct");
        }
    }

    public static void functionalBlocksCreateTheirBlockEntities(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        helper.assertBlockPresent(ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get(), ENCHANTING_CUSTOM_TABLE_POS);
        assertTrue(helper,
                helper.getBlockEntity(ENCHANTING_CUSTOM_TABLE_POS, EnchantingCustomTableBlockEntity.class) != null,
                "Enchanting Custom Table should create its block entity"
        );

        helper.assertBlockPresent(ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get(), ENCHANTMENT_CONVERSION_TABLE_POS);
        assertTrue(helper,
                helper.getBlockEntity(ENCHANTMENT_CONVERSION_TABLE_POS, EnchantmentConversionTableBlockEntity.class) != null,
                "Enchantment Conversion Table should create its block entity"
        );

        helper.succeed();
    }
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
        assertTrue(helper,
                enchantingCustomMenu.boundBlockEntity instanceof EnchantingCustomTableBlockEntity,
                "Enchanting Custom Table menu should bind to its block entity"
        );
        assertTrue(helper,
                enchantingCustomMenu.stillValid(player),
                "Enchanting Custom Table menu should be valid near its block"
        );

        movePlayerTo(player, enchantmentConversionTablePos);
        EnchantmentConversionMenu enchantmentConversionMenu = new EnchantmentConversionMenu(
                2,
                player.getInventory(),
                menuData(enchantmentConversionTablePos)
        );
        assertTrue(helper,
                enchantmentConversionMenu.boundBlockEntity instanceof EnchantmentConversionTableBlockEntity,
                "Enchantment Conversion Table menu should bind to its block entity"
        );
        assertTrue(helper,
                enchantmentConversionMenu.stillValid(player),
                "Enchantment Conversion Table menu should be valid near its block"
        );

        helper.succeed();
    }
    public static void conversionTableConsumesConfiguredPaymentAndClearsResultsWhenExhausted(GameTestHelper helper) {
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        int originalEmeraldCost = Config.minimumEmeraldCost;
        int originalEmeraldBlockCost = Config.minimumEmeraldBlockCost;
        boolean originalConvertOnlyLevelOneBook = Config.convertOnlyLevelOneBook;
        Config.minimumEmeraldCost = 3;
        Config.minimumEmeraldBlockCost = 0;
        Config.convertOnlyLevelOneBook = false;

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantmentConversionMenu menu = conversionMenu(helper, player);

            menu.getSlot(0).setByPlayer(new ItemStack(Items.BOOK));
            menu.getSlot(1).setByPlayer(new ItemStack(Items.EMERALD, 3));

            assertTrue(helper, menu.totalPage > 0, "Conversion table should generate at least one page when funded");
            assertTrue(helper,
                    menu.getSlot(2).getItem().is(Items.ENCHANTED_BOOK),
                    "Conversion table should generate selectable enchanted books"
            );
            assertTrue(helper, menu.pickEnchantedBook(), "Picking a generated book should consume configured resources");
            assertTrue(helper, menu.getSlot(0).getItem().isEmpty(), "One normal book should be consumed");
            assertTrue(helper, menu.getSlot(1).getItem().isEmpty(), "Configured emerald payment should be consumed");
            assertTrue(helper, menu.totalPage == 0, "Generated results should clear when resources are exhausted");
            assertTrue(helper, menu.getSlot(2).getItem().isEmpty(), "Generated result slots should be empty after resources run out");

            helper.succeed();
        } finally {
            Config.minimumEmeraldCost = originalEmeraldCost;
            Config.minimumEmeraldBlockCost = originalEmeraldBlockCost;
            Config.convertOnlyLevelOneBook = originalConvertOnlyLevelOneBook;
        }
    }
    public static void conversionTableSearchFiltersResultsByClientMatchedIds(GameTestHelper helper) {
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        int originalEmeraldCost = Config.minimumEmeraldCost;
        int originalEmeraldBlockCost = Config.minimumEmeraldBlockCost;
        boolean originalConvertOnlyLevelOneBook = Config.convertOnlyLevelOneBook;
        Config.minimumEmeraldCost = 1;
        Config.minimumEmeraldBlockCost = 0;
        Config.convertOnlyLevelOneBook = false;

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantmentConversionMenu menu = conversionMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            Identifier sharpnessId = enchantmentId(helper, sharpness);

            menu.getSlot(0).setByPlayer(new ItemStack(Items.BOOK, 2));
            menu.getSlot(1).setByPlayer(new ItemStack(Items.EMERALD, 2));

            menu.setSearchQuery("localized-sharpness", "zh_cn", List.of(sharpnessId));

            assertTrue(helper, menu.totalPage == 1, "Client matched ids should narrow the conversion list to one page");
            assertEnchantmentIdLevel(
                    helper,
                    menu.getSlot(2).getItem(),
                    sharpnessId,
                    sharpness.value().getMaxLevel(),
                    "Filtered conversion result should be the client-matched enchantment"
            );
            assertTrue(helper, menu.getSlot(3).getItem().isEmpty(), "Only the matched enchantment should be shown");

            menu.setSearchQuery("definitely-missing-enchantment", "en_us", List.of());

            assertTrue(helper, menu.totalPage == 0, "No-match search should clear conversion pages");
            assertTrue(helper, menu.getSlot(2).getItem().isEmpty(), "No-match search should clear visible result slots");

            helper.succeed();
        } finally {
            Config.minimumEmeraldCost = originalEmeraldCost;
            Config.minimumEmeraldBlockCost = originalEmeraldBlockCost;
            Config.convertOnlyLevelOneBook = originalConvertOnlyLevelOneBook;
        }
    }

    public static void conversionTableCanBeLimitedToLevelOneBooks(GameTestHelper helper) {
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        int originalEmeraldCost = Config.minimumEmeraldCost;
        int originalEmeraldBlockCost = Config.minimumEmeraldBlockCost;
        boolean originalConvertOnlyLevelOneBook = Config.convertOnlyLevelOneBook;
        Config.minimumEmeraldCost = 1;
        Config.minimumEmeraldBlockCost = 0;
        Config.convertOnlyLevelOneBook = true;

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantmentConversionMenu menu = conversionMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            Identifier sharpnessId = enchantmentId(helper, sharpness);

            menu.getSlot(0).setByPlayer(new ItemStack(Items.BOOK));
            menu.getSlot(1).setByPlayer(new ItemStack(Items.EMERALD));
            menu.setSearchQuery("localized-sharpness", "zh_cn", List.of(sharpnessId));

            assertEnchantmentIdLevel(
                    helper,
                    menu.getSlot(2).getItem(),
                    sharpnessId,
                    1,
                    "Level-one conversion mode should generate level-one enchanted books"
            );

            helper.succeed();
        } finally {
            Config.minimumEmeraldCost = originalEmeraldCost;
            Config.minimumEmeraldBlockCost = originalEmeraldBlockCost;
            Config.convertOnlyLevelOneBook = originalConvertOnlyLevelOneBook;
        }
    }
    public static void conversionTableRefillsTakenResultSlotAfterPick(GameTestHelper helper) {
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        int originalEmeraldCost = Config.minimumEmeraldCost;
        int originalEmeraldBlockCost = Config.minimumEmeraldBlockCost;
        boolean originalConvertOnlyLevelOneBook = Config.convertOnlyLevelOneBook;
        Config.minimumEmeraldCost = 0;
        Config.minimumEmeraldBlockCost = 1;
        Config.convertOnlyLevelOneBook = false;

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantmentConversionMenu menu = conversionMenu(helper, player);

            menu.getSlot(0).setByPlayer(new ItemStack(Items.BOOK, 64));
            menu.getSlot(1).setByPlayer(new ItemStack(Items.EMERALD_BLOCK, 64));

            assertTrue(helper, menu.totalPage > 0, "Funded conversion table should generate result pages");
            assertTrue(helper, menu.getSlot(2).getItem().is(Items.ENCHANTED_BOOK), "First result slot should start filled");
            assertTrue(helper, menu.getSlot(3).getItem().is(Items.ENCHANTED_BOOK), "Second result slot should start filled");

            player.containerMenu = menu;
            menu.clicked(2, 0, ContainerInput.PICKUP, player);
            ItemStack taken = menu.getCarried();

            assertTrue(helper, taken.is(Items.ENCHANTED_BOOK), "Taking a result slot should return an enchanted book");
            assertTrue(helper, menu.getSlot(0).getItem().getCount() == 63, "Taking a result should consume one normal book");
            assertTrue(helper, menu.getSlot(1).getItem().getCount() == 63, "Taking a result should consume one emerald block");
            assertTrue(helper, menu.getSlot(2).getItem().is(Items.ENCHANTED_BOOK), "Taken result slot should be refilled immediately");
            assertTrue(helper, menu.getSlot(3).getItem().is(Items.ENCHANTED_BOOK), "Other visible result slots should remain filled");

            helper.succeed();
        } finally {
            Config.minimumEmeraldCost = originalEmeraldCost;
            Config.minimumEmeraldBlockCost = originalEmeraldBlockCost;
            Config.convertOnlyLevelOneBook = originalConvertOnlyLevelOneBook;
        }
    }

    public static void conversionTablePreservesSearchFilterAfterPick(GameTestHelper helper) {
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        int originalEmeraldCost = Config.minimumEmeraldCost;
        int originalEmeraldBlockCost = Config.minimumEmeraldBlockCost;
        boolean originalConvertOnlyLevelOneBook = Config.convertOnlyLevelOneBook;
        Config.minimumEmeraldCost = 0;
        Config.minimumEmeraldBlockCost = 1;
        Config.convertOnlyLevelOneBook = false;

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantmentConversionMenu menu = conversionMenu(helper, player);
            Holder<Enchantment> depthStrider = enchantment(helper, Enchantments.DEPTH_STRIDER);
            Identifier depthStriderId = enchantmentId(helper, depthStrider);

            menu.getSlot(0).setByPlayer(new ItemStack(Items.BOOK, 64));
            menu.getSlot(1).setByPlayer(new ItemStack(Items.EMERALD_BLOCK, 64));
            menu.setSearchQuery("深海", "zh_cn", List.of(depthStriderId));

            assertTrue(helper, menu.totalPage == 1, "Single search match should keep one conversion page");
            assertEnchantmentIdLevel(
                    helper,
                    menu.getSlot(2).getItem(),
                    depthStriderId,
                    depthStrider.value().getMaxLevel(),
                    "Filtered conversion result should start as the client-matched enchantment"
            );
            assertTrue(helper, menu.getSlot(3).getItem().isEmpty(), "Search-filtered conversion results should not show unrelated enchantments");

            player.containerMenu = menu;
            menu.clicked(2, 0, ContainerInput.PICKUP, player);
            ItemStack taken = menu.getCarried();

            assertTrue(helper, taken.is(Items.ENCHANTED_BOOK), "Taking a filtered result should return an enchanted book");
            assertTrue(helper, menu.getSlot(0).getItem().getCount() == 63, "Taking a filtered result should consume one normal book");
            assertTrue(helper, menu.getSlot(1).getItem().getCount() == 63, "Taking a filtered result should consume one emerald block");
            assertTrue(helper, menu.totalPage == 1, "Taking a filtered result should preserve the search page count");
            assertEnchantmentIdLevel(
                    helper,
                    menu.getSlot(2).getItem(),
                    depthStriderId,
                    depthStrider.value().getMaxLevel(),
                    "Taking a filtered result should refill with the same filtered enchantment"
            );
            assertTrue(helper, menu.getSlot(3).getItem().isEmpty(), "Taking a filtered result should not repopulate unrelated enchantments");

            helper.succeed();
        } finally {
            Config.minimumEmeraldCost = originalEmeraldCost;
            Config.minimumEmeraldBlockCost = originalEmeraldBlockCost;
            Config.convertOnlyLevelOneBook = originalConvertOnlyLevelOneBook;
        }
    }

    public static void conversionTableCopyModeGeneratesResultAndDisablesCandidates(GameTestHelper helper) {
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        int originalEmeraldCost = Config.minimumEmeraldCost;
        int originalEmeraldBlockCost = Config.minimumEmeraldBlockCost;
        Config.minimumEmeraldCost = 1;
        Config.minimumEmeraldBlockCost = 0;

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantmentConversionMenu menu = conversionMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);

            menu.getSlot(0).setByPlayer(new ItemStack(Items.BOOK, 2));
            menu.getSlot(1).setByPlayer(new ItemStack(Items.EMERALD, 2));
            menu.getSlot(EnchantmentConversionMenu.TEMPLATE_BOOK_SLOT).setByPlayer(enchantedBook(sharpness, 3));

            assertTrue(helper, menu.totalPage == 0, "Copy mode should disable normal conversion pages");
            assertTrue(helper, menu.getSlot(2).getItem().isEmpty(), "Copy mode should clear selectable candidate slots");
            assertTrue(helper, menu.getSlot(0).getItem().getCount() == 1, "Copy mode should consume one normal book for the first copy");
            assertTrue(helper, menu.getSlot(1).getItem().getCount() == 1, "Copy mode should consume one emerald for the first copy");
            assertEnchantmentLevel(
                    helper,
                    menu.getSlot(EnchantmentConversionMenu.COPY_RESULT_SLOT).getItem(),
                    sharpness,
                    3,
                    "Copy result should match the template book"
            );

            menu.getSlot(EnchantmentConversionMenu.COPY_RESULT_SLOT).remove(1);
            menu.getSlot(EnchantmentConversionMenu.COPY_RESULT_SLOT).onTake(player, ItemStack.EMPTY);

            assertEnchantmentLevel(
                    helper,
                    menu.getSlot(EnchantmentConversionMenu.COPY_RESULT_SLOT).getItem(),
                    sharpness,
                    3,
                    "Taking a copy should generate the next copy while materials remain"
            );
            assertTrue(helper, menu.getSlot(0).getItem().isEmpty(), "Second copy should consume the final normal book");
            assertTrue(helper, menu.getSlot(1).getItem().isEmpty(), "Second copy should consume the final emerald");

            helper.succeed();
        } finally {
            Config.minimumEmeraldCost = originalEmeraldCost;
            Config.minimumEmeraldBlockCost = originalEmeraldBlockCost;
        }
    }

    public static void conversionTableRejectsInvalidCopyTemplates(GameTestHelper helper) {
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        EnchantmentConversionMenu menu = conversionMenu(helper, player);
        Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
        Holder<Enchantment> unbreaking = enchantment(helper, Enchantments.UNBREAKING);

        ItemStack multiEnchantmentBook = enchantedBook(sharpness, 3);
        multiEnchantmentBook.enchant(unbreaking, 1);
        ItemStack overMaxBook = enchantedBook(sharpness, sharpness.value().getMaxLevel() + 1);

        assertTrue(helper,
                !menu.getSlot(EnchantmentConversionMenu.TEMPLATE_BOOK_SLOT).mayPlace(multiEnchantmentBook),
                "Copy template slot should reject multi-enchantment books"
        );
        assertTrue(helper,
                !menu.getSlot(EnchantmentConversionMenu.TEMPLATE_BOOK_SLOT).mayPlace(overMaxBook),
                "Copy template slot should reject books above the enchantment max level"
        );

        helper.succeed();
    }

    public static void conversionTableAutomationInputsMaterialsAndExtractsCopiesOnly(GameTestHelper helper) {
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        int originalEmeraldCost = Config.minimumEmeraldCost;
        int originalEmeraldBlockCost = Config.minimumEmeraldBlockCost;
        Config.minimumEmeraldCost = 1;
        Config.minimumEmeraldBlockCost = 0;

        try {
            EnchantmentConversionTableBlockEntity blockEntity = helper.getBlockEntity(
                    ENCHANTMENT_CONVERSION_TABLE_POS,
                    EnchantmentConversionTableBlockEntity.class
            );
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            blockEntity.getInventory().setStackInSlot(
                    EnchantmentConversionTableBlockEntity.TEMPLATE_SLOT,
                    enchantedBook(sharpness, 2)
            );
            ResourceHandler<ItemResource> handler = helper.getLevel().getCapability(
                    Capabilities.Item.BLOCK,
                    helper.absolutePos(ENCHANTMENT_CONVERSION_TABLE_POS),
                    Direction.UP
            );

            assertTrue(helper, handler != null, "Conversion table should expose an item handler capability");
            assertTrue(helper, insertStack(handler, 2, new ItemStack(Items.BOOK)).getCount() == 1, "Automation should not insert into the output slot");
            assertTrue(helper, extractStack(handler, 0, 1).isEmpty(), "Automation should not extract input books");

            assertTrue(helper, insertStack(handler, 0, new ItemStack(Items.BOOK, 2)).isEmpty(), "Automation should insert normal books");
            assertTrue(helper, insertStack(handler, 1, new ItemStack(Items.EMERALD, 2)).isEmpty(), "Automation should insert payment items");
            assertEnchantmentLevel(
                    helper,
                    stackInSlot(handler, 2),
                    sharpness,
                    2,
                    "Automation should expose the generated copy in its output slot"
            );

            ItemStack firstCopy = extractStack(handler, 2, 1);
            assertEnchantmentLevel(helper, firstCopy, sharpness, 2, "Automation should extract the generated copy");
            assertEnchantmentLevel(
                    helper,
                    stackInSlot(handler, 2),
                    sharpness,
                    2,
                    "Extracting a copy should generate the next copy while materials remain"
            );

            helper.succeed();
        } finally {
            Config.minimumEmeraldCost = originalEmeraldCost;
            Config.minimumEmeraldBlockCost = originalEmeraldBlockCost;
        }
    }

    public static void customTableSplitsSingleHighLevelBookByDesign(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(false, false);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);

            menu.getSlot(0).setByPlayer(enchantedBook(sharpness, 8));

            assertTrue(helper, menu.totalPage == 1, "High-level single-enchantment books should generate one result page");
            assertEnchantmentLevel(helper, menu.getSlot(2).getItem(), sharpness, 4, "First split book should contain half the source level");
            assertEnchantmentLevel(helper, menu.getSlot(3).getItem(), sharpness, 2, "Second split book should contain the next binary level");
            assertEnchantmentLevel(helper, menu.getSlot(4).getItem(), sharpness, 1, "Third split book should contain the final unique level");
            assertTrue(helper, menu.getSlot(5).getItem().isEmpty(), "No duplicate split book should be generated");

            helper.succeed();
        } finally {
            config.restore();
        }
    }
    public static void customTableMergesDuplicateBookLevelsWithoutVanillaCap(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(false, false);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 4);

            menu.getSlot(0).setByPlayer(sword);
            assertTrue(helper,
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

    public static void customTableRejectsOvercapMergeWhenLevelLimitIsEnforced(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(true, false);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 4);
            ItemStack book = enchantedBook(sharpness, 4);

            menu.getSlot(0).setByPlayer(sword);

            assertTrue(helper, !menu.getSlot(1).mayPlace(book), "Input slot should reject over-cap duplicate books when level limits are enforced");
            assertTrue(helper, !menu.addEnchantment(book, 1, true), "Core add path should reject over-cap duplicate books when level limits are enforced");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 4, "Rejected merge should leave the tool unchanged");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    public static void customTableLevelLimitAllowsNewOvercapEnchantments(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(true, false);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            Holder<Enchantment> unbreaking = enchantment(helper, Enchantments.UNBREAKING);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 5);
            ItemStack overcapNewBook = enchantedBook(unbreaking, unbreaking.value().getMaxLevel() + 2);

            menu.getSlot(0).setByPlayer(sword);

            assertTrue(helper, menu.getSlot(1).mayPlace(overcapNewBook), "Level limits should not reject new over-cap enchantments");
            assertTrue(helper, menu.addEnchantment(overcapNewBook, 1, true), "Core add path should allow new over-cap enchantments");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 5, "Existing enchantment should remain unchanged");
            assertEnchantmentLevel(
                    helper,
                    menu.getSlot(0).getItem(),
                    unbreaking,
                    unbreaking.value().getMaxLevel() + 2,
                    "New over-cap enchantment should be added even when merge limits are enforced"
            );

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    public static void customTableIncrementalMergeAddsOneForSameLevel(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(false, true);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 5);

            menu.getSlot(0).setByPlayer(sword);

            assertTrue(helper, menu.addEnchantment(enchantedBook(sharpness, 5), 1, true), "Incremental merge should accept same-level duplicate books");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 6, "Incremental merge should add one level");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    public static void customTableIncrementalMergeRejectsDifferentLevel(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(false, true);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 5);
            ItemStack book = enchantedBook(sharpness, 4);

            menu.getSlot(0).setByPlayer(sword);

            assertTrue(helper, !menu.getSlot(1).mayPlace(book), "Input slot should reject different-level duplicate books in incremental mode");
            assertTrue(helper, !menu.addEnchantment(book, 1, true), "Core add path should reject different-level duplicate books in incremental mode");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 5, "Rejected incremental merge should leave the tool unchanged");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    public static void customTableIncrementalMergeObeysVanillaCapWhenLevelLimitIsEnforced(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(true, true);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 5);
            ItemStack book = enchantedBook(sharpness, 5);

            menu.getSlot(0).setByPlayer(sword);

            assertTrue(helper, !menu.getSlot(1).mayPlace(book), "Input slot should reject incremental merges above the vanilla cap when limits are enforced");
            assertTrue(helper, !menu.addEnchantment(book, 1, true), "Core add path should reject incremental merges above the vanilla cap when limits are enforced");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 5, "Rejected over-cap incremental merge should leave the tool unchanged");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    public static void customTableIncrementalMergeAllowsNewEnchantments(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(true, true);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            Holder<Enchantment> unbreaking = enchantment(helper, Enchantments.UNBREAKING);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 5);

            menu.getSlot(0).setByPlayer(sword);

            assertTrue(helper, menu.addEnchantment(enchantedBook(unbreaking, 3), 1, true), "Incremental mode should not block new enchantments");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 5, "Existing enchantment should remain unchanged");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), unbreaking, 3, "New enchantment should be added normally");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    public static void customTableRejectsWholeMultiEnchantmentBookWhenOneEntryIsInvalid(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(false, true);

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

            assertTrue(helper, !menu.addEnchantment(book, 1, true), "A multi-enchantment book should be rejected atomically if any entry is invalid");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 5, "Invalid multi-book should not change existing enchantments");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), unbreaking, 0, "Invalid multi-book should not add otherwise valid new enchantments");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    public static void customTableIncrementalModeSplitsSingleBookIntoMinusOnePair(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(false, true);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);

            menu.getSlot(0).setByPlayer(enchantedBook(sharpness, 8));

            assertTrue(helper, menu.totalPage == 1, "High-level single-enchantment books should generate one result page");
            assertEnchantmentLevel(helper, menu.getSlot(2).getItem(), sharpness, 7, "First incremental split book should be one level lower");
            assertEnchantmentLevel(helper, menu.getSlot(3).getItem(), sharpness, 7, "Second incremental split book should be one level lower");
            assertTrue(helper, menu.getSlot(4).getItem().isEmpty(), "Incremental split should only generate the matching pair");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    public static void customTableIncrementalModeTakingSplitBookOnlyDropsSourceBookOneLevel(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(false, true);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);

            menu.getSlot(0).setByPlayer(enchantedBook(sharpness, 5));
            player.containerMenu = menu;

            menu.clicked(2, 0, ContainerInput.PICKUP, player);

            assertTrue(helper, player.containerMenu.getCarried().is(Items.ENCHANTED_BOOK), "Taking a split book should put that book on the cursor");
            assertEnchantmentLevel(helper, player.containerMenu.getCarried(), sharpness, 4, "Taken split book should be one level lower than the source");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 4, "Source book should only drop by one level in incremental split mode");
            assertEnchantmentLevel(helper, menu.getSlot(2).getItem(), sharpness, 3, "Generated split books should refresh from the new source level");
            assertEnchantmentLevel(helper, menu.getSlot(3).getItem(), sharpness, 3, "Generated split pair should refresh from the new source level");

            helper.succeed();
        } finally {
            config.restore();
        }
    }
    public static void customTableQuickMoveBookIntoInputUpdatesTool(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(false, false);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 4);

            menu.getSlot(0).setByPlayer(sword);
            player.getInventory().setItem(0, enchantedBook(sharpness, 1));

            int firstHotbarSlot = EnchantingCustomMenu.ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE + 27;
            ItemStack moved = menu.quickMoveStack(player, firstHotbarSlot);

            assertTrue(helper, moved.is(Items.ENCHANTED_BOOK), "Quick-moving an enchanted book should move the book stack");
            assertTrue(helper, player.getInventory().getItem(0).isEmpty(), "Quick-moving should consume the source inventory book");
            assertTrue(helper, menu.getSlot(1).getItem().isEmpty(), "Quick-moving into the input book slot should leave that slot empty");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 5, "Quick-moving into the input book slot should merge into the tool");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    public static void customTableDragInsertBookIntoGeneratedSlotUpdatesTool(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(false, false);

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 4);

            menu.getSlot(0).setByPlayer(sword);

            ItemStack carriedBook = enchantedBook(sharpness, 1);
            ItemStack remaining = menu.getSlot(3).safeInsert(carriedBook);
            menu.getSlot(3).setChanged();

            assertTrue(helper, remaining.isEmpty(), "Drag-inserting an enchanted book should consume the carried book");
            assertTrue(helper, menu.getSlot(3).getItem().isEmpty(), "Drag-inserting into a generated slot should leave that slot empty");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 5, "Drag-inserting into a generated slot should merge into the tool");

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    public static void customTableKeepsToolInBlockStorageAfterMenuClose(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        EnchantingCustomMenu menu = enchantingMenu(helper, player);
        Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(sharpness, 3);

        menu.getSlot(0).setByPlayer(sword);
        menu.removed(player);

        EnchantingCustomTableBlockEntity blockEntity = helper.getBlockEntity(
                ENCHANTING_CUSTOM_TABLE_POS,
                EnchantingCustomTableBlockEntity.class
        );
        assertEnchantmentLevel(
                helper,
                blockEntity.getInventory().getStackInSlot(EnchantingCustomTableBlockEntity.TOOL_SLOT),
                sharpness,
                3,
                "Closing the menu should keep the tool stored in the block entity"
        );

        helper.succeed();
    }

    public static void customTableAutomationAppliesAcceptedBook(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(false, false);

        try {
            EnchantingCustomTableBlockEntity blockEntity = helper.getBlockEntity(
                    ENCHANTING_CUSTOM_TABLE_POS,
                    EnchantingCustomTableBlockEntity.class
            );
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 4);
            blockEntity.getInventory().setStackInSlot(EnchantingCustomTableBlockEntity.TOOL_SLOT, sword);

            ResourceHandler<ItemResource> handler = helper.getLevel().getCapability(
                    Capabilities.Item.BLOCK,
                    helper.absolutePos(ENCHANTING_CUSTOM_TABLE_POS),
                    Direction.UP
            );

            assertTrue(helper, handler != null, "Custom table should expose an item handler capability");
            assertTrue(helper, insertStack(handler, 0, enchantedBook(sharpness, 1)).isEmpty(), "Automation should consume an accepted enchanted book");
            assertTrue(helper, extractStack(handler, 0, 1).isEmpty(), "Automation should not extract the stored tool");
            assertEnchantmentLevel(
                    helper,
                    blockEntity.getInventory().getStackInSlot(EnchantingCustomTableBlockEntity.TOOL_SLOT),
                    sharpness,
                    5,
                    "Automation should apply the accepted book to the stored tool"
            );

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    public static void customTableAutomationRejectsInvalidBook(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        ConfigSnapshot config = useMergeConfig(true, false);

        try {
            EnchantingCustomTableBlockEntity blockEntity = helper.getBlockEntity(
                    ENCHANTING_CUSTOM_TABLE_POS,
                    EnchantingCustomTableBlockEntity.class
            );
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, sharpness.value().getMaxLevel());
            blockEntity.getInventory().setStackInSlot(EnchantingCustomTableBlockEntity.TOOL_SLOT, sword);

            ResourceHandler<ItemResource> handler = helper.getLevel().getCapability(
                    Capabilities.Item.BLOCK,
                    helper.absolutePos(ENCHANTING_CUSTOM_TABLE_POS),
                    Direction.UP
            );
            ItemStack rejected = insertStack(handler, 0, enchantedBook(sharpness, sharpness.value().getMaxLevel()));

            assertTrue(helper, !rejected.isEmpty(), "Automation should return a book that cannot be merged");
            assertEnchantmentLevel(
                    helper,
                    blockEntity.getInventory().getStackInSlot(EnchantingCustomTableBlockEntity.TOOL_SLOT),
                    sharpness,
                    sharpness.value().getMaxLevel(),
                    "Rejected automation input should leave the stored tool unchanged"
            );

            helper.succeed();
        } finally {
            config.restore();
        }
    }

    public static void customTableRemovingGeneratedBookSubtractsFromTool(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        EnchantingCustomMenu menu = enchantingMenu(helper, player);
        Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(sharpness, 5);

        menu.getSlot(0).setByPlayer(sword);
        player.containerMenu = menu;

        menu.clicked(2, 0, ContainerInput.PICKUP, player);

        assertTrue(helper, player.containerMenu.getCarried().is(Items.ENCHANTED_BOOK), "Taking a generated book should put that book on the cursor");
        assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 0, "Removing a generated book should remove the matching tool enchantment");
        assertTrue(helper, menu.getSlot(2).getItem().isEmpty(), "Generated books should clear after the source enchantment is removed");

        helper.succeed();
    }

    public static void customTableQuickMoveGeneratedBookSubtractsFromTool(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        EnchantingCustomMenu menu = enchantingMenu(helper, player);
        Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(sharpness, 5);

        menu.getSlot(0).setByPlayer(sword);

        ItemStack moved = menu.quickMoveStack(player, 2);

        assertTrue(helper, moved.is(Items.ENCHANTED_BOOK), "Quick-moving a generated book should move that book stack");
        assertTrue(helper, player.getInventory().contains(moved), "Quick-moving a generated book should place it in the player inventory");
        assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 0, "Quick-moving a generated book should remove the matching tool enchantment");
        assertTrue(helper, menu.getSlot(2).getItem().isEmpty(), "Generated books should clear after the source enchantment is quick-moved");

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
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElseThrow();
    }

    private static Identifier enchantmentId(GameTestHelper helper, Holder<Enchantment> enchantment) {
        return EnchantmentUtils.getEnchantmentKey(helper.getLevel(), enchantment)
                .orElseThrow()
                .identifier();
    }

    private static ItemStack enchantedBook(Holder<Enchantment> enchantment, int level) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.enchant(enchantment, level);
        return book;
    }

    private static ItemStack stackInSlot(ResourceHandler<ItemResource> handler, int slot) {
        return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
    }

    private static ItemStack insertStack(ResourceHandler<ItemResource> handler, int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemResource resource = ItemResource.of(stack);
        try (Transaction transaction = Transaction.open(null)) {
            int inserted = handler.insert(slot, resource, stack.getCount(), transaction);
            transaction.commit();
            return stack.copyWithCount(stack.getCount() - inserted);
        }
    }

    private static ItemStack extractStack(ResourceHandler<ItemResource> handler, int slot, int amount) {
        ItemResource resource = handler.getResource(slot);
        if (resource.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.open(null)) {
            int extracted = handler.extract(slot, resource, amount, transaction);
            transaction.commit();
            return resource.toStack(extracted);
        }
    }

    private static ConfigSnapshot useMergeConfig(boolean enforceEnchantmentLevelLimit, boolean incrementalSameLevelMerge) {
        ConfigSnapshot snapshot = new ConfigSnapshot(
                Config.enforceEnchantmentLevelLimit,
                Config.incrementalSameLevelMerge
        );
        Config.enforceEnchantmentLevelLimit = enforceEnchantmentLevelLimit;
        Config.incrementalSameLevelMerge = incrementalSameLevelMerge;
        return snapshot;
    }

    private record ConfigSnapshot(boolean enforceEnchantmentLevelLimit, boolean incrementalSameLevelMerge) {
        private void restore() {
            Config.enforceEnchantmentLevelLimit = enforceEnchantmentLevelLimit;
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
        assertTrue(helper, actualLevel == expectedLevel, message + " (expected " + expectedLevel + ", got " + actualLevel + ")");
    }

    private static void assertEnchantmentIdLevel(
            GameTestHelper helper,
            ItemStack stack,
            Identifier enchantmentId,
            int expectedLevel,
            String message
    ) {
        int actualLevel = 0;
        for (var entry : EnchantmentUtils.getEnchantments(stack).entrySet()) {
            Identifier entryId = enchantmentId(helper, entry.getKey());
            if (enchantmentId.equals(entryId)) {
                actualLevel = entry.getIntValue();
                break;
            }
        }
        assertTrue(helper, actualLevel == expectedLevel, message + " (expected " + expectedLevel + ", got " + actualLevel + ")");
    }

    private static void assertTrue(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }

    private static void movePlayerTo(Player player, BlockPos pos) {
        player.snapTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0F, 0.0F);
    }
}
