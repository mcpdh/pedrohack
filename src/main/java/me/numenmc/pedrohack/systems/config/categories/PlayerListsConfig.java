package me.numenmc.pedrohack.systems.config.categories;

import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.settings.StringsSetting;

public class PlayerListsConfig extends SettingCategory.Contained {
    public PlayerListsConfig() {
        super("Player Lists");
    }

    public StringsSetting friends = addGet(
            new StringsSetting.Builder()
                    .name("friends")
                    .description("The player names that should be marked as friends. Most modules will ignore and not target them.")
                    .defaultValue()
                    .build()
    );
}
