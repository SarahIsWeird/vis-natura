package com.sarahisweird.vis_natura

import com.sarahisweird.vis_natura.datagen.VisNaturaBlockLootTableProvider
import com.sarahisweird.vis_natura.datagen.VisNaturaModelProvider
import com.sarahisweird.vis_natura.datagen.VisNaturaTagProvider
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

// It seems like the data generator entrypoint can't just be a function ref :(
@Suppress("kotlin:S6516")
object VisNaturaDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider(::VisNaturaModelProvider)
        pack.addProvider(::VisNaturaTagProvider)
        pack.addProvider(::VisNaturaBlockLootTableProvider)
    }
}
