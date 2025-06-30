package com.sarahisweird.vis_natura.spell.impl

import com.sarahisweird.vis_natura.VisNatura
import com.sarahisweird.vis_natura.spell.BlockCastInfo
import com.sarahisweird.vis_natura.spell.EntityCastInfo
import com.sarahisweird.vis_natura.spell.Spell
import com.sarahisweird.vis_natura.vis.VisType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d

object WarpSpell : Spell(
    VisNatura.id("warp"),
    VisType.VIS_ABYSSI,
    VisType.VIS_ARBORUM,
    VisType.VIS_INUSITATA,
    VisType.VIS_SIMPLEX,
) {
    override fun onBlockHit(hitInfo: BlockCastInfo) {
        super.onBlockHit(hitInfo)
        this.teleportPlayerTo(hitInfo.player, hitInfo.blockPos.up().toCenterPos())
    }

    override fun onEntityHit(hitInfo: EntityCastInfo) {
        super.onEntityHit(hitInfo)
        this.teleportPlayerTo(hitInfo.player, hitInfo.pos)
    }

    private fun teleportPlayerTo(player: PlayerEntity?, pos: Vec3d) {
        if (player == null) return

        player.teleport(pos.x, pos.y, pos.z, true)
    }
}
