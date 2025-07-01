package com.sarahisweird.vis_natura

import net.minecraft.item.Item
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey

object VNTags {
    val VIS_CRYSTALS: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, VisNatura.id("vis_crystals"))
}
