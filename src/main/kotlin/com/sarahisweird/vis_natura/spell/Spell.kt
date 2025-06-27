package com.sarahisweird.vis_natura.spell

import com.sarahisweird.vis_natura.vis.VisType
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.DustParticleEffect
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.intprovider.UniformIntProvider
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager
import java.util.*

abstract class Spell(
    val id: Identifier,
    protected val visTypes: EnumSet<VisType>,
) {
    sealed class HitInfo(
        val world: World,
        val player: PlayerEntity?,
    )

    class NoHitInfo(
        world: World,
        player: PlayerEntity?,
    ) : HitInfo(world, player)

    class BlockHitInfo(
        world: World,
        player: PlayerEntity?,
        val blockPos: BlockPos,
    ) : HitInfo(world, player)

    class EntityHitInfo(
        world: World,
        player: PlayerEntity?,
        val pos: Vec3d,
        val entity: Entity,
    ) : HitInfo(world, player)

    protected val random: Random = Random.create()
    private val logger = LogManager.getLogger(Spell::class.java)

    constructor(id: Identifier, visType: VisType)
            : this(id, EnumSet.of(visType))
    constructor(id: Identifier, firstVisType: VisType, vararg visTypes: VisType)
            : this(id, EnumSet.of(firstVisType, *visTypes))

    open fun onBlockHit(hitInfo: BlockHitInfo) {
        this.spawnParticles(hitInfo.world, hitInfo.blockPos.up().toCenterPos())
    }

    open fun onEntityHit(hitInfo: EntityHitInfo) {
        this.spawnParticles(hitInfo.world, hitInfo.pos)
    }

    open fun onNothingHit(hitInfo: NoHitInfo) {
        // By default, we don't even spawn particles.
        // Some spells (like vitalize) override this, others don't.
    }

    protected fun spawnParticles(world: World, pos: Vec3d) {
        // todo: make custom particle effect, then i can use ParticleUtil again :3

        val count = UniformIntProvider.create(5, 10)[this.random]
        for (i in 0..<count) {
            val pos = pos.addRandom(this.random, 0.5f)

            world.addParticleClient(
                DustParticleEffect(visTypes.random().color, 1f),
                pos.x, pos.y, pos.z,
                0.0, 1.0, 0.0,
            )
        }
    }
}
