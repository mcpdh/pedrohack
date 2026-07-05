package me.numenmc.pedrohack.imgui;

import com.mojang.blaze3d.opengl.FrameBufferCache;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import imgui.*;
import imgui.extension.implot.ImPlot;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

public final class ImGuiImpl {

    private static final ImGuiImplGlfw imGuiImplGlfw = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 imGuiImplGl3 = new ImGuiImplGl3();
    private static short[] glyphRanges;

    /**
     * True when the active GpuDevice backend is OpenGL. imgui-java (SpaiR) only ships a GL3
     * renderer backend, so there is no drop-in way to draw ImGui through the new Vulkan backend.
     * When Vulkan is active we skip the GL calls entirely rather than run them against a
     * non-GL device, and the overlay is effectively disabled until a Vulkan renderer exists.
     */
    private static boolean glBackendActive = false;

    public static void create(final long handle) {
        glBackendActive = RenderSystem.getDevice().backend instanceof GlDevice;

        ImGui.createContext();
        ImPlot.createContext();

        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename("pedrohack.imgui.ini");
        io.setConfigFlags(ImGuiConfigFlags.DockingEnable);

        final ImFont defaultFont = loadFontResource("/assets/pedrohack/fonts/Roboto-Regular.ttf", 16);
        io.setFontDefault(defaultFont);

        // GLFW/input setup is backend-agnostic and safe to run regardless of the active
        // rendering backend.
        imGuiImplGlfw.init(handle, true);

        if (glBackendActive) {
            imGuiImplGl3.init();
        } else {
            System.err.println("[pedrohack] ImGui overlay disabled: the Vulkan rendering backend is "
                    + "active and imgui-java has no Vulkan renderer backend. Switch "
                    + "Video Settings > Graphics to OpenGL to use the overlay.");
        }
    }

    public static void beginImGuiRendering() {
        if (!glBackendActive) {
            return;
        }

        final RenderTarget framebuffer = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        final GlDevice glDevice = (GlDevice) RenderSystem.getDevice().backend;
        final FrameBufferCache frameBufferCache = glDevice.frameBufferCache();
        final GlTexture colorTexture = (GlTexture) framebuffer.getColorTexture();

        // NOTE: GlTexture#getFbo was moved onto FrameBufferCache in 26.2. The primer confirms the
        // move but not the full parameter list; verify this overload against your mapped/decompiled
        // 26.2 jar (the trailing null stands in for an optional depth attachment, as before).
        final int fbo = frameBufferCache.getFbo(glDevice.directStateAccess(), List.of(colorTexture), null);

        GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, fbo);
        GL11C.glViewport(0, 0, framebuffer.width, framebuffer.height);

        imGuiImplGl3.newFrame();
        imGuiImplGlfw.newFrame();
        ImGui.newFrame();
    }

    public static void endImGuiRendering() {
        if (!glBackendActive) {
            return;
        }

        ImGui.render();
        imGuiImplGl3.renderDrawData(ImGui.getDrawData());
        GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long pointer = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            GLFW.glfwMakeContextCurrent(pointer);
        }
    }

    private static void initGlyphRanges() {
        if (glyphRanges == null) {
            final ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder();
            rangesBuilder.addRanges(ImGui.getIO().getFonts().getGlyphRangesDefault());
            glyphRanges = rangesBuilder.buildRanges();
        }
    }

    private static ImFont loadFontResource(final String path, final int pixelSize) {
        initGlyphRanges();
        final ImFontConfig config = new ImFontConfig();
        config.setGlyphRanges(glyphRanges);
        try (final InputStream in = Objects.requireNonNull(ImGuiImpl.class.getResourceAsStream(path))) {
            final byte[] fontData = IOUtils.toByteArray(in);
            final ImFont font = ImGui.getIO().getFonts().addFontFromMemoryTTF(fontData, pixelSize, config);
            ImGui.getIO().getFonts().build();
            return font;
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to load font from path: " + path, e);
        } finally {
            config.destroy();
        }
    }

    public static void dispose() {
        if (glBackendActive) {
            imGuiImplGl3.shutdown();
        }
        imGuiImplGlfw.shutdown();
        ImPlot.destroyContext();
        ImGui.destroyContext();
    }
}
