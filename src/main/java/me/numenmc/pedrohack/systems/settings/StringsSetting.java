package me.numenmc.pedrohack.systems.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import imgui.ImGuiIO;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class StringsSetting extends Setting<List<String>> {
    private StringsSetting(
            String name,
            String description,
            List<String> defaultValue,
            Consumer<List<String>> onChange
    ) {
        super(SettingType.STRINGS, name, description, defaultValue, onChange);
    }

    @Override
    public JsonElement serialize() {
        JsonArray arr = new JsonArray();
        for (String s : get()) {
            arr.add(s);
        }
        return arr;
    }

    @Override
    public void deserialize(JsonElement element) {
        List<String> list = new ArrayList<>();
        for (JsonElement e : element.getAsJsonArray()) {
            list.add(e.getAsString());
        }
        set(List.copyOf(list), true);
    }

    @Override
    public void render(ImGuiIO io, Module parent) {

    }

    public static class Builder {
        private String name;
        private String description = "";
        private List<String> defaultValue = new ArrayList<>();
        private Consumer<List<String>> onChange;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(String... values) {
            this.defaultValue = new ArrayList<>(Arrays.asList(values));
            return this;
        }

        public Builder onChange(Consumer<List<String>> onChange) {
            this.onChange = onChange;
            return this;
        }

        public StringsSetting build() {
            return new StringsSetting(name, description, defaultValue, onChange);
        }
    }
}
