package com.sarahisweird.vis_natura.networking

import com.sarahisweird.vis_natura.VNItems
import com.sarahisweird.vis_natura.VisNatura
import com.sarahisweird.vis_natura.item.ItemComponents
import com.sarahisweird.vis_natura.networking.payloads.ChangeSelectedSpellC2SPayload
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import org.apache.logging.log4j.LogManager

object PacketHandler {
    private val logger = LogManager.getLogger("VisNatura/PacketHandler")

    fun receiveChangeSelectedSpellPacket(payload: ChangeSelectedSpellC2SPayload, context: ServerPlayNetworking.Context) {
        if (VisNatura.spellRegistry[payload.selectedSpellId] == null) {
            logger.warn("Received ChangeSelectedSpellC2S with invalid spell id '${payload.selectedSpellId}'!")
            return
        }

        val player = context.player()
        val wandItem = player.inventory.getStack(payload.wandSlot)
        if (!wandItem.isOf(VNItems.WAND)) {
            logger.warn("Received ChangeSelectedSpellC2S with slot id ${payload.wandSlot}, but that slot doesn't have a wand!")
            return
        }

        wandItem[ItemComponents.SELECTED_SPELL] = payload.selectedSpellId
    }
}
