package com.sarahisweird.vis_natura.entity

import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.util.Identifier
import java.util.*

object VNTrackedDataHandlers {
    val SPELL_TYPE: TrackedDataHandler<Optional<Identifier>> =
        TrackedDataHandler.create(PacketCodecs.optional(Identifier.PACKET_CODEC))
}
