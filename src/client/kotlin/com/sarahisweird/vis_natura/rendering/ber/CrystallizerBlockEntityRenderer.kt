package com.sarahisweird.vis_natura.rendering.ber

import com.sarahisweird.vis_natura.block.CrystallizerBlockEntity
import net.minecraft.client.render.*
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.entity.state.ItemStackEntityRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemDisplayContext
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import kotlin.math.sin

class CrystallizerBlockEntityRenderer(
    context: BlockEntityRendererFactory.Context,
) : BlockEntityRenderer<CrystallizerBlockEntity> {
    private val itemRenderer = context.itemRenderer
    private val random = Random.create()

    private var renderTicks = random.nextFloat()

    override fun render(
        entity: CrystallizerBlockEntity,
        tickProgress: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int,
        cameraPos: Vec3d,
    ) {
        renderVisFluid(entity, matrices, vertexConsumers, overlay)
        renderCrystals(entity, tickProgress, matrices, vertexConsumers)

        renderTicks += 1
    }

    private fun renderVisFluid(
        entity: CrystallizerBlockEntity,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        overlay: Int,
    ) {
        val visType = entity.biomeVisType ?: return
        val visLevel = entity.visLevel

        val size = 10f / 16f
        val x = 3f / 16f
        val z = x + size
        val visHeight = visLevel.toFloat() / entity.visCapacity * 1.75f / 16f + 0.01f

        matrices.push()
        matrices.translate(0.0, 10.0 / 16.0, 0.0)

        val state = entity.cachedState
        val transform = matrices.peek().positionMatrix
        val buffer = vertexConsumers.getBuffer(RenderLayers.getBlockLayer(state))

        fun doVertex(x: Float, z: Float) {
            buffer.vertex(transform, x, visHeight, z)
                .color(ColorHelper.withAlpha(0x80, visType.color))
                .texture(0f, 0f)
                .light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE)
                .overlay(overlay)
                .normal(0f, 1f, 0f)
        }

        doVertex(x, x)
        doVertex(x, z)
        doVertex(z, z)
        doVertex(z, x)

        matrices.pop()
    }

    private fun renderCrystals(
        entity: CrystallizerBlockEntity,
        tickProgress: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
    ) {
        val crystalsStack = entity.inventory.stack
        if (crystalsStack.isEmpty) return

        matrices.push()

        val lightAbove = WorldRenderer.getLightmapCoordinates(entity.world, entity.pos.up())

        val seed = ItemStackEntityRenderState.getSeed(crystalsStack)
        random.setSeed(seed.toLong())

        val count = ItemStackEntityRenderState.getRenderedAmount(crystalsStack.count)

        matrices.translate(0.5, 15.0 / 16.0, 0.5)
        val tickThingy = (this.renderTicks + tickProgress) / 120
        matrices.translate(0f, sin((this.renderTicks + tickProgress) / 60f) / 16f, 0f)
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(tickThingy))

        var maxZ = 0f
        for (i in 0..<count) {
            if (i > 0) {
                maxZ += i.toFloat() * 0.05f
            }

            maxZ += 1f / 16f
        }

        matrices.translate(0f, 0f, -maxZ / count.toFloat() / 2f)

        itemRenderer.renderItem(
            crystalsStack,
            ItemDisplayContext.GROUND,
            lightAbove,
            OverlayTexture.DEFAULT_UV,
            matrices,
            vertexConsumers,
            entity.world,
            0
        )

        for (i in 1..<count) {
            matrices.push()

            val xOffset = (random.nextFloat() * 2f - 1f) * 0.15f
            val yOffset = (random.nextFloat() * 2f - 1f) * 0.15f
            random.nextFloat()
            val zOffset = i.toFloat() * 0.05f
            matrices.translate(xOffset, yOffset, zOffset)

            itemRenderer.renderItem(
                crystalsStack,
                ItemDisplayContext.GROUND,
                lightAbove,
                OverlayTexture.DEFAULT_UV,
                matrices,
                vertexConsumers,
                entity.world,
                0
            )

            matrices.pop()
        }

        matrices.pop()
    }
}
