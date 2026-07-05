package me.numenmc.pedrohack.systems.config;

import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.config.categories.AppearanceConfig;
import me.numenmc.pedrohack.systems.config.categories.PlayerListsConfig;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public static AppearanceConfig APPEARANCE = new AppearanceConfig();
    public static PlayerListsConfig PLAYER_LISTS = new PlayerListsConfig();

    // Create list
    private static final List<SettingCategory> v = new ArrayList<>(List.of(
            APPEARANCE,
            PLAYER_LISTS
    ));

    public static List<SettingCategory> values() {
        return v;
    }
}
