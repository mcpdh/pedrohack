package me.numenmc.pedrohack.systems;

import com.google.gson.JsonObject;
import me.numenmc.pedrohack.imgui.RenderInterface;
import me.numenmc.pedrohack.render.notification.NotificationType;
import me.numenmc.pedrohack.render.notification.Notifications;
import me.numenmc.pedrohack.render.window.ModuleSettingsWindow;
import me.numenmc.pedrohack.systems.event.EventBus;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import me.numenmc.pedrohack.systems.settings.HotkeySetting;
import me.numenmc.pedrohack.util.NamingUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private String displayName = null;

    private final String name;
    private final String description;
    protected boolean enabled;

    private final List<SettingCategory> settings = new ArrayList<>();

    private transient final ModuleSettingsWindow settingsWindow;

    SettingCategory keybindsCategory = new SettingCategory("Keybinding");

    public BoolSetting sgToggleOnBindRelease = new BoolSetting.Builder()
            .name("toggle-on-bind-release")
            .description("Toggle the module again when the keybind is released. This allows the module to only be on (or off) for the duration that the keybind is held down.")
            .defaultValue(false)
            .onChange(v -> Categories.reloadKeybinds())
            .build();

    public HotkeySetting sgKeyBind = new HotkeySetting.Builder()
            .name("toggle-keybind")
            .description("The hotkey that toggles this module upon press (or release if Toggle On Bind Release is enabled).")
            .defaultValue(null)
            .onChange(v -> Categories.reloadKeybinds())
            .build();

    public Module(String name, String description) {
        this(name, description, false);
    }

    public Module(String name, String description, boolean patched) {
        this.name = name;

        if (patched) {
            this.description = "[! Patched/Detectable] " + description;
        } else {
            this.description = description;
        }

        keybindsCategory.add(sgToggleOnBindRelease);
        keybindsCategory.add(sgKeyBind);

        // Do not use the addSettingCategory function here!
        settings.add(keybindsCategory);

        this.settingsWindow = new ModuleSettingsWindow(this);
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;

        this.enabled = enabled;

        if (enabled) {
            EventBus.subscribe(this);
            onEnable();
        } else {
            EventBus.unsubscribe(this);
            onDisable();
        }
    }

    public void setEnabledSilent(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) EventBus.subscribe(this);
        else EventBus.unsubscribe(this);
    }

    public void addSettingCategory(SettingCategory setting) {
        int index = Math.max(0, settings.size() - 1);
        settings.add(index, setting);
    }

    public List<SettingCategory> getSettingCategories() {
        return settings;
    }

    protected void onEnable() {}
    protected void onDisable() {}

    public String getDescription() {
        return description;
    }
    public String getDisplayName() {
        if (displayName != null) return displayName;

        displayName = NamingUtils.getDisplayName(getName());
        return displayName;
    }

    public JsonObject serialize() {
        JsonObject root = new JsonObject();
        root.addProperty("enabled", isEnabled());
        JsonObject settingsRoot = new JsonObject();

        for (SettingCategory category : getSettingCategories()) {
            for (Setting<?> setting : category.getSettings()) {
                settingsRoot.add(setting.getName(), setting.serialize());
            }
        }

        root.add("settings", settingsRoot);
        return root;
    }

    public void deserialize(JsonObject root) {
        if (root.has("enabled")) {
            setEnabledSilent(root.get("enabled").getAsBoolean());
        }

        JsonObject settingsRoot = root.getAsJsonObject("settings");

        for (SettingCategory category : getSettingCategories()) {
            for (Setting<?> setting : category.getSettings()) {
                if (settingsRoot.has(setting.getName())) {
                    try { setting.deserialize(settingsRoot.get(setting.getName())); }
                    catch (Exception ignored) {} // corrupt value, just skip
                }
            }
        }
    }

    public void sendNotification() {
        if (enabled) Notifications.pushNotification(NotificationType.GENERIC, "Enabled " + this.getDisplayName());
        else Notifications.pushNotification(NotificationType.GENERIC, "Disabled " + this.getDisplayName());
    }

    public ModuleSettingsWindow getSettingsWindow() {
        return settingsWindow;
    }
}
