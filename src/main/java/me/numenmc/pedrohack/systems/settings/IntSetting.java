package me.numenmc.pedrohack.systems.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImInt;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.Setting;

import java.util.function.Consumer;

public class IntSetting extends Setting<Integer> {
    private final int min;
    private final int max;

    private IntSetting(
            String name,
            String description,
            int defaultValue,
            int min,
            int max,
            Consumer<Integer> onChange
    ) {
        super(SettingType.INT, name, description, defaultValue, onChange);

        this.min = min;
        this.max = max;
    }

    @Override
    public void set(Integer value) {
        super.set(Math.clamp(value, min, max));
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(get());
    }

    @Override
    public void deserialize(JsonElement element) {
        set(element.getAsInt(), true);
    }

    private transient int[] imguiValue;

    @Override
    public void render(ImGuiIO io, Module parent) {

        if (imguiValue == null) {
            imguiValue = new int[] { get() };
        }

        imguiValue[0] = get();

        String id = getName() + "-" + parent.getName();

        ImGui.setNextItemWidth(ImGui.getColumnWidth());

        if (ImGui.sliderInt("##" + id, imguiValue, 0, 100)) {
            set(imguiValue[0], false);
        }
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public static class Builder {
        private String name;
        private String description = "";
        private int defaultValue;
        private int min = -100;
        private int max = 100;
        private Consumer<Integer> onChange;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(int defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder min(int min) {
            this.min = min;
            return this;
        }

        public Builder max(int max) {
            this.max = max;
            return this;
        }

        public Builder onChange(Consumer<Integer> onChange) {
            this.onChange = onChange;
            return this;
        }

        public IntSetting build() {
            return new IntSetting(
                    name,
                    description,
                    defaultValue,
                    min,
                    max,
                    onChange
            );
        }
    }
}
