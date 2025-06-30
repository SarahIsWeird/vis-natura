package com.sarahisweird.vis_natura.spell

import com.sarahisweird.vis_natura.vis.VisType
import net.minecraft.particle.DustParticleEffect
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.intprovider.UniformIntProvider
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import java.util.*

abstract class Spell(
    val id: Identifier,
    protected val spellVisTypes: EnumSet<VisType>,
) {
    protected val random: Random = Random.create()

    constructor(id: Identifier, visType: VisType)
            : this(id, EnumSet.of(visType))
    constructor(id: Identifier, firstVisType: VisType, vararg visTypes: VisType)
            : this(id, EnumSet.of(firstVisType, *visTypes))

    val visTypes: EnumSet<VisType>
        get() = spellVisTypes.clone()

    open fun onDirectBlockHit(hitInfo: DirectBlockHitInfo) {
        this.onBlockHit(hitInfo)
    }

    open fun onRangedBlockHit(hitInfo: RangedBlockHitInfo) {
        this.onBlockHit(hitInfo)
    }

    open fun onBlockHit(hitInfo: BlockCastInfo) {
        this.spawnParticles(hitInfo.world, hitInfo.blockPos.up().toCenterPos())
    }

    open fun onDirectEntityHit(hitInfo: DirectEntityHitInfo) {
        this.onEntityHit(hitInfo)
    }

    open fun onRangedEntityHit(hitInfo: RangedEntityHitInfo) {
        this.onEntityHit(hitInfo)
    }

    open fun onEntityHit(hitInfo: EntityCastInfo) {
        this.spawnParticles(hitInfo.world, hitInfo.pos)
    }

    open fun onSelfTarget(hitInfo: SelfTargetInfo) {
        // By default, we don't even spawn particles.
        // Some spells (like vitalize) override this, others don't.
    }

    protected fun spawnParticles(world: World, pos: Vec3d) {
        // todo: make custom particle effect, then i can use ParticleUtil again :3

        val count = UniformIntProvider.create(5, 10)[this.random]
        for (i in 0..<count) {
            val pos = pos.addRandom(this.random, 0.5f)

            world.addParticleClient(
                DustParticleEffect(spellVisTypes.random().color, 1f),
                pos.x, pos.y, pos.z,
                0.0, 1.0, 0.0,
            )
        }
    }

    fun onHit(castInfo: CastInfo) {
        when (castInfo) {
            is DirectBlockHitInfo -> this.onDirectBlockHit(castInfo)
            is RangedBlockHitInfo -> this.onRangedBlockHit(castInfo)
            is DirectEntityHitInfo -> this.onDirectEntityHit(castInfo)
            is RangedEntityHitInfo -> this.onRangedEntityHit(castInfo)
            is SelfTargetInfo -> this.onSelfTarget(castInfo)
        }
    }
}
