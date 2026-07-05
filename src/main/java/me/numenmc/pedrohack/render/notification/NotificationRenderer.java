package me.numenmc.pedrohack.render.notification;

import me.numenmc.pedrohack.render.RenderUtil;
import me.numenmc.pedrohack.render.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class NotificationRenderer {

    // How fast notifications ease into their new slot. Higher = snappier, lower = floatier.
    private static final float SLOT_EASE_SPEED = 14f;

    // Tracks the currently-displayed (eased) Y offset for each notification instance.
    private static Map<Notification, Float> displayYMap = new IdentityHashMap<>();
    private static long lastFrameTimeNanos = System.nanoTime();

    public static void render(GuiGraphicsExtractor graphics) {
        List<Notification> notifications = Notifications.getActive();
        Matrix3x2fStack pose = graphics.pose();
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int height = 18;
        int marginX = 40;
        int marginY = 60;
        int y = screenHeight - marginY - height;

        // Frame-rate independent delta time for smoothing.
        long now = System.nanoTime();
        float deltaTime = (now - lastFrameTimeNanos) / 1_000_000_000f;
        lastFrameTimeNanos = now;
        deltaTime = Math.min(deltaTime, 0.1f); // clamp to avoid jumps after lag spikes
        float smoothFactor = 1f - (float) Math.exp(-SLOT_EASE_SPEED * deltaTime);

        Map<Notification, Float> newDisplayYMap = new IdentityHashMap<>();

        for (Notification notification : notifications) {
            Component message = Theme.Font(notification.message);
            int textWidth = mc.font.width(message);
            int width = textWidth + 8;
            int totalWidth = width + 16;
            float anim = notification.animation;
            float xOffset;
            float yOffset = 0f;
            if (anim < 0.1f) {
                float t = anim / 0.1f;
                float sprung = easeOutElastic(t);
                xOffset = (1f - sprung) * (totalWidth + 20f);
                yOffset = (1f - sprung) * (height + 20f);
            } else if (anim < 0.8f) {
                xOffset = 0f;
                // yOffset stays 0
            } else if (anim < 0.88f) {
                float t = (anim - 0.8f) / 0.08f;
                xOffset = -easeOutSine(t) * 16f;
            } else if (anim < 0.93f) {
                xOffset = -16f;
            } else {
                float t = (anim - 0.93f) / 0.07f;
                xOffset = -16f + easeOutSine(t) * (totalWidth * 2 + 50f);
            }
            float x = screenWidth - totalWidth - marginX + xOffset;

            // Ease the slot position instead of snapping to it.
            Float previousDisplayY = displayYMap.get(notification);
            float displayY;
            if (previousDisplayY == null) {
                // First time we've seen this notification: snap, let the entrance
                // animation (yOffset above) handle how it appears.
                displayY = y;
            } else {
                displayY = previousDisplayY + (y - previousDisplayY) * smoothFactor;
            }
            newDisplayYMap.put(notification, displayY);

            pose.pushMatrix();
            pose.translate(x, displayY + yOffset); // yOffset only active during enter
            graphics.fill(0, 0, totalWidth, height, RenderUtil.withAlpha(0x000000, 100));
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    notification.type.getIcon(),
                    1, 1,
                    0f, 0f,
                    16, 16,
                    32, 32,
                    32, 32
            );
            graphics.text(
                    mc.font,
                    message,
                    20,
                    5,
                    0xFFFFFFFF
            );
            pose.popMatrix();
            y -= height + 4;
        }

        // Replace the map so notifications no longer active are dropped (no leak).
        displayYMap = newDisplayYMap;

        Notifications.tick();
    }

    private static float easeOutElastic(float x) {
        if (x == 0) return 0;
        if (x == 1) return 1;
        final double c4 = (2 * Math.PI) / 3;
        return (float) (Math.pow(2, -14 * x) * Math.sin((x * 10 - 0.75) * c4) + 1);
    }

    private static float easeOutSine(float x) {
        return (float) Math.sin((x * Math.PI) / 2);
    }
}
