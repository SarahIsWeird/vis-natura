package com.sarahisweird.vis_natura.block

import com.sarahisweird.vis_natura.util.ShapeUtil
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.ShapeContext
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.WorldView
import net.minecraft.world.tick.ScheduledTickView

class CenserBlock(settings: Settings) : Block(settings) {
    companion object {
        val HANGING: BooleanProperty = Properties.HANGING

        val CENSER_BODY_SHAPE = ShapeUtil.union {
            // 0, 2, 6, 7
            centeredPosSize(3, 0, 10, 2)
            centeredPosSize(2, 2, 12, 4)
            centeredPosSize(3, 6, 10, 1)
            centeredPosSize(5, 7, 6, 1)
        }

        val HANGING_CENSER_BODY_SHAPE = ShapeUtil.offset(CENSER_BODY_SHAPE, offsetY = 4)
        val HANGING_CENSER_SHAPE = ShapeUtil.union {
            shape(HANGING_CENSER_BODY_SHAPE)
            centeredPosSize(6.5, 12.0, 3.0, 4.0)
        }
    }

    init {
        this.defaultState = this.defaultState
            .with(HANGING, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(HANGING)
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext,
    ): VoxelShape {
        return if (state[HANGING, false]) {
            HANGING_CENSER_SHAPE
        } else {
            CENSER_BODY_SHAPE
        }
    }

    override fun getCollisionShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext,
    ): VoxelShape {
        return if (state[HANGING, false]) {
            HANGING_CENSER_BODY_SHAPE
        } else {
            CENSER_BODY_SHAPE
        }
    }

    override fun getCullingShape(state: BlockState): VoxelShape {
        return if (state[HANGING, false]) {
            HANGING_CENSER_BODY_SHAPE
        } else {
            CENSER_BODY_SHAPE
        }
    }

    private fun canConnectUp(world: WorldView, thisPos: BlockPos): Boolean {
        return sideCoversSmallSquare(world, thisPos.up(), Direction.DOWN)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        if (ctx.verticalPlayerLookDirection == Direction.UP) {
            return this.defaultState
                .with(HANGING, this.canConnectUp(ctx.world, ctx.blockPos))
        }

        return this.defaultState
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        world: WorldView,
        tickView: ScheduledTickView,
        pos: BlockPos,
        direction: Direction,
        neighborPos: BlockPos,
        neighborState: BlockState,
        random: Random,
    ): BlockState {
        val shouldBreak = direction == Direction.UP
                && state[HANGING, false] == true
                && !this.canConnectUp(world, pos)
        if (shouldBreak) {
            return Blocks.AIR.defaultState
        }

        return super.getStateForNeighborUpdate(
            state,
            world,
            tickView,
            pos,
            direction,
            neighborPos,
            neighborState,
            random,
        )
    }
}
