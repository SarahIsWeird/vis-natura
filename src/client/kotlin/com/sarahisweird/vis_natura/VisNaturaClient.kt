package com.sarahisweird.vis_natura

import com.sarahisweird.vis_natura.rendering.CrosshairVisRenderer
import com.sarahisweird.vis_natura.rendering.ber.CrystallizerBlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories

fun initializeClient() {
    CrosshairVisRenderer.register()

    BlockEntityRendererFactories.register(
        VNBlocks.BE_CRYSTALLIZER, ::CrystallizerBlockEntityRenderer
    )
}
