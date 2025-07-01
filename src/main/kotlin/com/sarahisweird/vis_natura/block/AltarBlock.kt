package com.sarahisweird.vis_natura.block

import com.mojang.serialization.MapCodec
import com.sarahisweird.vis_natura.VNBlocks
import com.sarahisweird.vis_natura.VNItems
import com.sarahisweird.vis_natura.util.ShapeUtil
import com.sarahisweird.vis_natura.util.takeIfOrElse
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager
import kotlin.jvm.optionals.getOrElse

class AltarBlock(settings: Settings) : BlockWithEntity(settings) {
    companion object {
        private val LOGGER = LogManager.getLogger("AltarBlock")

        private val SHAPE = ShapeUtil.union {
            centeredPosSize(0, 0, 16, 8)
            centeredPosSize(2, 8, 14, 10)
            centeredPosSize(4, 10, 12, 12)
        }
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext,
    ): VoxelShape {
        return SHAPE
    }

    override fun getCollisionShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext,
    ): VoxelShape {
        return SHAPE
    }

    override fun getCullingShape(state: BlockState): VoxelShape {
        return SHAPE
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hit: BlockHitResult,
    ): ActionResult {
        return this.onUseWithItem(ItemStack.EMPTY, state, world, pos, player, Hand.MAIN_HAND, hit)
    }

    override fun onUseWithItem(
        stack: ItemStack,
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult,
    ): ActionResult {
        val altarBE = world.getBlockEntity(pos) as? AltarBlockEntity
        if (altarBE == null) {
            LOGGER.error("AltarBlock at ${pos.x}/${pos.y}/${pos.z} doesn't have an associated AltarBlockEntity!")
            return ActionResult.FAIL
        }

        val playerWand = stack.takeIfOrElse(ItemStack.EMPTY) { it.isOf(VNItems.WAND) }
        val altarWand = altarBE.inventory.stack

        if (playerWand.isEmpty && altarWand.isEmpty) {
            return ActionResult.PASS
        }

        if (world.isClient) {
            return ActionResult.SUCCESS
        }

        val altarHasWand = !altarWand.isEmpty
        val playerHasWand = !playerWand.isEmpty
        val playerHandIsEmpty = stack.isEmpty

        when {
            !altarHasWand -> {
                // Altar is empty -> move stack from player to altar
                altarBE.inventory.stack = playerWand
                player.inventory.selectedStack = ItemStack.EMPTY
            }
            playerHandIsEmpty -> {
                // Player hand is empty -> move stack from altar to player hand
                player.setStackInHand(hand, altarWand)
                altarBE.inventory.stack = ItemStack.EMPTY
            }
            altarHasWand && playerHasWand -> {
                // Two wands -> simply swap the stacks
                player.setStackInHand(hand, altarWand)
                altarBE.inventory.stack = playerWand
            }
            else -> {
                // Two items, but the player item is NOT a wand -> try to insert into the inventory
                if (!player.inventory.insertStack(altarWand)) {
                    // No space left! We don't want to drop the wand on the floor, so we just quit trying.
                    return ActionResult.FAIL
                } else {
                    altarBE.inventory.stack = ItemStack.EMPTY
                }
            }
        }

        return ActionResult.SUCCESS
    }

    override fun getCodec(): MapCodec<out BlockWithEntity> {
        return createCodec(::AltarBlock)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return AltarBlockEntity(pos, state)
    }
}

class AltarBlockEntity(
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(VNBlocks.BE_ALTAR, pos, state) {
    inner class AltarStorage : SingleStackStorage() {
        private var itemStack = ItemStack.EMPTY

        public override fun getStack(): ItemStack {
            return this.itemStack
        }

        public override fun setStack(stack: ItemStack) {
            this.itemStack = stack
            this.onFinalCommit()
        }

        override fun canInsert(itemVariant: ItemVariant): Boolean {
            return itemVariant.isOf(VNItems.WAND)
        }

        override fun onFinalCommit() {
            this@AltarBlockEntity.markDirty()
        }
    }

    val inventory = AltarStorage()

    override fun readNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup) {
        super.readNbt(nbt, registries)

        this.inventory.stack = nbt["inventory", ItemStack.CODEC].getOrElse { ItemStack.EMPTY }
    }

    override fun writeNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup) {
        super.writeNbt(nbt, registries)

        if (!this.inventory.stack.isEmpty) {
            nbt.put("inventory", ItemStack.CODEC, this.inventory.stack)
        }
    }

    override fun toInitialChunkDataNbt(registries: RegistryWrapper.WrapperLookup): NbtCompound {
        return this.createNbt(registries)
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener> {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun markDirty() {
        super.markDirty()

        val world = this.world ?: return
        val state = world.getBlockState(this.pos)
        world.updateListeners(this.pos, state, state, 0)
    }
}
