package net.caffeinemc.mods.lithium.common.block.redstone;

import net.caffeinemc.mods.lithium.common.util.DirectionConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;

/**
 * Optimizing redstone dust is tricky, but even more so if you wish to preserve behavior
 * perfectly. There are two big reasons redstone wire is laggy:
 * <br>
 * - It updates recursively. Each wire updates its power level in isolation, rather than
 * in the context of the network it is a part of. This means each wire in a network
 * could check and update its power level over half a dozen times. This also leads to
 * way more shape and block updates than necessary.
 * <br>
 * - It emits copious amounts of duplicate and redundant shape and block updates. While
 * the recursive updates are largely to blame, even a single state change leads to 18
 * redundant block updates and up to 16 redundant shape updates.
 * 
 * <p>
 * Unfortunately fixing either of these aspects can be detected in-game, even if it is
 * through obscure mechanics. Removing redundant block updates can be detected with
 * something as simple as a redstone wire on a trapdoor, while removing the recursive
 * updates can be detected with locational setups that rely on a specific block update
 * order.
 * 
 * <p>
 * What we can optimize, however, are the power calculations. In vanilla, these are split
 * into two parts:
 * <br>
 * - Power from non-wire components.
 * <br>
 * - Power from other redstone wires.
 * 
 * <p>
 * For the default evaluator, we can combine the two to reduce calls to Level.getBlockState
 * and BlockState.isRedstoneConductor as well as calls to BlockState.getSignal and
 * BlockState.getDirectSignal. We can avoid calling those last two methods on redstone wires
 * altogether, since we know they should return 0.
 * <br>
 * These changes can lead to a mspt reduction of up to 20% on top of Lithium's other
 * performance improvements.
 * 
 * <p>
 * The experimental evaluator actually uses the wire-power and non-wire-power separately,
 * so we can't combine them. We can, however, optimize each individually, with the same
 * principles.
 * 
 * @author Space Walker
 */
public class RedstoneWirePowerCalculations {

    private static final int MIN = 0;            // smallest possible power value
    private static final int MAX = 15;           // largest possible power value
    private static final int MAX_WIRE = MAX - 1; // largest possible power a wire can receive from another wire

    private static Block wireBlock;
    private static RedstoneWireEvaluator evaluator;
    private static boolean ignoreWires = false;
    private static boolean ignoreNonWires = false;

    public static int getNeighborBlockSignal(Block wireBlock, RedstoneWireEvaluator evaluator, Level level, BlockPos pos) {
        ignoreWires = true;
        int signal = getNeighborSignal(wireBlock, evaluator, level, pos);
        ignoreWires = false;

        return signal;
    }

    public static int getNeighborWireSignal(Block wireBlock, RedstoneWireEvaluator evaluator, Level level, BlockPos pos) {
        ignoreNonWires = true;
        int signal = getNeighborSignal(wireBlock, evaluator, level, pos);
        ignoreNonWires = false;

        return signal;
    }

    /**
     * Calculate the redstone power a wire at the given location receives from the
     * blocks around it.
     */
    public static int getNeighborSignal(Block wireBlock, RedstoneWireEvaluator evaluator, Level level, BlockPos pos) {
        RedstoneWirePowerCalculations.wireBlock = wireBlock;
        RedstoneWirePowerCalculations.evaluator = evaluator;

        int signal = MIN;
        LevelChunk chunk = level.getChunkAt(pos);

        if (!ignoreNonWires) {
            for (Direction dir : DirectionConstants.VERTICAL) {
                BlockPos side = pos.relative(dir);
                BlockState neighbor = chunk.getBlockState(side);

                // Wires do not accept power from other wires directly above or below them,
                // so those can be ignored. Similarly, if there is air directly above or
                // below a wire, it does not receive any power from that direction.
                if (!neighbor.isAir() && !neighbor.is(wireBlock)) {
                    signal = Math.max(signal, getSignalFromVertical(level, side, neighbor, dir));

                    if (signal >= MAX) {
                        return MAX;
                    }
                }
            }
        }

        boolean checkWiresAbove = false;

        if (!ignoreWires) {
            // In vanilla this check is done up to 4 times.
            BlockPos above = pos.above();
            checkWiresAbove = !chunk.getBlockState(above).isRedstoneConductor(level, above);
        }

        for (Direction dir : DirectionConstants.HORIZONTAL) {
            signal = Math.max(signal, getSignalFromSide(level, pos.relative(dir), dir, checkWiresAbove));

            if (signal >= MAX) {
                return MAX;
            }
        }

        return signal;
    }

    /**
     * Calculate the redstone power a wire receives from a block above or below it.
     * We do these positions separately because there are no wire connections
     * vertically. This simplifies the calculations a little.
     */
    private static int getSignalFromVertical(Level level, BlockPos pos, BlockState state, Direction toDir) {
        int signal = state.getSignal(level, pos, toDir);

        if (signal >= MAX) {
            return MAX;
        }

        if (state.isRedstoneConductor(level, pos)) {
            return Math.max(signal, getDirectSignalTo(level, pos, toDir.getOpposite()));
        }

        return signal;
    }

    /**
     * Calculate the redstone power a wire receives from blocks next to it.
     */
    private static int getSignalFromSide(Level level, BlockPos pos, Direction toDir, boolean checkWiresAbove) {
        LevelChunk chunk = level.getChunkAt(pos);
        BlockState state = chunk.getBlockState(pos);

        if (state.is(wireBlock)) {
            return ignoreWires ? MIN : evaluator.getWireSignal(pos, state) - 1;
        }

        int signal = MIN;

        if (!ignoreNonWires) {
            signal = state.getSignal(level, pos, toDir);

            if (signal >= MAX) {
                return MAX;
            }
        }

        if (state.isRedstoneConductor(level, pos)) {
            if (!ignoreNonWires) {
                signal = Math.max(signal, getDirectSignalTo(level, pos, toDir.getOpposite()));

                if (signal >= MAX) {
                    return MAX;
                }
            }
            if (!ignoreWires && checkWiresAbove && signal < MAX_WIRE) {
                BlockPos above = pos.above();
                BlockState aboveState = chunk.getBlockState(above);

                if (aboveState.is(wireBlock)) {
                    signal = Math.max(signal, evaluator.getWireSignal(above, aboveState) - 1);
                }
            }
        } else if (!ignoreWires && signal < MAX_WIRE) {
            BlockPos below = pos.below();
            BlockState belowState = chunk.getBlockState(below);

            if (belowState.is(wireBlock)) {
                signal = Math.max(signal, evaluator.getWireSignal(below, belowState) - 1);
            }
        }

        return signal;
    }

    /**
     * Calculate the strong power a block receives from the blocks around it.
     */
    private static int getDirectSignalTo(Level level, BlockPos pos, Direction ignore) {
        int signal = MIN;

        for (Direction dir : DirectionConstants.ALL) {
            if (dir != ignore) {
                BlockPos side = pos.relative(dir);
                BlockState neighbor = level.getBlockState(side);

                if (!neighbor.isAir() && !neighbor.is(wireBlock)) {
                    signal = Math.max(signal, neighbor.getDirectSignal(level, side, dir));

                    if (signal >= MAX) {
                        return MAX;
                    }
                }
            }
        }

        return signal;
    }
}
