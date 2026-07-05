package me.numenmc.pedrohack.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

public class RenderUtil {
    public static Vec3 center = new Vec3(0, 0, 0);

    private static final Matrix4f storedProjection = new Matrix4f();
    private static final Matrix4f storedModelView = new Matrix4f();
    private static Vec3 cameraPos = new Vec3(0, 0, 0);

    public static void updateScreenCenter(Matrix4fc projection, Matrix4fc view) {
        storedProjection.set(projection);
        storedModelView.set(view);
        cameraPos = Minecraft.getInstance().gameRenderer.mainCamera().position();

        Matrix4f invProjection = new Matrix4f(projection).invert();
        Matrix4f invView = new Matrix4f(view).invert();

        Vector4f center4 = new Vector4f(0, 0, 0, 1).mul(invProjection).mul(invView);
        center4.div(center4.w);

        center = new Vec3(cameraPos.x + center4.x, cameraPos.y + center4.y, cameraPos.z + center4.z);
    }

    public static Vec3 to2D(float x, float y, float z) {
        Minecraft mc = Minecraft.getInstance();

        // Translate relative to camera
        Vector4f vec = new Vector4f(
                (float)(x - cameraPos.x),
                (float)(y - cameraPos.y),
                (float)(z - cameraPos.z),
                1.0f
        );

        // Apply modelView then projection
        Vector4f clip = new Vector4f();
        vec.mul(storedModelView, clip);
        Vector4f ndc = new Vector4f();
        clip.mul(storedProjection, ndc);

        // Behind the camera
        if (ndc.w <= 0f) return null;

        // Perspective divide -> NDC [-1, 1]
        float ndcX = ndc.x / ndc.w;
        float ndcY = ndc.y / ndc.w;

        // Convert to window pixels
        float screenX = (ndcX * 0.5f + 0.5f) * mc.getWindow().getWidth();
        float screenY = (1.0f - (ndcY * 0.5f + 0.5f)) * mc.getWindow().getHeight();

        // Convert from framebuffer pixels to GUI-scaled coords
        double guiScale = mc.getWindow().getGuiScale();
        return new Vec3(screenX / guiScale, screenY / guiScale, ndc.w);
    }

    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    public static void centeredCleanText(GuiGraphicsExtractor graphics, int x, int y, String text, int color) {
        Component component = Theme.Font(text);

        int width = Minecraft.getInstance().font.width(component);
        graphics.text(
                Minecraft.getInstance().font,
                component,
                x - width / 2,
                y,
                color,
                false
        );
    }

    public static void leftText(GuiGraphicsExtractor graphics, int x, int y, String text, int color, boolean shadow) {
        Component component = Theme.Font(text);

        int width = Minecraft.getInstance().font.width(component);
        graphics.text(
                Minecraft.getInstance().font,
                component,
                x - width,
                y,
                color,
                shadow
        );
    }

    public static int color(int r, int g, int b, int a) {
        r = Math.clamp(r, 0, 255);
        g = Math.clamp(g, 0, 255);
        b = Math.clamp(b, 0, 255);
        a = Math.clamp(a, 0, 255);

        return (a << 24)
                | (r << 16)
                | (g << 8)
                | b;
    }

    public static boolean contains(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x &&
                mouseX < x + width &&
                mouseY >= y &&
                mouseY < y + height;
    }

    public static void fillRounded(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int radius, int color) {
        var pose = graphics.pose();
        float s = (float) Minecraft.getInstance().getWindow().getGuiScale();
        pose.pushMatrix();
        pose.scale(1f / s, 1f / s);

        int sx1 = Math.round(x1 * s);
        int sy1 = Math.round(y1 * s);
        int sx2 = Math.round(x2 * s);
        int sy2 = Math.round(y2 * s);
        int sr  = Math.round(radius * s);

        graphics.fill(sx1 + sr, sy1,      sx2 - sr, sy2,      color); // center
        graphics.fill(sx1,      sy1 + sr, sx1 + sr, sy2 - sr, color); // left
        graphics.fill(sx2 - sr, sy1 + sr, sx2,      sy2 - sr, color); // right

        for (int row = 0; row < sr; row++) {
            for (int col = 0; col < sr; col++) {
                int dx = sr - col;
                int dy = sr - row;
                if (dx * dx + dy * dy <= sr * sr) {
                    graphics.fill(sx1 + col,     sy1 + row,     sx1 + col + 1, sy1 + row + 1, color); // TL
                    graphics.fill(sx2 - col - 1, sy1 + row,     sx2 - col,     sy1 + row + 1, color); // TR
                    graphics.fill(sx1 + col,     sy2 - row - 1, sx1 + col + 1, sy2 - row,     color); // BL
                    graphics.fill(sx2 - col - 1, sy2 - row - 1, sx2 - col,     sy2 - row,     color); // BR
                }
            }
        }

        pose.popMatrix();
    }

    public static void fillRoundedOutline(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int radius, int color) {
        var pose = graphics.pose();
        float s = (float) Minecraft.getInstance().getWindow().getGuiScale();
        pose.pushMatrix();
        pose.scale(1f / s, 1f / s);

        int sx1 = Math.round(x1 * s);
        int sy1 = Math.round(y1 * s);
        int sx2 = Math.round(x2 * s);
        int sy2 = Math.round(y2 * s);
        int sr  = Math.round(radius * s);

        int baseA = (color >> 24) & 0xFF;
        int rgb   = color & 0x00FFFFFF;

        // straight edges — no AA needed
        graphics.fill(sx1 + sr, sy1,      sx2 - sr, sy1 + 1,  color);
        graphics.fill(sx1 + sr, sy2 - 1,  sx2 - sr, sy2,      color);
        graphics.fill(sx1,      sy1 + sr, sx1 + 1,  sy2 - sr, color);
        graphics.fill(sx2 - 1,  sy1 + sr, sx2,      sy2 - sr, color);

        for (int row = 0; row < sr; row++) {
            for (int col = 0; col < sr; col++) {
                // pixel center in corner space (corner origin = sr, sr)
                double ddx  = sr - col - 0.5;
                double ddy  = sr - row - 0.5;
                double dist = Math.sqrt(ddx * ddx + ddy * ddy);

                // coverage: 1 at the circle edge, falls off 1px either side
                double coverage = 1.0 - Math.abs(dist - sr);
                if (coverage <= 0.0) continue;
                coverage = Math.min(coverage, 1.0);

                int aa      = (int) Math.round(baseA * coverage);
                int aaColor = (aa << 24) | rgb;

                graphics.fill(sx1 + col,     sy1 + row,     sx1 + col + 1, sy1 + row + 1, aaColor); // TL
                graphics.fill(sx2 - col - 1, sy1 + row,     sx2 - col,     sy1 + row + 1, aaColor); // TR
                graphics.fill(sx1 + col,     sy2 - row - 1, sx1 + col + 1, sy2 - row,     aaColor); // BL
                graphics.fill(sx2 - col - 1, sy2 - row - 1, sx2 - col,     sy2 - row,     aaColor); // BR
            }
        }

        pose.popMatrix();
    }

    public static void fillRoundedBoth(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int radius, int color, int outlineColor) {
        fillRounded(graphics, x1, y1, x2, y2, radius, color);
        fillRoundedOutline(graphics, x1, y1, x2, y2, radius, outlineColor);
    }
}
