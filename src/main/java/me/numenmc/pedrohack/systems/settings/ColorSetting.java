package me.numenmc.pedrohack.systems.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import imgui.ImGui;
import imgui.ImGuiIO;
import me.numenmc.pedrohack.render.world.Color;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.Setting;

import java.util.function.Consumer;

public class ColorSetting extends Setting<Color> {
    private ColorSetting(
            String name,
            String description,
            Color defaultValue,
            Consumer<Color> onChange
    ) {
        super(SettingType.COLOR, name, description, defaultValue, onChange);
    }

    @Override
    public JsonElement serialize() {
        JsonObject obj = new JsonObject();
        Color c = get();
        obj.addProperty("r", c.r);
        obj.addProperty("g", c.g);
        obj.addProperty("b", c.b);
        obj.addProperty("a", c.a);
        return obj;
    }

    @Override
    public void deserialize(JsonElement element) {
        JsonObject obj = element.getAsJsonObject();
        set(new Color(
                obj.get("r").getAsInt(),
                obj.get("g").getAsInt(),
                obj.get("b").getAsInt(),
                obj.get("a").getAsInt()
        ), true);
    }

    private transient float[] imguiColor;

    @Override
    public void render(ImGuiIO io, Module parent) {

        Color c = get();

        if (imguiColor == null) {
            imguiColor = new float[4];
        }

        imguiColor[0] = c.r / 255f;
        imguiColor[1] = c.g / 255f;
        imguiColor[2] = c.b / 255f;
        imguiColor[3] = c.a / 255f;

        String id = getName() + "-" + parent.getName();

        if (ImGui.colorEdit4("##" + id, imguiColor)) {
            c.r = (int)(imguiColor[0] * 255f);
            c.g = (int)(imguiColor[1] * 255f);
            c.b = (int)(imguiColor[2] * 255f);
            c.a = (int)(imguiColor[3] * 255f);

            set(c, false);
        }
    }

    public static class Builder {
        private String name;
        private String description = "";
        private Color defaultValue;
        private Consumer<Color> onChange;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(Color defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder onChange(Consumer<Color> onChange) {
            this.onChange = onChange;
            return this;
        }

        public ColorSetting build() {
            return new ColorSetting(
                    name,
                    description,
                    defaultValue,
                    onChange
            );
        }
    }
}
