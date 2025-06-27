package com.sarahisweird.vis_natura.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.sarahisweird.vis_natura.rendering.CrosshairVisRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
class MouseMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @WrapOperation(
            method = "onMouseScroll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerInventory;setSelectedSlot(I)V"
            )
    )
    private void redirectScrollToVisRenderer(
            PlayerInventory inventory,
            int slot,
            Operation<Void> op,
            @Local(ordinal = 0) int scrollOffset
    ) {
        // We mix into the branch where player is not null, so it can't produce an NPE!
        // noinspection DataFlowIssue
        if (!client.player.isSneaking()) {
            op.call(inventory, slot);
            return;
        }

        if (!CrosshairVisRenderer.INSTANCE.onShiftScroll(scrollOffset)) {
            op.call(inventory, slot);
        }
    }
}
