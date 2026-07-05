package me.numenmc.pedrohack.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.numenmc.pedrohack.systems.Categories;
import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @ModifyReturnValue(method = "getSkyLightLevel", at = @At("RETURN"))
    private int onGetSkyLight(int original) {
        if (!Categories.RENDER.FULLBRIGHT.isEnabled()) return original;
        return Math.max(original, Categories.RENDER.FULLBRIGHT.luminosity.get());
    }

    @ModifyReturnValue(method = "getBlockLightLevel", at = @At("RETURN"))
    private int onGetBlockLight(int original) {
        if (!Categories.RENDER.FULLBRIGHT.isEnabled()) return original;
        return Math.max(original, Categories.RENDER.FULLBRIGHT.luminosity.get());
    }

    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBrightness(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/BlockPos;)I"))
    private int onGetLightLevel(int original) {
        if (!Categories.RENDER.FULLBRIGHT.isEnabled()) return original;
        return Math.max(original, Categories.RENDER.FULLBRIGHT.luminosity.get());
    }
}
