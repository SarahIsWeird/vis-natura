package com.sarahisweird.vis_natura.rendering.entity

import com.sarahisweird.vis_natura.VisNatura
import com.sarahisweird.vis_natura.entity.SpellCastEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.state.EntityRenderState
import net.minecraft.client.render.item.ItemRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemDisplayContext
import net.minecraft.util.Identifier

/**
 * Right now this is more or less copied from FlyingItemEntityRenderer. It'll be improved soon:tm: :)
 */
class SpellCastEntityRenderer(
    context: EntityRendererFactory.Context,
) : EntityRenderer<SpellCastEntity, SpellCastEntityRenderer.SCERenderState>(
    context
) {
    class SCERenderState(
        var spellId: Identifier? = null,
    ) : EntityRenderState() {
        internal val itemRenderState = ItemRenderState()
    }

    private val itemModelManager = context.itemModelManager

    override fun createRenderState(): SCERenderState? {
        return SCERenderState()
    }

    override fun updateRenderState(entity: SpellCastEntity, state: SCERenderState, tickProgress: Float) {
        super.updateRenderState(entity, state, tickProgress)
        state.spellId = entity.spellId

        val spell = entity.spellId?.let { VisNatura.spellRegistry[it] } ?: return
        val item = spell.visTypes.firstOrNull()?.item ?: return

        this.itemModelManager.updateForNonLivingEntity(
            state.itemRenderState,
            item.defaultStack,
            ItemDisplayContext.GROUND,
            entity)
    }

    override fun render(
        state: SCERenderState,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        matrices.push()
        matrices.scale(2f, 2f, 2f)
        matrices.multiply(this.dispatcher.rotation)
        state.itemRenderState.render(matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV)
        matrices.pop()

        super.render(state, matrices, vertexConsumers, light)
    }
}
