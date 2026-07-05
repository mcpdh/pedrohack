package me.numenmc.pedrohack.systems.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import imgui.ImGuiIO;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.Setting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.function.Consumer;

public class BlockTypesSetting extends Setting<Set<Block>> {

    private BlockTypesSetting(
            String name,
            String description,
            Set<Block> defaultValue,
            Consumer<Set<Block>> onChange
    ) {
        super(SettingType.BLOCK_TYPES, name, description, defaultValue, onChange);
    }

    @Override
    public JsonElement serialize() {
        JsonArray arr = new JsonArray();
        for (Block block : get()) {
            arr.add(BuiltInRegistries.BLOCK.getKey(block).toString());
        }
        return arr;
    }

    @Override
    public void deserialize(JsonElement element) {
        List<Block> list = new ArrayList<>();
        for (JsonElement e : element.getAsJsonArray()) {
            Identifier key = Identifier.parse(e.getAsString());
            BuiltInRegistries.BLOCK.getOptional(key).ifPresent(list::add);
        }
        set(Set.copyOf(list), true);
    }

    @Override
    public void render(ImGuiIO io, Module parent) {

    }

    public static class Builder {
        private String name;
        private String description = "";
        private Set<Block> defaultValue;
        private Consumer<Set<Block>> onChange;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(Block... values) {
            this.defaultValue = new HashSet<>(Arrays.asList(values));
            return this;
        }

        public Builder onChange(Consumer<Set<Block>> onChange) {
            this.onChange = onChange;
            return this;
        }

        public BlockTypesSetting build() {
            return new BlockTypesSetting(
                    name,
                    description,
                    defaultValue,
                    onChange
            );
        }
    }
}
