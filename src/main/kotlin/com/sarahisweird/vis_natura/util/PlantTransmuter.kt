package com.sarahisweird.vis_natura.util

import com.sarahisweird.vis_natura.VisNatura
import com.sarahisweird.vis_natura.vis.VisType
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.AutomaticItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.particle.DustParticleEffect
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import kotlin.jvm.optionals.getOrNull

object PlantTransmuter {
    private val random = Random.create()

    fun transmuteBlock(world: World, pos: BlockPos, visType: VisType, causeStack: ItemStack): Boolean {
        val state = world.getBlockState(pos)
        if (!state.isOf(Blocks.DEAD_BUSH)) return false

        val growablePlantsTag = this.getGrowablePlantsTagByVisType(visType)
        val resultingBlock = Registries.BLOCK.getRandomEntry(growablePlantsTag, this.random).getOrNull()
            ?: return false

        if (world.isClient) {
            this.addGrowthParticles(world, pos.toCenterPos(), visType)
            return true
        }

        val placementContext = AutomaticItemPlacementContext(
            world,
            pos,
            Direction.DOWN,
            causeStack,
            Direction.DOWN)
        val newState = resultingBlock.value().getPlacementState(placementContext)
            ?: resultingBlock.value().defaultState
        world.setBlockState(pos, newState)

        return true
    }

    private fun getGrowablePlantsTagByVisType(visType: VisType): TagKey<Block> {
        return TagKey.of(
            RegistryKeys.BLOCK,
            VisNatura.id("${visType.typeName}_growable")
        )
    }

    private fun addGrowthParticles(world: World, pos: Vec3d, visType: VisType) {
        val particleEffect = DustParticleEffect(visType.color, 1f)

        val count = 50
        for (i in 0..count) {
            val x = pos.x + random.nextDouble() - 0.5
            val y = pos.y + random.nextDouble() - 0.5
            val z = pos.z + random.nextDouble() - 0.5

            world.addParticleClient(particleEffect,
                x, y, z,
                0.0, 1.0, 0.0)
        }
    }
}
