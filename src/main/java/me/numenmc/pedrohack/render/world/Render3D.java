package me.numenmc.pedrohack.render.world;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import me.numenmc.pedrohack.Pedrohack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Render3D {
    public static final RenderPipeline LINES = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Pedrohack.id, "pipeline/lines"))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.DEBUG_LINES)
            .withVertexShader(Identifier.fromNamespaceAndPath(Pedrohack.id, "shaders/pos_color.vert"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(Pedrohack.id, "shaders/pos_color.frag"))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withColorTargetState(0, new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .build());

    public static final RenderPipeline LINES_DEPTH = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Pedrohack.id, "pipeline/lines_depth"))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.DEBUG_LINES)
            .withVertexShader(Identifier.fromNamespaceAndPath(Pedrohack.id, "shaders/pos_color.vert"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(Pedrohack.id, "shaders/pos_color.frag"))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .withColorTargetState(0, new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .build());

    public static final RenderPipeline TRIANGLES = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(Pedrohack.id, "pipeline/triangles"))
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                    .withColorTargetState(0, new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(false)
                    .build());

    public static final RenderPipeline TRIANGLES_DEPTH = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(Pedrohack.id, "pipeline/triangles_depth"))
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                    .withColorTargetState(0, new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(false)
                    .build());

    public static void drawLine3D(MeshBuilder lines,
                                  double x1, double y1, double z1,
                                  double x2, double y2, double z2,
                                  Color color) {
        lines.ensureLineCapacity();
        lines.line(
                lines.vec3(x1, y1, z1).color(color).next(),
                lines.vec3(x2, y2, z2).color(color).next()
        );
    }

    public static void drawBoxLines(MeshBuilder lines,
                                    double x1, double y1, double z1,
                                    double x2, double y2, double z2,
                                    Color color) {
        lines.ensureCapacity(8, 24);

        int blb = lines.vec3(x1, y1, z1).color(color).next();
        int blf = lines.vec3(x1, y1, z2).color(color).next();
        int brb = lines.vec3(x2, y1, z1).color(color).next();
        int brf = lines.vec3(x2, y1, z2).color(color).next();
        int tlb = lines.vec3(x1, y2, z1).color(color).next();
        int tlf = lines.vec3(x1, y2, z2).color(color).next();
        int trb = lines.vec3(x2, y2, z1).color(color).next();
        int trf = lines.vec3(x2, y2, z2).color(color).next();

        // Bottom
        lines.line(blb, blf); lines.line(blf, brf);
        lines.line(brf, brb); lines.line(brb, blb);
        // Top
        lines.line(tlb, tlf); lines.line(tlf, trf);
        lines.line(trf, trb); lines.line(trb, tlb);
        // Verticals
        lines.line(blb, tlb); lines.line(blf, tlf);
        lines.line(brf, trf); lines.line(brb, trb);
    }

    public static void drawBoxFilled(MeshBuilder tris,
                                     double x1, double y1, double z1,
                                     double x2, double y2, double z2,
                                     Color color) {
        tris.ensureCapacity(8, 36);

        int blb = tris.vec3(x1, y1, z1).color(color).next();
        int blf = tris.vec3(x1, y1, z2).color(color).next();
        int brb = tris.vec3(x2, y1, z1).color(color).next();
        int brf = tris.vec3(x2, y1, z2).color(color).next();
        int tlb = tris.vec3(x1, y2, z1).color(color).next();
        int tlf = tris.vec3(x1, y2, z2).color(color).next();
        int trb = tris.vec3(x2, y2, z1).color(color).next();
        int trf = tris.vec3(x2, y2, z2).color(color).next();

        // Bottom
        tris.quad(blb, brb, brf, blf);
        // Top
        tris.quad(tlb, tlf, trf, trb);
        // Sides
        tris.quad(blb, blf, tlf, tlb); // west
        tris.quad(brb, trb, trf, brf); // east
        tris.quad(blb, tlb, trb, brb); // north
        tris.quad(blf, brf, trf, tlf); // south
    }

    public static void precompile() {
        GpuDevice device = RenderSystem.getDevice();
        ResourceManager resources = Minecraft.getInstance().getResourceManager();

        for (RenderPipeline pipeline : List.of(LINES, LINES_DEPTH, TRIANGLES, TRIANGLES_DEPTH)) {
            device.precompilePipeline(pipeline, (identifier, k) -> {
                var resource = resources.getResource(identifier).get();
                try (var in = resource.open()) {
                    return IOUtils.toString(in, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
