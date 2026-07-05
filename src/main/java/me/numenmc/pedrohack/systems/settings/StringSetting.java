package me.numenmc.pedrohack.systems.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImString;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.Setting;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class StringSetting extends Setting<String> {
    private final int maxLength;
    private final Predicate<String> validator;

    private StringSetting(
            String name,
            String description,
            String defaultValue,
            int maxLength,
            Predicate<String> validator,
            Consumer<String> onChange
    ) {
        super(SettingType.STRING, name, description, defaultValue, onChange);

        this.maxLength = maxLength;
        this.validator = validator != null ? validator : s -> true;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(get());
    }

    @Override
    public void deserialize(JsonElement element) {
        set(element.getAsString(), true);
    }

    private transient ImString imguiValue;

    @Override
    public void render(ImGuiIO io, Module parent) {
        if (imguiValue == null) {
            imguiValue = new ImString(get());
        }

        imguiValue.set(get());

        ImGui.setNextItemWidth(ImGui.getColumnWidth());
        if (ImGui.inputText("##" + getName() + "-" + parent.getName(), imguiValue)) {
            set(imguiValue.get(), false);
        }
    }

    @Override
    public void set(String value) {
        if (value == null) {
            return;
        }

        if (value.length() > maxLength) {
            value = value.substring(0, maxLength);
        }

        if (validator.test(value)) {
            super.set(value);
        }
    }

    public int getMaxLength() {
        return maxLength;
    }

    public static class Builder {
        private String name;
        private String description = "";
        private String defaultValue = "";
        private int maxLength = Integer.MAX_VALUE;
        private Predicate<String> validator = s -> true;
        private Consumer<String> onChange;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder validator(Predicate<String> validator) {
            this.validator = validator;
            return this;
        }

        public Builder onChange(Consumer<String> onChange) {
            this.onChange = onChange;
            return this;
        }

        public StringSetting build() {
            return new StringSetting(
                    name,
                    description,
                    defaultValue,
                    maxLength,
                    validator,
                    onChange
            );
        }
    }
}
