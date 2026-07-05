package me.numenmc.pedrohack.systems.event.events;

import me.numenmc.pedrohack.systems.event.Event;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public class ScreenRenderEvent extends Event {
    public final Screen screen;
    public final GuiGraphicsExtractor graphicsExtractor;
    public final int mouseX;
    public final int mouseY;
    public final float delta;

    public ScreenRenderEvent(Screen screen, GuiGraphicsExtractor graphicsExtractor, int mouseX, int mouseY, float delta) {
        this.screen = screen;
        this.graphicsExtractor = graphicsExtractor;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.delta = delta;
    }
}
