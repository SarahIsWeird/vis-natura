package com.sarahisweird.vis_natura

import com.sarahisweird.vis_natura.block.CrystallizerBlock
import com.sarahisweird.vis_natura.block.CrystallizerBlockEntity
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.MapColor
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.sound.BlockSoundGroup

object VNBlocks {
    val CRYSTALLIZER = register("crystallizer", ::CrystallizerBlock,
        AbstractBlock.Settings.create()
            .strength(2.5f)
            .mapColor(MapColor.IRON_GRAY)
            .requiresTool()
            .sounds(BlockSoundGroup.STONE)
            .luminance { state ->
                if (state[CrystallizerBlock.LIT, false]) {
                    10
                } else {
                    0
                }
            })
    val BE_CRYSTALLIZER = registerBE("crystallizer", ::CrystallizerBlockEntity, CRYSTALLIZER)

    fun init() {
        ItemGroupEvents.modifyEntriesEvent(VNItems.ITEM_GROUP_KEY).register { group ->
            group.add(CRYSTALLIZER)
        }
    }

    private fun register(name: String, factory: (AbstractBlock.Settings) -> Block, settings: AbstractBlock.Settings, registerItem: Boolean = true): Block {
        val id = VisNatura.id(name)
        val key = RegistryKey.of(RegistryKeys.BLOCK, id)
        val block = factory(settings.registryKey(key))

        if (registerItem) {
            val itemKey = RegistryKey.of(RegistryKeys.ITEM, id)
            val item = BlockItem(block, Item.Settings()
                .registryKey(itemKey)
                .useBlockPrefixedTranslationKey())

            Registry.register(Registries.ITEM, itemKey, item)
        }

        return Registry.register(Registries.BLOCK, key, block)
    }

    private fun <T : BlockEntity> registerBE(
        name: String,
        factory: FabricBlockEntityTypeBuilder.Factory<T>,
        vararg blocks: Block
    ): BlockEntityType<T> {
        val id = VisNatura.id(name)
        return Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            id,
            FabricBlockEntityTypeBuilder.create(factory)
                .addBlocks(*blocks)
                .build()
        )
    }
}
