package com.sarahisweird.vis_natura.rendering.ber

import com.sarahisweird.vis_natura.block.AltarBlockEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.entity.state.ItemStackEntityRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemDisplayContext
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import kotlin.math.sin

class AltarBlockEntityRenderer(
    context: BlockEntityRendererFactory.Context,
) : BlockEntityRenderer<AltarBlockEntity> {
    private val itemRenderer = context.itemRenderer
    private val random = Random.create()

    private var renderTicks = random.nextFloat()

    override fun render(
        entity: AltarBlockEntity,
        tickProgress: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int,
        cameraPos: Vec3d,
    ) {
        this.renderWand(entity, tickProgress, matrices, vertexConsumers)

        this.renderTicks++
    }

    private fun renderWand(
        altar: AltarBlockEntity,
        tickProgress: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
    ) {
        val stack = altar.inventory.stack
        if (stack.isEmpty) return

        val lightAbove = WorldRenderer.getLightmapCoordinates(altar.world, altar.pos.up())
        val seed = ItemStackEntityRenderState.getSeed(stack)
        random.setSeed(seed.toLong())

        val partialTicks = this.renderTicks + tickProgress

        val yBase = (12.0 + 4.0) / 16.0
        val yOffset = sin(partialTicks / 60f) / 16f

        matrices.push()
        matrices.translate(0.5, yBase + yOffset, 0.5)
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotation(partialTicks / 120))

        this.itemRenderer.renderItem(
            stack,
            ItemDisplayContext.GROUND,
            lightAbove,
            OverlayTexture.DEFAULT_UV,
            matrices,
            vertexConsumers,
            altar.world,
            0,
        )

        matrices.pop()
    }
}
