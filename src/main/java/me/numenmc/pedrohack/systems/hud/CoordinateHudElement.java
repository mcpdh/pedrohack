package me.numenmc.pedrohack.systems.hud;

import me.numenmc.pedrohack.render.Theme;
import me.numenmc.pedrohack.systems.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class CoordinateHudElement extends HudElement {
    Component currentText = Theme.Font("Loading position...");

    @Override
    public int getWidth() {
        return Minecraft.getInstance().font.width(currentText);
    }

    @Override
    public int getHeight() {
        return 12;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            currentText = Theme.Font(String.format(
                    "XYZ: %d  %d  %d", // two spaces it looks better
                    player.getBlockX(),
                    player.getBlockY(),
                    player.getBlockZ()
            ));
        }

        graphics.text(Minecraft.getInstance().font, currentText, 0, 2, Theme.FOREGROUND);
    }
}
