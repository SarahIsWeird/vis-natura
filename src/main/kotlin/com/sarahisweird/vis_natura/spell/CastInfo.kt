package com.sarahisweird.vis_natura.spell

import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

sealed class CastInfo(
    val world: World,
    val player: PlayerEntity?,
)

class SelfTargetInfo(
    world: World,
    player: PlayerEntity?,
) : CastInfo(world, player)

sealed class BlockCastInfo(
    world: World,
    player: PlayerEntity?,
    val blockPos: BlockPos,
) : CastInfo(world, player)

class DirectBlockHitInfo(
    world: World,
    player: PlayerEntity?,
    blockPos: BlockPos,
) : BlockCastInfo(world, player, blockPos)

class RangedBlockHitInfo(
    world: World,
    player: PlayerEntity?,
    blockPos: BlockPos,
) : BlockCastInfo(world, player, blockPos)

sealed class EntityCastInfo(
    world: World,
    player: PlayerEntity?,
    val pos: Vec3d,
    val entity: Entity,
) : CastInfo(world, player)

class DirectEntityHitInfo(
    world: World,
    player: PlayerEntity?,
    pos: Vec3d,
    entity: Entity,
) : EntityCastInfo(world, player, pos, entity)

class RangedEntityHitInfo(
    world: World,
    player: PlayerEntity?,
    pos: Vec3d,
    entity: Entity,
) : EntityCastInfo(world, player, pos, entity)
