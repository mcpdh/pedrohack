package me.numenmc.pedrohack.systems.categories;

import me.numenmc.pedrohack.systems.ModuleCategory;
import me.numenmc.pedrohack.systems.modules.player.AutoRelogChunks;
import me.numenmc.pedrohack.systems.modules.player.AutoSurfaceFly;
import me.numenmc.pedrohack.systems.modules.player.Freecam;

public class PlayerCategory extends ModuleCategory {
    public Freecam FREECAM = add(new Freecam());
    public AutoSurfaceFly AUTO_SURFACE_FLY = add(new AutoSurfaceFly());
    public AutoRelogChunks AUTO_RELOG_CHUNKS = add(new AutoRelogChunks());

    public PlayerCategory() {
        super("Player");
    }
}
