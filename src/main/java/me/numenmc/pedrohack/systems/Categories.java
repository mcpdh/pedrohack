package me.numenmc.pedrohack.systems;

import me.numenmc.pedrohack.systems.categories.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Categories {
    private static final List<Module> allModules = new ArrayList<>();
    private static final List<Module> allTogglableModules = new ArrayList<>();
    private static final List<Module> allActionableModules = new ArrayList<>();

    private static final Map<Integer, List<Module>> keybindMap = new HashMap<>();

    public static RenderCategory RENDER = new RenderCategory();
    public static MiscCategory MISC = new MiscCategory();
    public static PlayerCategory PLAYER = new PlayerCategory();
    public static SusChunksCategory SUS_CHUNKS = new SusChunksCategory();
    public static CombatCategory COMBAT = new CombatCategory();

    public static ActionCategory ACTIONS = new ActionCategory();

    // Create list
    private static final List<ModuleCategory> v = new ArrayList<>(List.of(
            COMBAT,
            RENDER,
            PLAYER,
            SUS_CHUNKS,
            MISC
    ));

    static {
        for (ModuleCategory category : values()) {
            allModules.addAll(category.getModules());

            category.getModules().forEach(module -> {
                if (module instanceof ActionModule) {
                    allActionableModules.add(module);
                } else {
                    allTogglableModules.add(module);
                }
            });
        }
    }

    public static List<ModuleCategory> values() {
        return v;
    }

    public static List<Module> getAllModules() {
        return allModules;
    }

    public static List<Module> getAllTogglableModules() {
        return allTogglableModules;
    }

    public static List<Module> getAllActionableModules() {
        return allActionableModules;
    }

    public static Module getByName(String name) {
        return allModules.stream().filter(n -> n.getName().equals(name)).findFirst().orElse(null);
    }

    public static Module getTogglableByName(String name) {
        return allTogglableModules.stream().filter(n -> n.getName().equals(name)).findFirst().orElse(null);
    }

    public static Module getActionableByName(String name) {
        return allActionableModules.stream().filter(n -> n.getName().equals(name)).findFirst().orElse(null);
    }

    public static void reloadKeybinds() {
        keybindMap.clear();

        for (Module module : getAllModules()) {
            var key = module.sgKeyBind.get();

            if (key == null) continue;

            keybindMap
                    .computeIfAbsent(
                            key.getValue(),
                            v -> new ArrayList<>()
                    )
                    .add(module);
        }
    }

    public static void onKeybindPress(int keyCode) {
        List<Module> modules = keybindMap.get(keyCode);
        if (modules == null) return;

        for (Module module : modules) {
            module.toggle();
            if (!module.sgToggleOnBindRelease.get()) module.sendNotification();
        }
    }

    public static void onKeybindRelease(int keyCode) {
        List<Module> modules = keybindMap.get(keyCode);
        if (modules == null) return;

        for (Module module : modules) {
            if (module.sgToggleOnBindRelease.get()) {
                module.toggle();
            }
        }
    }
}
