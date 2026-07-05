package me.numenmc.pedrohack.mixin;

import me.numenmc.pedrohack.systems.Categories;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LightCoordsUtil.BrightnessGetter.class)
public interface BrightnessGetterMixin {
    @ModifyVariable(method = "lambda$static$0", at = @At(value = "STORE"), name = "sky")
    private static int getLightmapCoordinatesModifySkyLight(int sky) {
        if (!Categories.RENDER.FULLBRIGHT.isEnabled()) return sky;
        return Math.max(sky, Categories.RENDER.FULLBRIGHT.luminosity.get());
    }

    @ModifyVariable(method = "lambda$static$0", at = @At(value = "STORE"), name = "block")
    private static int getLightmapCoordinatesModifyBlockLight(int block) {
        if (!Categories.RENDER.FULLBRIGHT.isEnabled()) return block;
        return Math.max(block, Categories.RENDER.FULLBRIGHT.luminosity.get());
    }
}
