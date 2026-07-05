package me.numenmc.pedrohack.systems;

import com.google.gson.JsonElement;
import imgui.ImGuiIO;
import me.numenmc.pedrohack.util.NamingUtils;

import java.util.function.Consumer;

public abstract class Setting<T> {
    public SettingType getType() {
        return type;
    }

    public enum SettingType {
        BOOL,
        ENTITY_TYPES,
        ENUM,
        INT,
        BLOCK_TYPES,
        STRING,
        STRINGS,
        COLOR,
        HOTKEY
    }

    private final SettingType type;

    private String displayName = null;

    private final String name;
    private final String description;
    private T value;
    private final T defaultValue;
    private final Consumer<T> onChange;

    public Setting(String name, String description, T defaultValue, SettingType type) {
        this(type, name, description, defaultValue, null);
    }

    public Setting(SettingType type, String name, String description, T defaultValue, Consumer<T> onChange) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.onChange = onChange;
    }

    public String getName() {
        return name;
    }

    public T get() {
        return value;
    }

    public void set(T value, boolean noOnChange) {
        this.value = value;
        if (onChange != null && !noOnChange) {
            onChange.accept(value);
        }
    }

    public void set(T value) {
        set(value, false);
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void reset() {
        set(defaultValue);
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayName() {
        if (displayName != null) return displayName;

        displayName = NamingUtils.getDisplayName(getName());
        return displayName;
    }

    public abstract JsonElement serialize();
    public abstract void deserialize(JsonElement element);

    public abstract void render(ImGuiIO io, Module parent);
}
