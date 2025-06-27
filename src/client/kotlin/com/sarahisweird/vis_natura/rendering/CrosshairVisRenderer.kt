package com.sarahisweird.vis_natura.rendering

import com.sarahisweird.vis_natura.VNItems
import com.sarahisweird.vis_natura.VisNatura
import com.sarahisweird.vis_natura.item.ItemComponents
import com.sarahisweird.vis_natura.networking.payloads.ChangeSelectedSpellC2SPayload
import com.sarahisweird.vis_natura.vis.VisType
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.LayeredDrawer
import net.minecraft.client.input.Scroller
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import org.apache.logging.log4j.LogManager
import kotlin.math.cos
import kotlin.math.sin

private enum class SelectorAnimationState {
    NO_SELECTION,
    EXPANDING,
    EXPANDED,
    RETRACTING,
}

object CrosshairVisRenderer : LayeredDrawer.Layer {
    private val LOGGER = LogManager.getLogger(CrosshairVisRenderer::class.java)

    private val WAND_GUI_LAYER = VisNatura.id("crosshair_vis_layer")

    private val CHARGE_TEXTURE_EMPTY = VisNatura.id("textures/gui/crosshair_meter_empty.png")
    private val CHARGE_TEXTURE_FULL = VisNatura.id("textures/gui/crosshair_meter_full.png")
    private const val CHARGE_TEXTURE_WIDTH = 7
    private const val CHARGE_TEXTURE_HEIGHT = 17

    private var visChargeOffset = 20f

    private val SPELL_ICON_BACKGROUND_TEXTURE = VisNatura.id("textures/gui/spell_icons/background.png")
    private const val SPELL_ICON_TEXTURE_SIZE = 16
    private const val SPELL_ICON_BACKGROUND_SIZE = 24
    private var spellIconOffset = 50f

    private var isSelecting = false

    private val SPELL_SELECTOR_TEXTURE = VisNatura.id("textures/gui/spell_selector.png")

    private const val SELECTOR_ANIMATION_DURATION = 20
    private var selectorAnimationState = SelectorAnimationState.NO_SELECTION
    private var selectorAnimTicks = 0
    private var selectedSpell = 0
    private var selectedSpellId = VisNatura.spellRegistry.getSpellId(selectedSpell)!!

    fun onShiftScroll(scrollOffset: Int): Boolean {
        val player = MinecraftClient.getInstance().player ?: return false
        if (!player.mainHandStack.isOf(VNItems.WAND)) {
            return false
        }

        this.selectedSpell = Scroller.scrollCycling(
            scrollOffset.toDouble(),
            selectedSpell,
            VisNatura.spellRegistry.size,
        )

        this.selectedSpellId = VisNatura.spellRegistry.getSpellId(selectedSpell)!!

        val payload = ChangeSelectedSpellC2SPayload(
            player.inventory.selectedSlot,
            selectedSpellId)
        ClientPlayNetworking.send(payload)

        return true
    }

    fun register() {
        HudLayerRegistrationCallback.EVENT.register { layeredDrawer ->
            layeredDrawer.attachLayerBefore(IdentifiedLayer.CHAT, WAND_GUI_LAYER, this::render)
        }
    }

    override fun render(context: DrawContext, tickCounter: RenderTickCounter) {
        this.visChargeOffset = 50f
        this.spellIconOffset = 30f

        val player = MinecraftClient.getInstance().player ?: return

        val wand = player.mainHandStack
        if (!wand.isOf(VNItems.WAND)) return

        this.selectedSpellId = wand.getOrDefault(ItemComponents.SELECTED_SPELL,
            VisNatura.spellRegistry.getSpellIds().first())
        this.selectedSpell = VisNatura.spellRegistry.getSpellIds().indexOf(this.selectedSpellId)

        this.updateSelection(player)

        val animProgress = this.selectorAnimTicks.toFloat() / SELECTOR_ANIMATION_DURATION
        this.visChargeOffset = MathHelper.lerp(animProgress, 15f, 45f)
        this.spellIconOffset = MathHelper.lerp(animProgress, 0f, 30f)

        context.matrices.push()
        // I have no clue where the 0.5 is coming from :(
        context.matrices.translate((context.scaledWindowWidth / 2f - 0.5f), (context.scaledWindowHeight / 2f - 0.5f), 0f)

        this.renderVisCharges(context, wand)
        this.renderSpells(context)

        context.matrices.pop()
    }

    private fun updateSelection(player: ClientPlayerEntity) {
        this.isSelecting = player.isSneaking

        if (this.isSelecting) {
            if (this.selectorAnimationState != SelectorAnimationState.EXPANDING && this.selectorAnimationState != SelectorAnimationState.EXPANDED) {
                this.selectorAnimationState = SelectorAnimationState.EXPANDING
            } else if (this.selectorAnimTicks >= SELECTOR_ANIMATION_DURATION) {
                this.selectorAnimationState = SelectorAnimationState.EXPANDED
            } else {
                this.selectorAnimTicks++
            }
        } else {
            if (this.selectorAnimationState != SelectorAnimationState.RETRACTING && this.selectorAnimationState != SelectorAnimationState.NO_SELECTION) {
                this.selectorAnimationState = SelectorAnimationState.RETRACTING
            } else if (this.selectorAnimTicks == 0) {
                this.selectorAnimationState = SelectorAnimationState.NO_SELECTION
            } else {
                this.selectorAnimTicks--
            }
        }
    }

