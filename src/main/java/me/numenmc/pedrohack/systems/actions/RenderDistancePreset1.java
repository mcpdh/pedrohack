package me.numenmc.pedrohack.systems.actions;

import me.numenmc.pedrohack.render.notification.NotificationType;
import me.numenmc.pedrohack.render.notification.Notifications;
import me.numenmc.pedrohack.systems.ActionModule;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.settings.IntSetting;
import net.minecraft.client.Minecraft;

public class RenderDistancePreset1 extends ActionModule {
    SettingCategory mainCategory = SettingCategory.createDefault();

    IntSetting renderDistance = new IntSetting.Builder()
            .name("render-distance")
            .description("The render distance to set to.")
            .defaultValue(2)
            .min(2)
            .max(32)
            .build();

    public RenderDistancePreset1() {
        super("render-distance-p1", "Quickly change your render distance to a certain value. Useful for render distance glitch to load chunks on DonutSMP. (Preset 1)");

        mainCategory.add(renderDistance);
        addSettingCategory(mainCategory);
    }

    @Override
    public void onExecute() {
        Minecraft.getInstance().options.renderDistance().set(renderDistance.get());
        Notifications.pushNotification(NotificationType.HINT, String.format("Render distance: %d", renderDistance.get()));
    }
}
