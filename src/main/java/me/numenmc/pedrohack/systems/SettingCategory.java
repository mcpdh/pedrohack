package me.numenmc.pedrohack.systems;

import java.util.ArrayList;
import java.util.List;

public class SettingCategory {
    private final String name;
    protected final List<Setting<?>> settings = new ArrayList<>();

    public SettingCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public void add(Setting<?> setting) {
        settings.add(setting);
    }

    public void remove(Setting<?> setting) {
        settings.remove(setting);
    }

    public static SettingCategory createDefault() {
        return new SettingCategory("General");
    }

    public static class Contained extends SettingCategory {
        public Contained(String name) {
            super(name);
        }

        public <T extends Setting<?>> T addGet(T setting) {
            this.settings.add(setting);
            return setting;
        }
    }
}
