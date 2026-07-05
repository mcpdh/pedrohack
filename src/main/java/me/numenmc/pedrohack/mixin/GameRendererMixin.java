package me.numenmc.pedrohack.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.numenmc.pedrohack.render.RenderUtil;
import me.numenmc.pedrohack.render.world.MeshBuilder;
import me.numenmc.pedrohack.render.world.MeshRenderer;
import me.numenmc.pedrohack.render.world.Render3D;
import me.numenmc.pedrohack.systems.event.EventBus;
import me.numenmc.pedrohack.systems.event.events.Render3DEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private Camera mainCamera;

    @Unique
    private final PoseStack matrices = new PoseStack();

    @Shadow
    protected abstract void bobView(final CameraRenderState cameraState, final PoseStack poseStack);

    @Shadow
    protected abstract void bobHurt(final CameraRenderState cameraState, final PoseStack poseStack);

    @Unique private MeshBuilder lines;
    @Unique private MeshBuilder linesDepth;
    @Unique private MeshBuilder tris;
    @Unique private MeshBuilder trisDepth;

    @Shadow
    @Final
    private GameRenderState gameRenderState;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=hand"))
    private void onRenderLevel(DeltaTracker deltaTracker, CallbackInfo ci, @Local(name = "projectionMatrix") Matrix4f projectionMatrix, @Local(name = "modelViewMatrix") Matrix4fc modelViewMatrix) {
        if (lines == null) {
            lines = new MeshBuilder(Render3D.LINES);
            linesDepth = new MeshBuilder(Render3D.LINES_DEPTH);
            tris = new MeshBuilder(Render3D.TRIANGLES);
            trisDepth = new MeshBuilder(Render3D.TRIANGLES_DEPTH);
        }

        RenderSystem.getModelViewStack()
                .pushMatrix()
                .mul(modelViewMatrix);

        RenderUtil.updateScreenCenter(
                projectionMatrix,
                modelViewMatrix
        );

        lines.begin();
        linesDepth.begin();
        tris.begin();
        trisDepth.begin();

        EventBus.post(
                new Render3DEvent(
                        lines,
                        linesDepth,
                        tris,
                        trisDepth,
                        modelViewMatrix,
                        deltaTracker.getGameTimeDeltaPartialTick(true)
                )
        );

        MeshRenderer.render(lines, Render3D.LINES);
        MeshRenderer.render(linesDepth, Render3D.LINES_DEPTH);
        MeshRenderer.render(tris, Render3D.TRIANGLES);
        MeshRenderer.render(trisDepth, Render3D.TRIANGLES_DEPTH);

        RenderSystem.getModelViewStack().popMatrix();
    }
}
