package com.sarahisweird.vis_natura.spell.impl

import com.sarahisweird.vis_natura.VisNatura
import com.sarahisweird.vis_natura.spell.BlockCastInfo
import com.sarahisweird.vis_natura.spell.EntityCastInfo
import com.sarahisweird.vis_natura.spell.Spell
import com.sarahisweird.vis_natura.vis.VisType
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnReason
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager

object SmiteSpell : Spell(VisNatura.id("smite"), VisType.VIS_IGNIS, VisType.VIS_AQUAE) {
    private val logger = LogManager.getLogger()

    override fun onBlockHit(hitInfo: BlockCastInfo) {
        super.onBlockHit(hitInfo)
        this.summonLightning(hitInfo.world, hitInfo.blockPos.up().toCenterPos())
    }

    override fun onEntityHit(hitInfo: EntityCastInfo) {
        super.onEntityHit(hitInfo)
        this.summonLightning(hitInfo.world, hitInfo.pos)
    }

    private fun summonLightning(world: World, pos: Vec3d) {
        val entity = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED)
        if (entity == null) {
            logger.error("Failed to summon lightning bolt: EntityType::create returned null?")
            return
        }

        entity.refreshPositionAfterTeleport(pos)
        world.spawnEntity(entity)
    }
}
