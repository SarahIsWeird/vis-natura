package com.sarahisweird.vis_natura

import com.sarahisweird.vis_natura.rendering.CrosshairVisRenderer
import com.sarahisweird.vis_natura.rendering.ber.CrystallizerBlockEntityRenderer
import com.sarahisweird.vis_natura.rendering.entity.SpellCastEntityRenderer
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories

fun initializeClient() {
    CrosshairVisRenderer.register()

    BlockEntityRendererFactories.register(
        VNBlocks.BE_CRYSTALLIZER, ::CrystallizerBlockEntityRenderer
    )

    EntityRendererRegistry.register(VisNatura.SPELL_CAST, ::SpellCastEntityRenderer)
}
