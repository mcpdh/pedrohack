package me.numenmc.pedrohack.systems.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import imgui.ImGuiIO;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.Setting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.*;
import java.util.function.Consumer;

public class EntityTypesSetting extends Setting<Set<EntityType<?>>> {
    private EntityTypesSetting(
            String name,
            String description,
            Set<EntityType<?>> defaultValue,
            Consumer<Set<EntityType<?>>> onChange
    ) {
        super(SettingType.ENTITY_TYPES, name, description, defaultValue, onChange);
    }

    @Override
    public JsonElement serialize() {
        JsonArray arr = new JsonArray();
        for (EntityType<?> type : get()) {
            arr.add(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        }
        return arr;
    }

    @Override
    public void deserialize(JsonElement element) {
        List<EntityType<?>> list = new ArrayList<>();
        for (JsonElement e : element.getAsJsonArray()) {
            Identifier key = Identifier.parse(e.getAsString());
            BuiltInRegistries.ENTITY_TYPE.getOptional(key).ifPresent(list::add);
        }

        set(Set.copyOf(list), true);
    }

    @Override
    public void render(ImGuiIO io, Module parent) {

    }

    public static class Builder {
        private String name;
        private String description = "";
        private Set<EntityType<?>> defaultValue;
        private Consumer<Set<EntityType<?>>> onChange;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(EntityType<?>... values) {
            this.defaultValue = new HashSet<>(Arrays.asList(values));
            return this;
        }

        public Builder onChange(Consumer<Set<EntityType<?>>> onChange) {
            this.onChange = onChange;
            return this;
        }

        public EntityTypesSetting build() {
            return new EntityTypesSetting(
                    name,
                    description,
                    defaultValue,
                    onChange
            );
        }
    }
}
