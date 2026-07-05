package me.numenmc.pedrohack.mixin;

import me.numenmc.pedrohack.systems.Categories;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void onTurn(double xo, double yo, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        if (Categories.PLAYER.FREECAM.isEnabled() && this.equals(mc.player)) {
            Categories.PLAYER.FREECAM.changeLookDirection(xo * 0.15, yo * 0.15);
            ci.cancel();
        }
    }
}
