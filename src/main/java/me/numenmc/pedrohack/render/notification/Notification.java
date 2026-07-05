package me.numenmc.pedrohack.render.notification;

public class Notification {
    public final NotificationType type;
    public final String message;

    public final long createdAt;

    public float animation;

    public Notification(NotificationType type, String message) {
        this.type = type;
        this.message = message;
        this.createdAt = System.currentTimeMillis();
    }

    public boolean expired() {
        return animation >= 1;
    }
}
