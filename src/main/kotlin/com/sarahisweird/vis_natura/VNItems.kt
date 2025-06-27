package com.sarahisweird.vis_natura

import com.sarahisweird.vis_natura.item.VisCrystalItem
import com.sarahisweird.vis_natura.item.WandItem
import com.sarahisweird.vis_natura.vis.VisType
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object VNItems {
    val WAND = register("wand", ::WandItem, Item.Settings())

    val CRYSTAL_ABYSSI = register("vis_abyssi_crystal", VisCrystalItem.getFactory(VisType.VIS_ABYSSI), Item.Settings())
    val CRYSTAL_AQUAE = register("vis_aquae_crystal", VisCrystalItem.getFactory(VisType.VIS_AQUAE), Item.Settings())
    val CRYSTAL_ARBORUM = register("vis_arborum_crystal", VisCrystalItem.getFactory(VisType.VIS_ARBORUM), Item.Settings())
    val CRYSTAL_FUNGORUM = register("vis_fungorum_crystal", VisCrystalItem.getFactory(VisType.VIS_FUNGORUM), Item.Settings())
    val CRYSTAL_IGNIS = register("vis_ignis_crystal", VisCrystalItem.getFactory(VisType.VIS_IGNIS), Item.Settings())
    val CRYSTAL_INUSITATA = register("vis_inusitata_crystal", VisCrystalItem.getFactory(VisType.VIS_INUSITATA), Item.Settings())
    val CRYSTAL_SIMPLEX = register("vis_simplex_crystal", VisCrystalItem.getFactory(VisType.VIS_SIMPLEX), Item.Settings())

    val CRYSTALS =
        mapOf(
            VisType.VIS_ABYSSI to CRYSTAL_ABYSSI,
            VisType.VIS_AQUAE to CRYSTAL_AQUAE,
            VisType.VIS_ARBORUM to CRYSTAL_ARBORUM,
            VisType.VIS_FUNGORUM to CRYSTAL_FUNGORUM,
            VisType.VIS_IGNIS to CRYSTAL_IGNIS,
            VisType.VIS_INUSITATA to CRYSTAL_INUSITATA,
            VisType.VIS_SIMPLEX to CRYSTAL_SIMPLEX,
        )

    val ITEM_GROUP_KEY: RegistryKey<ItemGroup> = RegistryKey.of(RegistryKeys.ITEM_GROUP, VisNatura.id("item_group"))
    val ITEM_GROUP: ItemGroup = FabricItemGroup.builder()
        .displayName(Text.translatable("itemGroup.vis_natura"))
        .icon { ItemStack(WAND) }
        .build()

    fun init() {
        Registry.register(Registries.ITEM_GROUP, ITEM_GROUP_KEY, ITEM_GROUP)

        ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP_KEY).register { group ->
            group.add(WAND)

            for (visType in VisType.entries) {
                group.add(CRYSTALS[visType])
            }
        }
    }

    private fun register(name: String, factory: (Item.Settings) -> Item, settings: Item.Settings): Item {
        val key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(VisNatura.MOD_ID, name))
        val item = factory(settings.registryKey(key))
        return Registry.register(Registries.ITEM, key, item)
    }
}
