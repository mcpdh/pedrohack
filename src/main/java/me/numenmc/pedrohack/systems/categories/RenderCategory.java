package me.numenmc.pedrohack.systems.categories;

import me.numenmc.pedrohack.systems.ModuleCategory;
import me.numenmc.pedrohack.systems.modules.render.*;

public class RenderCategory extends ModuleCategory {
    public Fullbright FULLBRIGHT = add(new Fullbright());
    public WeatherChanger WEATHER_CHANGER = add(new WeatherChanger());
    public Tracers TRACERS = add(new Tracers());
    public EntityEsp ENTITY_ESP = add(new EntityEsp());
    public StorageEsp STORAGE_ESP = add(new StorageEsp());
    public Nametags NAMETAGS = add(new Nametags());
    public TunnelEsp TUNNEL_ESP = add(new TunnelEsp());
    public BlockEsp BLOCK_ESP = add(new BlockEsp());

    public RenderCategory() {
        super("Render");
    }
}
