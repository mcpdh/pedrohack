package me.numenmc.pedrohack.mixin;

import me.numenmc.pedrohack.systems.event.EventBus;
import me.numenmc.pedrohack.systems.event.events.ScreenOpenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "setScreenAndShow", at = @At("HEAD"), cancellable = true)
    private void onSetScreenAndShow(Screen screen, CallbackInfo ci)  {
        if (EventBus.post(new ScreenOpenEvent(screen)).isCancelled()) {
            ci.cancel();
        }
    }
}
