package com.sarahisweird.vis_natura.item

import com.sarahisweird.vis_natura.VisNatura
import com.sarahisweird.vis_natura.entity.SpellCastEntity
import com.sarahisweird.vis_natura.spell.DirectBlockHitInfo
import com.sarahisweird.vis_natura.spell.DirectEntityHitInfo
import com.sarahisweird.vis_natura.spell.SelfTargetInfo
import com.sarahisweird.vis_natura.spell.Spell
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager

class WandItem(settings: Settings) : Item(settings) {
    companion object {
        private val logger = LogManager.getLogger("VisNatura/WandItem")
    }

    private fun getSpellForHeldWand(user: PlayerEntity, hand: Hand): Spell? {
        val spellId = this.getSpellIdForHeldWand(user, hand)
        return this.getSpell(spellId)
    }

    private fun getSpellIdForHeldWand(user: PlayerEntity, hand: Hand): Identifier {
        val stack = user.getStackInHand(hand)
        return this.getSpellIdForStack(stack)
    }

    private fun getSpellForStack(stack: ItemStack): Spell? {
        val spellId = this.getSpellIdForStack(stack)
        return this.getSpell(spellId)
    }

    private fun getSpellIdForStack(stack: ItemStack): Identifier {
        return stack.getOrDefault(
            ItemComponents.SELECTED_SPELL,
            VisNatura.spellRegistry.getSpellIds().first())
    }

    private fun getSpell(spellId: Identifier): Spell? {
        val spell = VisNatura.spellRegistry[spellId]
        if (spell == null) {
            logger.error("Failed to find spell with id $spellId!")
            return null
        }

        return spell
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): ActionResult {
        if (world.isClient || world !is ServerWorld || user !is ServerPlayerEntity) return ActionResult.SUCCESS

        val hitResult = user.raycast(user.blockInteractionRange, 0f, true)
        val hitInfo = when {
            hitResult is BlockHitResult && hitResult.type != HitResult.Type.MISS ->
                DirectBlockHitInfo(world, user, hitResult.blockPos)
            hitResult is EntityHitResult && hitResult.type != HitResult.Type.MISS ->
                DirectEntityHitInfo(world, user, hitResult.pos, hitResult.entity)
            user.isSneaking ->
                SelfTargetInfo(world, user)
            else -> null
        }

        if (hitInfo == null) {
            return this.castRangedSpell(world, user, hand)
        }

        val spell = this.getSpell(this.getSpellIdForHeldWand(user, hand))
            ?: return ActionResult.FAIL

        spell.onHit(hitInfo)

        return ActionResult.SUCCESS
    }

    private fun castRangedSpell(world: World, user: PlayerEntity, hand: Hand): ActionResult {
        val spellId = this.getSpellIdForHeldWand(user, hand)

        val spellCast = SpellCastEntity(VisNatura.SPELL_CAST, world, user.eyePos, spellId)
        spellCast.setVelocity(user, user.pitch, user.yaw, 0f, 1f, 1f)
        spellCast.setPosition(spellCast.pos.add(spellCast.velocity.normalize()))
        world.spawnEntity(spellCast)

        return ActionResult.SUCCESS
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if (context.world.isClient) return ActionResult.SUCCESS

        val spell = this.getSpellForStack(context.stack) ?: return ActionResult.FAIL
        val hitInfo = DirectBlockHitInfo(context.world, context.player, context.blockPos)
        spell.onDirectBlockHit(hitInfo)

        return ActionResult.SUCCESS
    }

    override fun useOnEntity(
        stack: ItemStack,
        user: PlayerEntity,
        entity: LivingEntity,
        hand: Hand,
    ): ActionResult? {
        if (user.world.isClient) return ActionResult.SUCCESS

        val spell = getSpellForHeldWand(user, hand) ?: return ActionResult.FAIL
        val hitInfo = DirectEntityHitInfo(user.world, user, entity.pos, entity)
        spell.onDirectEntityHit(hitInfo)

        return ActionResult.SUCCESS
    }
}
