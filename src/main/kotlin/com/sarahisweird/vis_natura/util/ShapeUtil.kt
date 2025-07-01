package com.sarahisweird.vis_natura.util

import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

class ShapeBuilderContext internal constructor() {
    internal val parts = mutableListOf<VoxelShape>()

    fun posSize(x: Int, y: Int, z: Int, width: Int, height: Int, depth: Int) {
        parts.add(ShapeUtil.posSize(x, y, z, width, height, depth))
    }

    fun centeredPosSize(xz: Double, y: Double, size: Double, height: Double) {
        parts.add(ShapeUtil.centeredPosSize(xz, y, size, height))
    }

    fun centeredPosSize(xz: Int, y: Int, size: Int, height: Int) {
        parts.add(ShapeUtil.centeredPosSize(xz, y, size, height))
    }

    fun minMax(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int) {
        parts.add(ShapeUtil.minMax(minX, minY, minZ, maxX, maxY, maxZ))
    }

    fun centeredMinMax(minXZ: Int, minY: Int, maxXZ: Int, maxY: Int) {
        parts.add(ShapeUtil.centeredMinMax(minXZ, minY, maxXZ, maxY))
    }

    fun shape(voxelShape: VoxelShape, offsetX: Int = 0, offsetY: Int = 0, offsetZ: Int = 0) {
        parts.add(voxelShape.offset(offsetX / 16.0, offsetY / 16.0, offsetZ / 16.0))
    }

    fun union(builder: ShapeBuilderContext.() -> Unit) {
        parts.add(ShapeUtil.union(builder))
    }

    fun combined(function: BooleanBiFunction, builder: ShapeBuilderContext.() -> Unit) {
        parts.add(ShapeUtil.combined(function, builder))
    }
}

object ShapeUtil {
    fun union(builder: ShapeBuilderContext.() -> Unit): VoxelShape {
        return combined(BooleanBiFunction.OR, builder)
    }

    fun combined(
        function: BooleanBiFunction,
        builder: ShapeBuilderContext.() -> Unit
    ): VoxelShape {
        val ctx = ShapeBuilderContext()
        ctx.apply(builder)
        return combineAll(ctx.parts, function)
    }

    fun combineAll(shapes: Collection<VoxelShape>, function: BooleanBiFunction): VoxelShape {
        var finalShape = VoxelShapes.empty()

        for (shape in shapes) {
            finalShape = VoxelShapes.combine(finalShape, shape, function)
        }

        return finalShape.simplify()
    }

    fun posSize(x: Int, y: Int, z: Int, width: Int, height: Int, depth: Int): VoxelShape {
        return minMax(
            x, y, z,
            x + width, y + height, z + depth,
        )
    }

    fun centeredPosSize(xz: Int, y: Int, size: Int, height: Int): VoxelShape {
        return posSize(xz, y, xz, size, height, size)
    }

    fun centeredPosSize(xz: Double, y: Double, size: Double, height: Double): VoxelShape {
        return VoxelShapes.cuboid(
            xz / 16.0,
            y / 16.0,
            xz / 16.0,
            (xz + size) / 16.0,
            (y + height) / 16.0,
            (xz + size) / 16.0,
        )
    }

    fun minMax(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int): VoxelShape {
        return VoxelShapes.cuboid(
            minX / 16.0,
            minY / 16.0,
            minZ / 16.0,
            maxX / 16.0,
            maxY / 16.0,
            maxZ / 16.0,
        )
    }

    fun centeredMinMax(minXZ: Int, minY: Int, maxXZ: Int, maxY: Int): VoxelShape {
        return minMax(minXZ, minY, minXZ, maxXZ, maxY, maxXZ)
    }

    fun offset(shape: VoxelShape, offsetX: Int = 0, offsetY: Int = 0, offsetZ: Int = 0): VoxelShape {
        return shape.offset(offsetX / 16.0, offsetY / 16.0, offsetZ / 16.0)
    }
}
