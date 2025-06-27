package com.sarahisweird.vis_natura.item

import com.mojang.serialization.Codec
import com.sarahisweird.vis_natura.VisNatura
import com.sarahisweird.vis_natura.vis.VisType
import net.minecraft.component.ComponentType
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ItemComponents {
    val SELECTED_SPELL: ComponentType<Identifier> = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        VisNatura.id("selected_spell"),
        ComponentType.builder<Identifier>()
            .codec(Identifier.CODEC)
            .packetCodec(Identifier.PACKET_CODEC)
            .build())

    val ABYSSI_CHARGE = registerVisComponent(VisType.VIS_ABYSSI)
    val AQUAE_CHARGE = registerVisComponent(VisType.VIS_AQUAE)
    val ARBORUM_CHARGE = registerVisComponent(VisType.VIS_ARBORUM)
    val FUNGORUM_CHARGE = registerVisComponent(VisType.VIS_FUNGORUM)
    val IGNIS_CHARGE = registerVisComponent(VisType.VIS_IGNIS)
    val INUSITATA_CHARGE = registerVisComponent(VisType.VIS_INUSITATA)
    val SIMPLEX_CHARGE = registerVisComponent(VisType.VIS_SIMPLEX)

    private fun registerVisComponent(type: VisType): ComponentType<Int> {
        return Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            VisNatura.id(type.visName),
            ComponentType.builder<Int>()
                .codec(Codec.INT)
                .packetCodec(PacketCodecs.INTEGER)
                .build(),
        )
    }

    fun init() {
        // Do nothing.
    }
}
