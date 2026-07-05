package me.numenmc.pedrohack.systems.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import imgui.ImGui;
import imgui.ImGuiIO;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.Setting;

import java.util.function.Consumer;

public class BoolSetting extends Setting<Boolean> {
    private BoolSetting(
            String name,
            String description,
            boolean defaultValue,
            Consumer<Boolean> onChange
    ) {
        super(SettingType.BOOL, name, description, defaultValue, onChange);
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(get());
    }

    @Override
    public void deserialize(JsonElement element) {
        set(element.getAsBoolean(), true);
    }

    @Override
    public void render(ImGuiIO io, Module parent) {
        boolean value = get();

        if (ImGui.checkbox("##" + getName() + "-" + parent.getName(), value)) {
            set(!value);
        }
    }

    public static class Builder {
        private String name;
        private String description = "";
        private boolean defaultValue;
        private Consumer<Boolean> onChange;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(boolean defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder onChange(Consumer<Boolean> onChange) {
            this.onChange = onChange;
            return this;
        }

        public BoolSetting build() {
            return new BoolSetting(
                    name,
                    description,
                    defaultValue,
                    onChange
            );
        }
    }
}
