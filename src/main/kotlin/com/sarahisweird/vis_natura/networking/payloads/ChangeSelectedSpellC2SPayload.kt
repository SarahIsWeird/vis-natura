package com.sarahisweird.vis_natura.networking.payloads

import com.sarahisweird.vis_natura.VisNatura
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

@JvmRecord
data class ChangeSelectedSpellC2SPayload(
    val wandSlot: Int,
    val selectedSpellId: Identifier,
) : CustomPayload {
    companion object {
        val CHANGE_SELECTED_SPELL_PAYLOAD_ID = VisNatura.id("change_selected_spell")
        val ID = CustomPayload.Id<ChangeSelectedSpellC2SPayload>(CHANGE_SELECTED_SPELL_PAYLOAD_ID)
        val CODEC: PacketCodec<PacketByteBuf, ChangeSelectedSpellC2SPayload> =
            CustomPayload.codecOf(ChangeSelectedSpellC2SPayload::write, ::ChangeSelectedSpellC2SPayload)
    }

    constructor(buf: PacketByteBuf) : this(
        wandSlot = buf.readInt(),
        selectedSpellId = buf.readIdentifier(),
    )

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }

    private fun write(buf: PacketByteBuf) {
        buf.writeInt(this.wandSlot)
        buf.writeIdentifier(this.selectedSpellId)
    }
}
