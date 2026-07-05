package me.numenmc.pedrohack.systems.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import imgui.ImGuiIO;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.Setting;

import java.util.function.Consumer;

public class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final Class<E> enumClass;

    private EnumSetting(
            String name,
            String description,
            E defaultValue,
            Class<E> enumClass,
            Consumer<E> onChange
    ) {
        super(SettingType.ENUM, name, description, defaultValue, onChange);

        this.enumClass = enumClass;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(get().name());
    }

    @Override
    public void deserialize(JsonElement element) {
        try {
            set(Enum.valueOf(enumClass, element.getAsString()), true);
        } catch (IllegalArgumentException ignored) {} // unknown value, keep default
    }

    @Override
    public void render(ImGuiIO io, Module parent) {

    }

    public Class<E> getEnumClass() {
        return enumClass;
    }

    public E[] getValues() {
        return enumClass.getEnumConstants();
    }

    public static class Builder<E extends Enum<E>> {
        private final Class<E> enumClass;

        private String name;
        private String description = "";
        private E defaultValue;
        private Consumer<E> onChange;

        public Builder(Class<E> enumClass) {
            this.enumClass = enumClass;
        }

        public Builder<E> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<E> description(String description) {
            this.description = description;
            return this;
        }

        public Builder<E> defaultValue(E defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder<E> onChange(Consumer<E> onChange) {
            this.onChange = onChange;
            return this;
        }

        public EnumSetting<E> build() {
            return new EnumSetting<>(
                    name,
                    description,
                    defaultValue,
                    enumClass,
                    onChange
            );
        }
    }
}
