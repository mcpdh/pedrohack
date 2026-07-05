package me.numenmc.pedrohack.systems.event.events;

import me.numenmc.pedrohack.systems.event.Event;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Render2DEvent extends Event {
    public final GuiGraphicsExtractor graphics;
    public final DeltaTracker delta;

    public Render2DEvent(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        this.graphics = graphics;
        this.delta = delta;
    }
}
