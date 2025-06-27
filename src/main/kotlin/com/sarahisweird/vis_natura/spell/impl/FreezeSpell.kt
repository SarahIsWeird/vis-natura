package com.sarahisweird.vis_natura.spell.impl

import com.sarahisweird.vis_natura.VisNatura
import com.sarahisweird.vis_natura.spell.Spell
import com.sarahisweird.vis_natura.vis.VisType
import net.minecraft.block.Blocks
import net.minecraft.registry.tag.BlockTags
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object FreezeSpell : Spell(VisNatura.id("freeze"), VisType.VIS_AQUAE) {
    private const val RANGE = 4

    override fun onBlockHit(hitInfo: BlockHitInfo) {
        super.onBlockHit(hitInfo)

        this.iterateBlocks(hitInfo.world, hitInfo.blockPos)
    }

    override fun onEntityHit(hitInfo: EntityHitInfo) {
        super.onEntityHit(hitInfo)

        this.iterateBlocks(hitInfo.world, BlockPos.ofFloored(hitInfo.pos))
    }

    private fun iterateBlocks(world: World, centerPos: BlockPos) {
        for (pos in BlockPos.iterateOutwards(centerPos, RANGE, RANGE, RANGE)) {
            if (pos.getSquaredDistance(centerPos) > RANGE * RANGE) continue
            this.tryReplaceBlock(world, pos)
        }
    }

    private fun tryReplaceBlock(world: World, pos: BlockPos) {
        val state = world.getBlockState(pos)

        when {
            state.isOf(Blocks.WATER) -> {
                world.setBlockState(pos, Blocks.FROSTED_ICE.defaultState)
            }

            state.isOf(Blocks.LAVA) -> {
                world.setBlockState(pos, Blocks.OBSIDIAN.defaultState)
            }

            state.isIn(BlockTags.FIRE) -> {
                world.setBlockState(pos, Blocks.AIR.defaultState)
            }

            state.isIn(BlockTags.CAMPFIRES) -> {
                val newState = state.with(Properties.LIT, false)
                world.setBlockState(pos, newState)
            }
        }
    }
}
