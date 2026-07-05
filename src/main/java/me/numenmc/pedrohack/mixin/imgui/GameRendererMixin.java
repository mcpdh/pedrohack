package me.numenmc.pedrohack.mixin.imgui;

import me.numenmc.pedrohack.imgui.ImGuiImpl;
import me.numenmc.pedrohack.imgui.RenderInterface;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "render", at = @At("RETURN"))
    private void render(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        if (minecraft.gui.screen() instanceof final RenderInterface renderInterface) {
            ImGuiImpl.beginImGuiRendering();
            renderInterface.render(ImGui.getIO());
            ImGuiImpl.endImGuiRendering();
        }
    }
}
