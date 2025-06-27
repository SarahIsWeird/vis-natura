package com.sarahisweird.vis_natura.datagen

import com.sarahisweird.vis_natura.VNItems
import com.sarahisweird.vis_natura.vis.VisType
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.client.data.BlockStateModelGenerator
import net.minecraft.client.data.ItemModelGenerator
import net.minecraft.client.data.Models

class VisNaturaModelProvider(
    output: FabricDataOutput,
) : FabricModelProvider(output) {
    override fun getName(): String {
        return "VisNatura Model Provider"
    }

    override fun generateBlockStateModels(gen: BlockStateModelGenerator) {
        // unused for now
    }

    override fun generateItemModels(gen: ItemModelGenerator) {
        gen.register(VNItems.WAND, Models.GENERATED)

        for (visType in VisType.entries) {
            gen.register(VNItems.CRYSTALS[visType], Models.GENERATED)
        }
    }
}
