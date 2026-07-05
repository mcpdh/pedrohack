package me.numenmc.pedrohack.render.notification;

import me.numenmc.pedrohack.Pedrohack;
import net.minecraft.resources.Identifier;

public enum NotificationType {
    GENERIC(Identifier.fromNamespaceAndPath(Pedrohack.id, "textures/gui/toast/generic.png")),
    ERROR(Identifier.fromNamespaceAndPath(Pedrohack.id, "textures/gui/toast/error.png")),
    HINT(Identifier.fromNamespaceAndPath(Pedrohack.id, "textures/gui/toast/hint.png"));

    private final Identifier icon;

    NotificationType(Identifier icon) {
        this.icon = icon;
    }

    public Identifier getIcon() {
        return icon;
    }
}
