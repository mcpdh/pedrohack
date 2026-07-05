package me.numenmc.pedrohack.systems.categories;

import me.numenmc.pedrohack.systems.ModuleCategory;
import me.numenmc.pedrohack.systems.modules.combat.AimAssist;
import me.numenmc.pedrohack.systems.modules.combat.AutoTotem;
import me.numenmc.pedrohack.systems.modules.combat.InvTotem;
import me.numenmc.pedrohack.systems.modules.combat.TriggerBot;

public class CombatCategory extends ModuleCategory {
    public TriggerBot TRIGGER_BOT = add(new TriggerBot());
    public AimAssist AIM_ASSIST = add(new AimAssist());
    public AutoTotem AUTO_TOTEM = add(new AutoTotem());
    public InvTotem INV_TOTEM = add(new InvTotem());

    public CombatCategory() {
        super("Combat");
    }
}
