package com.sarahisweird.vis_natura.datagen

import com.sarahisweird.vis_natura.VNBlocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class VisNaturaBlockLootTableProvider(
    dataOutput: FabricDataOutput,
    registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricBlockLootTableProvider(dataOutput, registryLookup) {
    override fun generate() {
        addDrop(VNBlocks.ALTAR)
        addDrop(VNBlocks.CENSER)
        addDrop(VNBlocks.CRYSTALLIZER)
    }
}