    private fun renderSpells(context: DrawContext) {
        if (this.selectorAnimationState == SelectorAnimationState.NO_SELECTION) {
            val backgroundOffset = (- SPELL_ICON_BACKGROUND_SIZE) / 2
            context.drawTexture(
                RenderLayer::getGuiTextured,
                SPELL_ICON_BACKGROUND_TEXTURE,
                backgroundOffset, backgroundOffset,
                0f, 0f,
                SPELL_ICON_BACKGROUND_SIZE, SPELL_ICON_BACKGROUND_SIZE,
                SPELL_ICON_BACKGROUND_SIZE, SPELL_ICON_BACKGROUND_SIZE,
            )

            context.drawTexture(
                RenderLayer::getGuiTextured,
                getSpellSelectorTextureId(selectedSpellId),
                -SPELL_ICON_TEXTURE_SIZE / 2, -SPELL_ICON_TEXTURE_SIZE / 2,
                0f, 0f,
                SPELL_ICON_TEXTURE_SIZE, SPELL_ICON_TEXTURE_SIZE,
                SPELL_ICON_TEXTURE_SIZE, SPELL_ICON_TEXTURE_SIZE,
            )

            return
        }

        VisNatura.spellRegistry.getSpellIds().forEachIndexed { index, spellId ->
            this.renderSpell(context, index, spellId, index == selectedSpell)
        }
    }

    private fun renderSpell(context: DrawContext, index: Int, spellId: Identifier, isSelected: Boolean) {
        context.matrices.push()

        val angle = index.toFloat() * 2f * Math.PI.toFloat() / VisNatura.spellRegistry.size.toFloat()
        val x = cos(angle) * this.spellIconOffset - SPELL_ICON_TEXTURE_SIZE / 2
        val y = sin(angle) * this.spellIconOffset - SPELL_ICON_TEXTURE_SIZE / 2

        context.matrices.translate(x, y, 0f)

        val backgroundOffset = (SPELL_ICON_TEXTURE_SIZE - SPELL_ICON_BACKGROUND_SIZE) / 2
        context.drawTexture(
            RenderLayer::getGuiTextured,
            SPELL_ICON_BACKGROUND_TEXTURE,
            backgroundOffset, backgroundOffset,
            0f, 0f,
            SPELL_ICON_BACKGROUND_SIZE, SPELL_ICON_BACKGROUND_SIZE,
            SPELL_ICON_BACKGROUND_SIZE, SPELL_ICON_BACKGROUND_SIZE,
        )

        context.drawTexture(
            RenderLayer::getGuiTextured,
            getSpellSelectorTextureId(spellId),
            0, 0,
            0f, 0f,
            SPELL_ICON_TEXTURE_SIZE, SPELL_ICON_TEXTURE_SIZE,
            SPELL_ICON_TEXTURE_SIZE, SPELL_ICON_TEXTURE_SIZE,
        )

        if (isSelected && this.selectorAnimTicks > (SELECTOR_ANIMATION_DURATION / 2)) {
            context.drawTexture(
                RenderLayer::getGuiTextured,
                SPELL_SELECTOR_TEXTURE,
                backgroundOffset, backgroundOffset,
                0f, 0f,
                SPELL_ICON_BACKGROUND_SIZE, SPELL_ICON_BACKGROUND_SIZE,
                SPELL_ICON_BACKGROUND_SIZE, SPELL_ICON_BACKGROUND_SIZE,
            )
        }

        context.matrices.pop()
    }

    private fun getSpellSelectorTextureId(spellId: Identifier): Identifier {
        return VisNatura.id("textures/gui/spell_icons/${spellId.path}.png")
    }

    private fun renderVisCharges(context: DrawContext, wand: ItemStack) {
        VisType.entries.forEachIndexed { index, visType ->
            val charge = wand[visType.chargeComponentType] ?: 0

            this.renderCharge(context, index, visType, charge)
        }
    }

    private fun renderCharge(context: DrawContext, i: Int, type: VisType, charge: Int) {
        context.matrices.push()

        val angle = i.toFloat() * (2f * Math.PI.toFloat() / 7f)
        context.matrices.multiply(RotationAxis.POSITIVE_Z.rotation(angle))
        context.matrices.translate(-CHARGE_TEXTURE_WIDTH / 2f, this.visChargeOffset, 0f)

        context.drawTexture(
            RenderLayer::getGuiTextured,
            CHARGE_TEXTURE_EMPTY,
            0, 0,
            0f, 0f,
            CHARGE_TEXTURE_WIDTH, CHARGE_TEXTURE_HEIGHT,
            CHARGE_TEXTURE_WIDTH, CHARGE_TEXTURE_HEIGHT,
            CHARGE_TEXTURE_WIDTH, CHARGE_TEXTURE_HEIGHT,
            type.color,
        )

        val chargeFactor = charge.toFloat() / 1024f
        val chargedHeight = (chargeFactor * CHARGE_TEXTURE_HEIGHT).toInt() + 1

        context.drawTexture(
            RenderLayer::getGuiTextured,
            CHARGE_TEXTURE_FULL,
            0, 0,
            0f, 0f,
            CHARGE_TEXTURE_WIDTH, chargedHeight,
            CHARGE_TEXTURE_WIDTH, chargedHeight,
            CHARGE_TEXTURE_WIDTH, CHARGE_TEXTURE_HEIGHT,
            type.color,
        )

        context.matrices.pop()
    }
}
