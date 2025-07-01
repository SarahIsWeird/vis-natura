package com.sarahisweird.vis_natura.datagen

import com.sarahisweird.vis_natura.VNItems
import com.sarahisweird.vis_natura.VNTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class VisNaturaTagProvider(
    output: FabricDataOutput,
    future: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricTagProvider<Item>(
    output,
    RegistryKeys.ITEM,
    future,
) {
    override fun configure(wrapperLookup: RegistryWrapper.WrapperLookup) {
        val crystalTagBuilder = this.getOrCreateTagBuilder(VNTags.VIS_CRYSTALS)
        VNItems.CRYSTALS.values.forEach(crystalTagBuilder::add)
        crystalTagBuilder.setReplace(false)
    }
}
