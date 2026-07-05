package me.numenmc.pedrohack.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.numenmc.pedrohack.systems.Categories;
import me.numenmc.pedrohack.systems.modules.player.Freecam;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public class CameraMixin {
    @Shadow
    private boolean detached;

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void forceDetached(float partialTicks, CallbackInfo ci) {
        if (Categories.PLAYER.FREECAM.isEnabled()) {
            this.detached = true;
        }
    }

    @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"))
    private void overridePosition(Args args, @Local(argsOnly = true, name = "partialTicks") float partialTicks) {
        Freecam freecam = Categories.PLAYER.FREECAM;
        if (!freecam.isEnabled()) return;

        args.set(0, freecam.getX(partialTicks));
        args.set(1, freecam.getY(partialTicks));
        args.set(2, freecam.getZ(partialTicks));
    }

    @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void overrideRotation(Args args, @Local(argsOnly = true, name = "partialTicks") float partialTicks) {
        Freecam freecam = Categories.PLAYER.FREECAM;
        if (!freecam.isEnabled()) return;

        args.set(0, (float) freecam.getYaw(partialTicks));
        args.set(1, (float) freecam.getPitch(partialTicks));
    }
}
