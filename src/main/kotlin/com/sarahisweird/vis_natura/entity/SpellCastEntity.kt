package com.sarahisweird.vis_natura.entity

import com.sarahisweird.vis_natura.VisNatura
import com.sarahisweird.vis_natura.spell.DirectBlockHitInfo
import com.sarahisweird.vis_natura.spell.DirectEntityHitInfo
import com.sarahisweird.vis_natura.spell.Spell
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager
import java.util.*
import kotlin.jvm.optionals.getOrNull

class SpellCastEntity(
    entityType: EntityType<SpellCastEntity>,
    world: World
) : ProjectileEntity(entityType, world) {
    companion object {
        private val LOGGER = LogManager.getLogger(SpellCastEntity::class.java)

        private val SPELL_TYPE = DataTracker.registerData(
            SpellCastEntity::class.java,
            VNTrackedDataHandlers.SPELL_TYPE)
    }

    constructor(
        entityType: EntityType<SpellCastEntity>,
        world: World,
        pos: Vec3d,
        spellId: Identifier
    ) : this(entityType, world) {
        this.setPosition(pos)
        this.spellId = spellId
    }

    var spellId: Identifier?
        get() = this.dataTracker[SPELL_TYPE].getOrNull()
        set(value) {
            this.dataTracker[SPELL_TYPE] = Optional.ofNullable(value)
        }

    private fun getSpell(): Spell? {
        val spellId = this.spellId
        if (spellId == null) {
            LOGGER.error("Tried to resolve spell for 'null' spell id!")
            return null
        }

        val spell = VisNatura.spellRegistry[spellId]
        if (spell == null) {
            LOGGER.error("No spell impl registered for spell id '{}'!", spellId)
            return null
        }

        return spell
    }

    override fun damage(world: ServerWorld, source: DamageSource, amount: Float): Boolean {
        return false
    }

    override fun tick() {
        val owner = this.owner
        // TODO: this.applyDrag()

        val canTick = world.isClient
                || ((owner == null || !owner.isRemoved) && world.isChunkLoaded(this.blockPos))

        if (!canTick) {
            this.discard()
            return
        }

        // We need to reimplement ProjectileUtil#getCollision here because of fluids :')
        val hitResult = this.getCollision()

        val newPosition = if (hitResult.type != HitResult.Type.MISS) {
            hitResult.pos
        } else {
            this.pos.add(this.velocity)
        }

        ProjectileUtil.setRotationFromVelocity(this, 0.2f)
        this.setPosition(newPosition)
        this.tickBlockCollision()
        super.tick()

        if (hitResult.type != HitResult.Type.MISS && this.isAlive) {
            this.hitOrDeflect(hitResult)
        }
    }

    private fun getCollision(): HitResult {
        var end = this.pos.add(this.velocity)
        val hitResult: HitResult = this.world.getCollisionsIncludingWorldBorder(RaycastContext(
            this.pos,
            end,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.ANY,
            this,
        ))

        if (hitResult.type != HitResult.Type.MISS) {
            end = hitResult.pos
        }

        val entityHitResult = this.getEntityCollision(
            this.pos,
            end,
            this.boundingBox.stretch(velocity).expand(1.0))

        return entityHitResult ?: hitResult
    }

    private fun getEntityCollision(min: Vec3d, max: Vec3d, box: Box): EntityHitResult? {
        var distance = Double.MAX_VALUE
        var hitPos: Vec3d? = null
        var hitEntity: Entity? = null

        for (entity in this.world.getOtherEntities(this, box, this::canHit)) {
            val hit = entity.boundingBox.expand(0.3).raycast(min, max).getOrNull() ?: continue
            val currDistance = min.squaredDistanceTo(hit)
            if (currDistance < distance) {
                hitEntity = entity
                distance = currDistance
                hitPos = hit
            }
        }

        return hitEntity?.let { EntityHitResult(it, hitPos) }
    }

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)

        val world = this.world as? ServerWorld ?: return
        val spell = this.getSpell() ?: return
        val playerOwner = this.owner as? PlayerEntity

        val hitInfo = DirectEntityHitInfo(
            world,
            playerOwner,
            entityHitResult.pos,
            entityHitResult.entity)

        this.discard()
        spell.onDirectEntityHit(hitInfo)
    }

    override fun onBlockHit(blockHitResult: BlockHitResult) {
        super.onBlockHit(blockHitResult)

        val world = this.world as? ServerWorld ?: return
        val spell = this.getSpell() ?: return
        val playerOwner = this.owner as? PlayerEntity

        val hitInfo = DirectBlockHitInfo(
            world,
            playerOwner,
            blockHitResult.blockPos)

        this.discard()
        spell.onDirectBlockHit(hitInfo)
    }

    override fun canHit(entity: Entity): Boolean {
        return super.canHit(entity) && !entity.noClip
    }

    override fun initDataTracker(builder: DataTracker.Builder) {
        builder.add(SPELL_TYPE, Optional.empty())
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)

        val spellId = this.spellId
        if (spellId != null) {
            nbt.put("SpellId", Identifier.CODEC, spellId)
        } else {
            nbt.remove("SpellId")
        }
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        this.spellId = nbt["SpellId", Identifier.CODEC].getOrNull()
    }
}
