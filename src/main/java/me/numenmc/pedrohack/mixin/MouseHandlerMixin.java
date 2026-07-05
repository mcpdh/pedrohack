package me.numenmc.pedrohack.mixin;

import me.numenmc.pedrohack.systems.Categories;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;

    @Inject(method = "turnPlayer", at = @At("HEAD"))
    private void modifyDelta(CallbackInfo ci) {
        if (Categories.COMBAT.AIM_ASSIST.isEnabled()) {
            Categories.COMBAT.AIM_ASSIST.onUpdateMouse(
                    accumulatedDX,
                    accumulatedDY,
                    (v) -> accumulatedDX = v,
                    (v) -> accumulatedDY = v
            );
        } else if (Categories.PLAYER.AUTO_SURFACE_FLY.isEnabled()) {
            Categories.PLAYER.AUTO_SURFACE_FLY.onUpdateMouse(
                    accumulatedDX,
                    accumulatedDY,
                    (v) -> accumulatedDX = v,
                    (v) -> accumulatedDY = v
            );
        }
    }
}
