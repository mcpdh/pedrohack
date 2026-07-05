package me.numenmc.pedrohack.render.notification;

import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public final class Notifications {
    private static final List<Notification> active = new ArrayList<>();

    public static void pushNotification(NotificationType type, String message) {
        active.addFirst(new Notification(
                type,
                message
        ));

        if (active.size() > 8) {
            active.removeLast();
        }
    }

    private static long lastTickMillis = Util.getMillis();

    public static void tick() {
        float duration = 5f;
    
        long now = Util.getMillis();
        float deltaSeconds = (now - lastTickMillis) / 1000f;
        lastTickMillis = now;
    
        for (Notification n : active) {
            n.animation += deltaSeconds / duration;
            if (n.animation > 1f) {
                n.animation = 1f;
            }
        }
    
        active.removeIf(Notification::expired);
    }

    public static List<Notification> getActive() {
        return active;
    }
}
