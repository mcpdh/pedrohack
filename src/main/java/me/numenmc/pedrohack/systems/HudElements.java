package me.numenmc.pedrohack.systems;

import me.numenmc.pedrohack.systems.hud.*;

import java.util.ArrayList;
import java.util.List;

public class HudElements {
    public record HudEntry(
            Class<? extends HudElement> clazz,
            String name
    ) {}

    private static final List<HudEntry> values = new ArrayList<>();

    private static <T extends HudElement> HudEntry add(Class<T> clazz, String name) {
        HudEntry entry = new HudEntry(clazz, name);
        values.add(entry);
        return entry;
    }

    public static List<HudEntry> values() {
        return values;
    }

    public static final HudEntry BRANDING = add(BrandingHudElement.class, "Branding");
    public static final HudEntry GOLIATH_MAP = add(GoliathMapHudElement.class, "Goliaths Map");
    public static final HudEntry COORDINATES = add(CoordinateHudElement.class, "Coordinates");
    public static final HudEntry INPUT_HUD = add(InputHudElement.class, "Input HUD");
    public static final HudEntry ENABLED_MODULES = add(EnabledModulesHudElement.class, "Enabled Modules");

    public static HudElement create(Class<? extends HudElement> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate HUD element: " + clazz.getName(), e);
        }
    }

    public static String getName(Class<? extends HudElement> clazz) {
        for (HudEntry entry : values) {
            if (entry.clazz() == clazz) {
                return entry.name();
            }
        }
        return clazz.getSimpleName();
    }

    public static Class<? extends HudElement> fromName(String name) {
        for (HudEntry entry : values) {
            if (entry.name().equals(name)) {
                return entry.clazz();
            }
        }
        return null;
    }
}
