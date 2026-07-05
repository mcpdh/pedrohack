package me.numenmc.pedrohack.systems.hud;

import me.numenmc.pedrohack.render.RenderUtil;
import me.numenmc.pedrohack.render.Theme;
import me.numenmc.pedrohack.systems.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class InputHudElement extends HudElement {
    private static final int gap = 1;
    private static final int width = 25;

    @Override
    public int getWidth() {
        return width * 3 + gap * 2;
    }

    @Override
    public int getHeight() {
        return width * 2 + gap;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();

        // Top row
        drawKey(graphics, 0, 0, "LM", mc.options.keyAttack.isDown());
        drawKey(graphics, width + gap, 0, "W", mc.options.keyUp.isDown());
        drawKey(graphics, (width + gap) * 2, 0, "RM", mc.options.keyUse.isDown());

        // Bottom row
        drawKey(graphics, 0, width + gap, "A", mc.options.keyLeft.isDown());
        drawKey(graphics, width + gap, width + gap, "S", mc.options.keyDown.isDown());
        drawKey(graphics, (width + gap) * 2, width + gap, "D", mc.options.keyRight.isDown());
    }

    private void drawKey(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            String text,
            boolean pressed
    ) {
        graphics.fill(
                x,
                y,
                x + width,
                y + width,
                pressed ? Theme.PRIMARY : 0x80000000
        );

        RenderUtil.centeredCleanText(
                graphics,
                x + width / 2,
                y + width / 2 - 2,
                text,
                Theme.FOREGROUND
        );
    }
}
