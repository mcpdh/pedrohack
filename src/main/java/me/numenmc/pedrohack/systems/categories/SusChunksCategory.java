package me.numenmc.pedrohack.systems.categories;

import me.numenmc.pedrohack.systems.ModuleCategory;
import me.numenmc.pedrohack.systems.modules.suschunks.ClusterChunks;
import me.numenmc.pedrohack.systems.modules.suschunks.GeodeChunks;
import me.numenmc.pedrohack.systems.modules.suschunks.KelpChunks;
import me.numenmc.pedrohack.systems.modules.suschunks.NewChunks;

public class SusChunksCategory extends ModuleCategory {
    public KelpChunks KELP_CHUNKS = add(new KelpChunks());
    public ClusterChunks CLUSTER_CHUNKS = add(new ClusterChunks());
    public GeodeChunks GEODE_CHUNKS = add(new GeodeChunks());
    public NewChunks NEW_CHUNKS = add(new NewChunks());

    public SusChunksCategory() {
        super("Sus Chunks");
    }
}
