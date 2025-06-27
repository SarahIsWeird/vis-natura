package com.sarahisweird.vis_natura.vis

import com.sarahisweird.vis_natura.VNItems
import com.sarahisweird.vis_natura.VisNatura
import com.sarahisweird.vis_natura.item.ItemComponents
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.StringIdentifiable
import net.minecraft.world.biome.Biome

private fun getTagKey(name: String): TagKey<Biome> {
    return TagKey.of(RegistryKeys.BIOME, VisNatura.id(name))
}

enum class VisType(
    val color: Int,
) : StringIdentifiable {
    VIS_ABYSSI(0xff222222.toInt()),
    VIS_AQUAE(0xff5d49e1.toInt()),
    VIS_ARBORUM(0xff594421.toInt()),
    VIS_FUNGORUM(0xff7f7b6d.toInt()),
    VIS_IGNIS(0xfff96e36.toInt()),
    VIS_INUSITATA(0xffc536f9.toInt()),
    VIS_SIMPLEX(0xff19e54c.toInt()),
    ;

    companion object {
        val CODEC = StringIdentifiable.EnumCodec(entries.toTypedArray(), VisType::fromString)

        fun getByBiome(biome: RegistryEntry<Biome>): VisType? {
            return VisType.entries.firstOrNull { type -> biome.isIn(type.generationBiomeTag) }
        }

        fun fromString(name: String): VisType {
            return valueOf(name.uppercase())
        }

        fun init() {
            // ???
        }
    }

    val visName = name.lowercase()
    val typeName = visName.split("_")[1]

    val generationBiomeTag = getTagKey(visName)
    val item by lazy { VNItems.CRYSTALS[this] }
    val chargeComponentType by lazy {
        when (this) {
            VIS_ABYSSI -> ItemComponents.ABYSSI_CHARGE
            VIS_AQUAE -> ItemComponents.AQUAE_CHARGE
            VIS_ARBORUM -> ItemComponents.ARBORUM_CHARGE
            VIS_FUNGORUM -> ItemComponents.FUNGORUM_CHARGE
            VIS_IGNIS -> ItemComponents.IGNIS_CHARGE
            VIS_INUSITATA -> ItemComponents.INUSITATA_CHARGE
            VIS_SIMPLEX -> ItemComponents.SIMPLEX_CHARGE
        }
    }

    override fun asString(): String {
        return this.visName
    }
}
