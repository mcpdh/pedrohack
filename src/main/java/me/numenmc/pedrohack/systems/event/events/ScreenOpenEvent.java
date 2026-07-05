package me.numenmc.pedrohack.systems.event.events;

import me.numenmc.pedrohack.systems.event.CancellableEvent;
import net.minecraft.client.gui.screens.Screen;

public class ScreenOpenEvent extends CancellableEvent {
    public final Screen screen;

    public ScreenOpenEvent(Screen screen) {
        this.screen = screen;
    }
}
