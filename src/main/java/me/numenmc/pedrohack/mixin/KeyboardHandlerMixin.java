package me.numenmc.pedrohack.mixin;

import me.numenmc.pedrohack.systems.Categories;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(
            method = "keyPress",
            at = @At("HEAD")
    )
    private void onKey(
            long handle,
            int action,
            KeyEvent event,
            CallbackInfo ci
    ) {
        if (Minecraft.getInstance().gui.screen() == null) {
            if (action == GLFW.GLFW_RELEASE) {
                // Release
                Categories.onKeybindRelease(event.key());
            }

            if (action == GLFW.GLFW_PRESS) {
                // Press
                Categories.onKeybindPress(event.key());
            }
        }
    }
}
