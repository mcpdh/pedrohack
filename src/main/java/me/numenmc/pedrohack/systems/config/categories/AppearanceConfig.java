package me.numenmc.pedrohack.systems.config.categories;

import me.numenmc.pedrohack.render.Theme;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.settings.EnumSetting;

public class AppearanceConfig extends SettingCategory.Contained {
    public AppearanceConfig() {
        super("Appearance");
    }

    public EnumSetting<Theme.Fonts> uiFont = addGet(
            new EnumSetting.Builder<>(Theme.Fonts.class)
                .name("ui-font")
                .description("The font used for rendering the custom UI.")
                .defaultValue(Theme.Fonts.LATO)
                .build()
    );
}
