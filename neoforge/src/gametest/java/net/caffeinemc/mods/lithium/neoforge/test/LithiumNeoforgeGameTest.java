package net.caffeinemc.mods.lithium.neoforge.test;


import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@PrefixGameTestTemplate(value = false)
@GameTestHolder("lithium-gametest")
public class LithiumNeoforgeGameTest {

    //Not very nice to do this but it works
    public static final String LITHIUM_GAMETEST_SNBT_PATH = "../../../common/src/gametest/resources/data/lithium-gametest/gametest/structure";

    //Some tests are excluded because Neoforge is breaking them, not lithium.
    //test_redstone.lava_push_speed broken by https://github.com/neoforged/NeoForge/issues/1575
    //test_redstone.comparator_update_collection broken by https://github.com/neoforged/NeoForge/issues/1750
    public static final Collection<String> NEOFORGE_EXLUDED_TESTS = List.of("test_redstone.lava_push_speed", "test_redstone.comparator_update_collection");

    static {
        StructureUtils.testStructuresDir = LITHIUM_GAMETEST_SNBT_PATH;
    }

    @Nullable
    public static String getTemplateName(String structureName) {
        int dotIndex = structureName.indexOf(".");
        if (dotIndex < 0) {
            return null;
        }
        return structureName.substring(0, dotIndex);
    }

    private static boolean shouldReplaceWithRedstoneBlock(BlockState blockState) {
        return blockState.is(Blocks.RED_TERRACOTTA);
    }

    private static boolean isSuccessBlock(BlockState blockState) {
        return blockState.is(Blocks.EMERALD_BLOCK) || blockState.is(Blocks.GREEN_WOOL) || blockState.is(Blocks.LIME_WOOL);
    }

    private static boolean isFailureBlock(BlockState blockState) {
        return blockState.is(Blocks.RED_WOOL);
    }

    /**
     * A test function that can be used to create tests with a simple redstone interface.
     * This allows omitting the test function code by starting the structure filename with "test_redstone."
     */
    public static void test_redstone(GameTestHelper gameTestHelper) {
        ArrayList<BlockPos> successBlocks = new ArrayList<>();
        ArrayList<BlockPos> failureBlocks = new ArrayList<>();

        //Replace all red terracotta with redstone block at the start and fill the condition block lists
        gameTestHelper.forEveryBlockInStructure(blockPos -> {
            BlockState blockState = gameTestHelper.getBlockState(blockPos);
            if (shouldReplaceWithRedstoneBlock(blockState)) {
                gameTestHelper.setBlock(blockPos, Blocks.REDSTONE_BLOCK.defaultBlockState());
            }
            if (isSuccessBlock(blockState)) successBlocks.add(blockPos.immutable());
            if (isFailureBlock(blockState)) failureBlocks.add(blockPos.immutable());
        });
        if (successBlocks.isEmpty()) {
            throw new GameTestAssertException("Expected success condition blocks anywhere inside the test. test_redstone requires green wool, lime wool or emerald blocks for the success condition");
        }

        //Fail when any powered note block is on top of a failure condition block.
        //Succeed when any powered note block is on top of a success condition block. Assume the success/failure condition blocks don't move during the test.
        gameTestHelper.onEachTick(
                () -> {
                    //Always check the failure condition blocks before the success conditions. Failed tests throw exceptions.
                    Optional<BlockPos> failurePosition = checkFailureBlocks(gameTestHelper, failureBlocks);
                    if (failurePosition.isPresent()) {
                        throw new GameTestAssertPosException("Failure condition block activated!", gameTestHelper.absolutePos(failurePosition.get()), failurePosition.get(), gameTestHelper.getTick());
                    }

                    if (checkSuccessBlocks(gameTestHelper, successBlocks)) {
                        gameTestHelper.succeed();
                    }
                }
        );
    }

    private static boolean checkSuccessBlocks(GameTestHelper gameTestHelper, ArrayList<BlockPos> successBlocks) {
        return successBlocks.stream().anyMatch(blockPos -> {
            BlockState blockState = gameTestHelper.getBlockState(blockPos.above());
            return blockState.is(Blocks.NOTE_BLOCK) && blockState.getValue(NoteBlock.POWERED) &&
                    isSuccessBlock(gameTestHelper.getBlockState(blockPos));
        });
    }

    private static Optional<BlockPos> checkFailureBlocks(GameTestHelper gameTestHelper, ArrayList<BlockPos> failureBlocks) {
        return failureBlocks.stream().filter(blockPos -> {
            BlockState blockState = gameTestHelper.getBlockState(blockPos.above());
            return blockState.is(Blocks.NOTE_BLOCK) && blockState.getValue(NoteBlock.POWERED) &&
                    isFailureBlock(gameTestHelper.getBlockState(blockPos));
        }).findFirst();
    }

    @GameTestGenerator
    public Collection<TestFunction> getAllRedstoneTests() {
        List<String> structureNames = null;
        try {
            structureNames = getLithiumSNBTFilenames();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ArrayList<TestFunction> testFunctions = new ArrayList<>();
        for (String structureName : structureNames) {
            String templateName = getTemplateName(structureName);

            if ("test_redstone".equals(templateName)) {
                testFunctions.add(new TestFunction(
                        "lithium_test_redstone",
                        structureName.substring(templateName.length() + 1),
                        "lithium-gametest:" + structureName, //This structure file location method is Fabric dependent?
                        400 /* Timeout ticks. 20 Seconds is fine for our redstone tests. */,
                        10 /* Setup ticks (between placing structure and test start). Prevents random redstone firing from activating success / failure condition immediately. */,
                        !NEOFORGE_EXLUDED_TESTS.contains(structureName) /* Test required */,
                        LithiumNeoforgeGameTest::test_redstone));
            }
        }

        return testFunctions;
    }

    private List<String> getLithiumSNBTFilenames() throws IOException {
        Path folderPath = Paths.get(LITHIUM_GAMETEST_SNBT_PATH);
        try (Stream<Path> paths = Files.walk(folderPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.toFile().getName())
                    .filter(string -> string.endsWith(".snbt"))
                    .map(file -> file.substring(0, file.lastIndexOf('.'))).toList();
        }
    }
}