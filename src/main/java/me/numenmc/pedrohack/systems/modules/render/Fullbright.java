package me.numenmc.pedrohack.systems.modules.render;

import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.settings.IntSetting;
import net.minecraft.client.Minecraft;

public class Fullbright extends Module {
    SettingCategory mainCategory = SettingCategory.createDefault();

    public IntSetting luminosity = new IntSetting.Builder()
            .name("luminosity-override")
            .description("The value to use as the world's luminosity.")
            .defaultValue(15)
            .min(1)
            .max(15)
            .onChange((v) -> allChanged())
            .build();

    public Fullbright() {
        super("fullbright", "Render the world at full brightness.");

        mainCategory.add(luminosity);

        addSettingCategory(mainCategory);
    }

    @Override
    protected void onEnable() {
        allChanged();
    }

    @Override
    protected void onDisable() {
        allChanged();
    }

    private void allChanged() {
        Minecraft.getInstance().levelExtractor.allChanged();
    }
}
