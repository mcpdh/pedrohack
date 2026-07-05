package me.numenmc.pedrohack.systems.event;

import me.numenmc.pedrohack.Pedrohack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EventBus {
    private static final List<Object> subscribers = new ArrayList<>();

    public static void subscribe(Object object) {
        subscribers.add(object);
    }

    public static void unsubscribe(Object object) {
        subscribers.remove(object);
    }

    public static <T extends Event> T post(T event) {
        List<Object> snapshot = new ArrayList<>(subscribers);

        for (Object subscriber : snapshot) {
            for (Method method : subscriber.getClass().getDeclaredMethods()) {

                if (!method.isAnnotationPresent(EventHandler.class)) continue;
                if (method.getParameterCount() != 1) continue;

                Class<?> param = method.getParameterTypes()[0];

                if (!param.isAssignableFrom(event.getClass())) continue;

                try {
                    method.setAccessible(true);
                    method.invoke(subscriber, event);
                } catch (Exception e) {
                    Pedrohack.log.error("Event bus error", e);
                }
            }
        }

        return event;
    }
}
