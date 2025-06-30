package com.sarahisweird.vis_natura.spell.impl

import com.sarahisweird.vis_natura.VisNatura
import com.sarahisweird.vis_natura.spell.BlockCastInfo
import com.sarahisweird.vis_natura.spell.EntityCastInfo
import com.sarahisweird.vis_natura.spell.SelfTargetInfo
import com.sarahisweird.vis_natura.spell.Spell
import com.sarahisweird.vis_natura.util.PlantTransmuter
import com.sarahisweird.vis_natura.vis.VisType
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

object VitalizeSpell : Spell(VisNatura.id("vitalize"), VisType.VIS_SIMPLEX) {
    private const val RADIUS = 3.0
    private const val HEAL_STRENGTH = 6f

    override fun onBlockHit(hitInfo: BlockCastInfo) {
        if (PlantTransmuter.transmuteBlock(hitInfo.world, hitInfo.blockPos, VisType.VIS_SIMPLEX, ItemStack.EMPTY)) {
            return
        }

        super.onBlockHit(hitInfo)
        this.healAround(hitInfo.world, hitInfo.blockPos.up().toCenterPos())
    }

    override fun onEntityHit(hitInfo: EntityCastInfo) {
        super.onEntityHit(hitInfo)
        this.healAround(hitInfo.world, hitInfo.pos)
    }

    override fun onSelfTarget(hitInfo: SelfTargetInfo) {
        super.onSelfTarget(hitInfo)

        val pos = hitInfo.player?.pos ?: return
        this.healAround(hitInfo.world, pos)
        super.spawnParticles(hitInfo.world, hitInfo.player.eyePos)
    }

    private fun healAround(world: World, pos: Vec3d) {
        if (world.isClient || world !is ServerWorld) return

        val box = Box.from(pos).expand(RADIUS)
        val entities = world.getNonSpectatingEntities(LivingEntity::class.java, box) as List<LivingEntity>
        for (entity in entities) {
            if (entity.squaredDistanceTo(pos) > (RADIUS * RADIUS)) continue

            if (entity.hasInvertedHealingAndHarm()) {
                entity.damage(world, entity.damageSources.magic(), HEAL_STRENGTH)
            } else {
                entity.heal(HEAL_STRENGTH)
            }
        }
    }
}
