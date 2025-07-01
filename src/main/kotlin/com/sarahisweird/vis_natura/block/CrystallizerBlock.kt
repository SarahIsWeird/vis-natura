package com.sarahisweird.vis_natura.block

import com.mojang.serialization.MapCodec
import com.sarahisweird.vis_natura.VNBlocks
import com.sarahisweird.vis_natura.VNItems
import com.sarahisweird.vis_natura.util.seconds
import com.sarahisweird.vis_natura.vis.VisType
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.particle.DustParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.RegistryWrapper
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

class CrystallizerBlock(settings: Settings) : BlockWithEntity(settings) {
    companion object {
        val LIT: BooleanProperty = Properties.LIT

        private val SHAPE by lazy(::makeShape)
        private fun makeShape(): VoxelShape {
            var shape = VoxelShapes.empty()

            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.125, 0.0, 0.125, 0.25, 0.5, 0.25), BooleanBiFunction.OR)
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.125, 0.0, 0.75, 0.25, 0.5, 0.875), BooleanBiFunction.OR)
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.75, 0.0, 0.75, 0.875, 0.5, 0.875), BooleanBiFunction.OR)
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.75, 0.0, 0.125, 0.875, 0.5, 0.25), BooleanBiFunction.OR)
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.0625, 0.5, 0.0625, 0.9375, 0.625, 0.9375), BooleanBiFunction.OR)
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.8125, 0.625, 0.0625, 0.9375, 0.875, 0.9375), BooleanBiFunction.OR)
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.0625, 0.625, 0.0625, 0.1875, 0.875, 0.9375), BooleanBiFunction.OR)
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.1875, 0.625, 0.0625, 0.8125, 0.875, 0.1875), BooleanBiFunction.OR)
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.1875, 0.625, 0.8125, 0.8125, 0.875, 0.9375), BooleanBiFunction.OR)
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.3125, 0.625, 0.3125, 0.6875, 0.75, 0.6875), BooleanBiFunction.OR)

            return shape
        }
    }

    init {
        defaultState = defaultState.with(LIT, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(LIT)
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

    override fun getCodec(): MapCodec<out BlockWithEntity?>? {
        return createCodec(::CrystallizerBlock)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return CrystallizerBlockEntity(pos, state)
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hit: BlockHitResult
    ): ActionResult {
        val entity = world.getBlockEntity(pos) as? CrystallizerBlockEntity
            ?: return super.onUse(state, world, pos, player, hit)

        if (entity.inventory.stack.isEmpty) {
            return ActionResult.CONSUME
        }

        val stack = entity.inventory.yoinkStack()
        player.inventory.insertStack(stack)

        return ActionResult.SUCCESS
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
        val entity = world.getBlockEntity(pos) as? CrystallizerBlockEntity
            ?: return super.onUseWithItem(stack, state, world, pos, player, hand, hit)

        if (!world.fuelRegistry.isFuel(stack) && !stack.isOf(Items.WATER_BUCKET)) {
            return this.onUse(state, world, pos, player, hit)
        }

        if (world.isClient) return ActionResult.SUCCESS

        val newStack = entity.tryConsume(stack)
        if (stack != newStack) {
            player.inventory.selectedStack = stack
            return ActionResult.SUCCESS
        }

        return ActionResult.FAIL
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        if (!state[LIT]) return

        val x = pos.x + 0.5
        val y = pos.y + 4.0 / 16.0
        val z = pos.z + 0.5
        if (random.nextDouble() < 0.1) {
            world.playSoundClient(
                x, y, z,
                SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE,
                SoundCategory.BLOCKS,
                1.0f, 1.0f,
                false
            )
        }

        val particleOffsetX = random.nextDouble() * 0.4 - 0.2
        val particleOffsetY = random.nextDouble() * 0.25 - 0.125
        val particleOffsetZ = random.nextDouble() * 0.4 - 0.2

        val particleX = x + particleOffsetX
        val particleY = y + particleOffsetY
        val particleZ = z + particleOffsetZ

        // I wanted to also spawn smoke particles here like for furnaces, but they rise, clipping into the basin. :(
        world.addParticleClient(
            ParticleTypes.FLAME,
            particleX, particleY, particleZ,
            0.0, 0.0, 0.0)

        spawnVisParticle(world, pos, random)

    }

    private fun spawnVisParticle(world: World, pos: BlockPos, random: Random) {
        val entity = world.getBlockEntity(pos) as? CrystallizerBlockEntity ?: return
        if (entity.inventory.stack.count == entity.inventory.stack.maxCount) return

        val visColor = entity.biomeVisType?.color ?: return

        val xOffset = random.nextDouble() * 0.4 - 0.2
        val zOffset = random.nextDouble() * 0.4 - 0.2

        val x = pos.x + 0.5 + xOffset
        val y = pos.y + 13.0 / 16.0
        val z = pos.z + 0.5 + zOffset

        val visParticleEffect = DustParticleEffect(visColor, 0.5f)
        world.addParticleClient(visParticleEffect,
            x, y, z,
            0.0, 0.75, 0.0)
    }

    override fun <T : BlockEntity> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return validateTicker(type, VNBlocks.BE_CRYSTALLIZER, CrystallizerBlockEntity::tick)
    }
}

class CrystallizerBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(VNBlocks.BE_CRYSTALLIZER, pos, state) {
    companion object {
        private val LOGGER = LogManager.getLogger("VisNatura/CrystallizerBlockEntity")
        private val BIOME_UPDATE_FREQUENCY = 5.seconds

        fun tick(world: World, pos: BlockPos, state: BlockState, entity: CrystallizerBlockEntity) {
            if (entity.fuelTicks == 0) return

            val forceUpdateVisType = entity.visLevel == 0 && entity.biomeVisType == null
            entity.updateBiomeVisType(world = world, forceUpdate = forceUpdateVisType)

            val visType = entity.biomeVisType
            if (visType == null) {
                entity.updateFuelTicks(entity.fuelTicks - 1)
                return
            }

            if (entity.inventory.stack.count == entity.inventory.stack.maxCount) {
                entity.updateFuelTicks(entity.fuelTicks - 1)
                return
            }

            entity.visLevel++
            if (entity.visLevel >= entity.visCapacity) {
                val stack =
                    if (entity.inventory.stack.isEmpty)
                        VNItems.CRYSTALS.getValue(visType).defaultStack
                    else
                        entity.inventory.stack.apply { increment(1) }

                entity.inventory.stack = stack
                entity.visLevel = 0
            }

            entity.updateFuelTicks(entity.fuelTicks - 1)
        }
    }

    inner class CrystallizerStorage : SingleStackStorage() {
        private var itemStack = ItemStack.EMPTY

        public override fun getStack(): ItemStack {
            return itemStack
        }

        public override fun setStack(stack: ItemStack) {
            itemStack = stack
            this.onFinalCommit()
        }

        fun yoinkStack(): ItemStack {
            val stack = this.itemStack
            this.itemStack = ItemStack.EMPTY
            this.onFinalCommit()
            return stack
        }

        override fun supportsInsertion(): Boolean {
            return false
        }

        override fun canInsert(variant: ItemVariant): Boolean {
            return false
        }

        override fun onFinalCommit() {
            this@CrystallizerBlockEntity.markDirty()
        }
    }

    private var biomeUpdateTicks = 0

    private var maxFuelTicks = 1600 * 16 // == 16 coal items
    private var fuelTicks = 0
    val visCapacity = 800
    var visLevel = 0
        private set

    var biomeVisType: VisType? = null
        private set

    val inventory = CrystallizerStorage()

    private fun updateBiomeVisType(world: World? = this.world, forceUpdate: Boolean = false) {
        if (this.biomeUpdateTicks < BIOME_UPDATE_FREQUENCY) {
            this.biomeUpdateTicks++
            if (!forceUpdate) return
        } else {
            this.biomeUpdateTicks = 0
        }

        if (forceUpdate) {
            this.biomeUpdateTicks = 0
        }

        val biome = world?.getBiome(this.pos) ?: return

        val oldType = this.biomeVisType
        this.biomeVisType = VisType.getByBiome(biome)

        if (oldType != this.biomeVisType) {
            this.markDirty()
        }
    }

    fun tryConsume(stack: ItemStack): ItemStack {
        if (stack.isEmpty) return stack

        val fuelRegistry = world?.fuelRegistry ?: return stack
        if (!fuelRegistry.isFuel(stack)) {
            if (stack.isOf(Items.WATER_BUCKET)) {
                this.updateFuelTicksFromPlayer(0)
            }

            return stack
        }

        val additionalTicks = fuelRegistry.getFuelTicks(stack)

        if (this.fuelTicks + additionalTicks >= this.maxFuelTicks) {
            return stack
        }


        this.updateBiomeVisType(forceUpdate = true)
        this.updateFuelTicksFromPlayer(this.fuelTicks + additionalTicks)

        stack.decrement(1)
        return if (stack.isEmpty) ItemStack.EMPTY else stack
    }

    private fun updateFuelTicks(newTicks: Int) {
        this.fuelTicks = newTicks
        this.markDirty()

        val world = this.world ?: return

        val newBlockState = world.getBlockState(this.pos)
            .with(CrystallizerBlock.LIT, this.fuelTicks > 0)
        world.setBlockState(this.pos, newBlockState)
    }

    private fun updateFuelTicksFromPlayer(newTicks: Int) {
        val prevTicks = this.fuelTicks
        val wasExtinguished = newTicks == 0

        this.updateFuelTicks(newTicks)
        this.updateBiomeVisType()

        if (prevTicks > 0 && wasExtinguished) {
            playStateUpdateSound(SoundEvents.BLOCK_FIRE_EXTINGUISH)
        } else if (newTicks > 0) {
            playStateUpdateSound(SoundEvents.ITEM_FIRECHARGE_USE)
        }
    }

    private fun playStateUpdateSound(soundEvent: SoundEvent) {
        this.world?.playSound(
            null,
            this.pos,
            soundEvent,
            SoundCategory.BLOCKS,
            0.25f,
            1.0f
        )
    }

    override fun readNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup) {
        super.readNbt(nbt, registries)

        this.fuelTicks = nbt.getInt("fuel_ticks", 0)
        this.visLevel = nbt.getInt("vis_level", 0)
        this.biomeVisType = nbt["biome_vis_type", VisType.CODEC].getOrNull()
        this.inventory.stack = nbt["crystals", ItemStack.CODEC].getOrElse { ItemStack.EMPTY }
    }

    override fun writeNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup) {
        super.writeNbt(nbt, registries)

        nbt.putInt("fuel_ticks", this.fuelTicks)
        nbt.putInt("vis_level", this.visLevel)
        nbt.putNullable("biome_vis_type", VisType.CODEC, this.biomeVisType)

        if (!this.inventory.stack.isEmpty) {
            nbt.put("crystals", ItemStack.CODEC, this.inventory.stack)
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

        val state = world?.getBlockState(pos) ?: return
        world?.updateListeners(pos, state, state, 0)
    }
}
