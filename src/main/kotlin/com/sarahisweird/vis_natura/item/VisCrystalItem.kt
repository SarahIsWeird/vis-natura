package com.sarahisweird.vis_natura.item

import com.sarahisweird.vis_natura.util.PlantTransmuter
import com.sarahisweird.vis_natura.vis.VisType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult

open class VisCrystalItem(
    val visType: VisType,
    settings: Settings,
) : Item(settings) {
    companion object {
        fun getFactory(visType: VisType): (Settings) -> VisCrystalItem {
            return { settings -> VisCrystalItem(visType, settings) }
        }
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if (!PlantTransmuter.transmuteBlock(context.world, context.blockPos, this.visType, context.stack)) {
            return ActionResult.PASS
        }

        val player = context.player ?: return ActionResult.SUCCESS

        val selectedStack = player.inventory.selectedStack
        selectedStack.decrement(1)
        player.inventory.selectedStack = if (selectedStack.isEmpty) ItemStack.EMPTY else selectedStack

        return ActionResult.SUCCESS
    }
}
