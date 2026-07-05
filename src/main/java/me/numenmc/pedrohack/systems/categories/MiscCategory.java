package me.numenmc.pedrohack.systems.categories;

import me.numenmc.pedrohack.systems.ModuleCategory;
import me.numenmc.pedrohack.systems.modules.misc.NameProtect;
import me.numenmc.pedrohack.systems.modules.misc.Notifier;
import me.numenmc.pedrohack.systems.modules.misc.SpoofStats;

public class MiscCategory extends ModuleCategory {
    public SpoofStats SPOOF_STATS = add(new SpoofStats());
    public NameProtect NAME_PROTECT = add(new NameProtect());
    public Notifier NOTIFIER = add(new Notifier());

    public MiscCategory() {
        super("Misc");
    }
}
