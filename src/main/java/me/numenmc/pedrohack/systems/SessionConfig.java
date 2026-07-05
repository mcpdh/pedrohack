package me.numenmc.pedrohack.systems;

import com.google.gson.*;
import me.numenmc.pedrohack.Pedrohack;
import me.numenmc.pedrohack.systems.config.Config;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SessionConfig {
    private static final Path CONFIG_DIR = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("pedrohack");

    private static final Path MODULES_PATH =
            CONFIG_DIR.resolve("pdh.modules.json");

    private static final Path HUD_PATH =
            CONFIG_DIR.resolve("pdh.hud.json");

    private static final Path SETTINGS_PATH =
            CONFIG_DIR.resolve("pdh.settings.json");

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    public static final List<HudElement> loadedHudElements =
            new ArrayList<>();

    private static void ensureConfigDirectory() throws IOException {
        Files.createDirectories(CONFIG_DIR);
    }

    public static void saveModules() {
        try {
            ensureConfigDirectory();

            JsonObject root = new JsonObject();

            for (ModuleCategory category : Categories.values()) {
                for (Module module : category.getModules()) {
                    root.add(module.getName(), module.serialize());
                }
            }

            Files.writeString(MODULES_PATH, GSON.toJson(root));

        } catch (IOException e) {
            Pedrohack.log.error("Failed to save module config", e);
        }
    }

    public static void loadModules() {
        if (!Files.exists(MODULES_PATH)) return;

        try {
            JsonObject root = JsonParser.parseString(
                    Files.readString(MODULES_PATH)
            ).getAsJsonObject();

            for (ModuleCategory category : Categories.values()) {
                for (Module module : category.getModules()) {
                    if (root.has(module.getName())) {
                        module.deserialize(
                                root.getAsJsonObject(module.getName())
                        );
                    }
                }
            }

        } catch (Exception e) {
            Pedrohack.log.error("Failed to load module config", e);
        }
    }

    public static void saveHud() {
        try {
            ensureConfigDirectory();
            JsonArray root = new JsonArray();
            for (HudElement element : loadedHudElements) {
                JsonObject entry = new JsonObject();
                entry.addProperty("name", HudElements.getName(element.getClass()));
                entry.add("data", element.serialize());
                root.add(entry);
            }
            Files.writeString(HUD_PATH, GSON.toJson(root));
        } catch (IOException e) {
            Pedrohack.log.error("Failed to save HUD config", e);
        }
    }

    public static void loadHud() {
        if (!Files.exists(HUD_PATH)) return;

        try {
            JsonArray root = JsonParser.parseString(
                    Files.readString(HUD_PATH)
            ).getAsJsonArray();

            loadedHudElements.clear();

            for (JsonElement jsonElement : root) {
                JsonObject entry = jsonElement.getAsJsonObject();
                String name = entry.get("name").getAsString();
                JsonObject data = entry.getAsJsonObject("data");

                HudElement element = HudElements.create(HudElements.fromName(name));

                try {
                    element.deserialize(data);
                } catch (Exception e) {
                    Pedrohack.log.warn("Failed to deserialize HUD element {}", name, e);
                }

                loadedHudElements.add(element);
            }
        } catch (Exception e) {
            Pedrohack.log.error("Failed to load HUD config", e);
        }
    }

    public static void saveSettings() {
        try {
            ensureConfigDirectory();

            JsonObject root = new JsonObject();

            for (SettingCategory category : Config.values()) {
                for (Setting<?> setting : category.getSettings()) {
                    root.add(
                            setting.getName(),
                            setting.serialize()
                    );
                }
            }

            Files.writeString(
                    SETTINGS_PATH,
                    GSON.toJson(root)
            );

        } catch (IOException e) {
            Pedrohack.log.error("Failed to save settings config", e);
        }
    }

    public static void loadSettings() {
        if (!Files.exists(SETTINGS_PATH)) return;

        try {
            JsonObject root = JsonParser.parseString(
                    Files.readString(SETTINGS_PATH)
            ).getAsJsonObject();

            for (SettingCategory category : Config.values()) {
                for (Setting<?> setting : category.getSettings()) {
                    if (root.has(setting.getName())) {
                        setting.deserialize(
                                root.getAsJsonPrimitive(setting.getName())
                        );
                    }
                }
            }

        } catch (Exception e) {
            Pedrohack.log.error("Failed to load settings config", e);
        }
    }

    public static void load() {
        loadModules();
        loadHud();
        loadSettings();
    }
}
