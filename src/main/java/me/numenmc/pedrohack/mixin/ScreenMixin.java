package me.numenmc.pedrohack.mixin;

import me.numenmc.pedrohack.systems.event.EventBus;
import me.numenmc.pedrohack.systems.event.events.ScreenRenderEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        EventBus.post(new ScreenRenderEvent((Screen)(Object) this, graphics, mouseX, mouseY, a));
    }
}
