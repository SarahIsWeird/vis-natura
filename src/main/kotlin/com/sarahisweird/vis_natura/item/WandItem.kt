package com.sarahisweird.vis_natura.item

import com.sarahisweird.vis_natura.VisNatura
import com.sarahisweird.vis_natura.spell.Spell
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager

class WandItem(settings: Settings) : Item(settings) {
    companion object {
        private val logger = LogManager.getLogger("VisNatura/WandItem")
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): ActionResult {
        // doesnt do entities? :(
        val result = user.raycast(20.0, 1f, true)

        if (world.isClient) return ActionResult.SUCCESS

        val stack = user.getStackInHand(hand)
        val spellId = stack.getOrDefault(
            ItemComponents.SELECTED_SPELL,
            VisNatura.spellRegistry.getSpellIds().first())
        val spell = VisNatura.spellRegistry[spellId]
        if (spell == null) {
            logger.error("Failed to find spell with id $spellId!")
            return ActionResult.FAIL
        }

        when {
            result == null || result.type == HitResult.Type.MISS -> {
                val hitInfo = Spell.NoHitInfo(world, user)
                spell.onNothingHit(hitInfo)
            }
            result is BlockHitResult -> {
                val hitInfo = Spell.BlockHitInfo(world, user, result.blockPos)
                spell.onBlockHit(hitInfo)
            }
            result is EntityHitResult -> {
                val hitInfo = Spell.EntityHitInfo(world, user, result.pos, result.entity)
                spell.onEntityHit(hitInfo)
            }
        }

        return ActionResult.SUCCESS
    }
}
