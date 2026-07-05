package me.numenmc.pedrohack.render.world;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Optional;
import java.util.OptionalDouble;

public class MeshRenderer {
    public static void render(MeshBuilder mesh, RenderPipeline pipeline) {
        if (mesh.isBuilding()) mesh.end();
        if (mesh.getIndicesCount() == 0) return;

        GpuBuffer vbo = mesh.getVertexBuffer();
        GpuBuffer ibo = mesh.getIndexBuffer();

        RenderSystem.getModelViewStack().pushMatrix();
        Vec3 cam = Minecraft.getInstance().gameRenderer.mainCamera().position();
        RenderSystem.getModelViewStack().translate(0, (float) -cam.y, 0);

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(
                        RenderSystem.getModelViewMatrixCopy(),
                        new Vector4f(1f, 1f, 1f, 1f),
                        new Vector3f(),
                        new Matrix4f()
                );

        RenderSystem.getModelViewStack().popMatrix();

        // 26.2: GameRenderer no longer takes the RenderBuffers via Minecraft#getMainRenderTarget;
        // it's now exposed off of Minecraft#gameRenderer instead.
        var mainTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();

        try (RenderPass pass = RenderSystem.getDevice()
                .createCommandEncoder()
                // 26.2: the clear color overload now takes an Optional<Vector4fc> instead of an
                // OptionalInt.
                .createRenderPass(
                        () -> "Numenic render",
                        mainTarget.getColorTextureView(),
                        Optional.empty(),
                        mainTarget.getDepthTextureView(),
                        OptionalDouble.empty()
                )) {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            // 26.2: setVertexBuffer now takes a GpuBufferSlice rather than a raw GpuBuffer.
            pass.setVertexBuffer(0, vbo.slice());
            // 26.2: VertexFormat$IndexType moved out to its own top-level com.mojang.blaze3d.IndexType.
            pass.setIndexBuffer(ibo, IndexType.INT);
            // 26.2: drawIndexed's five ints are now (indexCount, instanceCount, firstIndex,
            // baseVertex, firstInstance) - firstInstance is new, defaulted to 0 here.
            pass.drawIndexed(mesh.getIndicesCount(), 1, 0, 0, 0);
        }
    }
}
