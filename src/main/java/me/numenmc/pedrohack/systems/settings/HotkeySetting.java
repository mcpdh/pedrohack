package me.numenmc.pedrohack.systems.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.InputConstants;
import imgui.ImGuiIO;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.Setting;

import java.util.function.Consumer;

public class HotkeySetting extends Setting<InputConstants.Key> {
    private HotkeySetting(String name, String description, InputConstants.Key defaultValue, Consumer<InputConstants.Key> onChange) {
        super(SettingType.HOTKEY, name, description, defaultValue, onChange);
    }

    @Override
    public JsonElement serialize() {
        InputConstants.Key key = get();
        if (key == null) return JsonNull.INSTANCE;

        return new JsonPrimitive(key.getName());
    }

    @Override
    public void deserialize(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            set(null, true);
            return;
        }

        set(InputConstants.getKey(element.getAsString()), true);
    }

    @Override
    public void render(ImGuiIO io, Module parent) {

    }

    public static class Builder {
        private String name;
        private String description = "";
        private InputConstants.Key defaultValue;
        private Consumer<InputConstants.Key> onChange;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(InputConstants.Key defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder onChange(Consumer<InputConstants.Key> onChange) {
            this.onChange = onChange;
            return this;
        }

        public HotkeySetting build() {
            return new HotkeySetting(
                    name,
                    description,
                    defaultValue,
                    onChange
            );
        }
    }
}
