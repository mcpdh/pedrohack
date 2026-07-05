package me.numenmc.pedrohack.systems.categories;

import me.numenmc.pedrohack.systems.ModuleCategory;
import me.numenmc.pedrohack.systems.actions.Panic;
import me.numenmc.pedrohack.systems.actions.RenderDistancePreset1;
import me.numenmc.pedrohack.systems.actions.RenderDistancePreset2;

public class ActionCategory extends ModuleCategory {
    public RenderDistancePreset1 RENDER_DISTANCE_PRESET_1 = add(new RenderDistancePreset1());
    public RenderDistancePreset2 RENDER_DISTANCE_PRESET_2 = add(new RenderDistancePreset2());
    public Panic PANIC = add(new Panic());

    public ActionCategory() {
        super("Executables");
    }
}
