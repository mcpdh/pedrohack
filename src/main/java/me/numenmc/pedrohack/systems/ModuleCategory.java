package me.numenmc.pedrohack.systems;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModuleCategory {
    private final String name;
    private final List<Module> modules = new ArrayList<>();

    public ModuleCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Module> getModules() {
        return modules;
    }

    public <T extends Module> T add(T module) {
        modules.add(module);
        modules.sort(Comparator.comparing(Module::getDisplayName));
        return module;
    }

    public void remove(Module module) {
        modules.remove(module);
    }
}
