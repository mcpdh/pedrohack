package me.numenmc.pedrohack.systems.actions;

import me.numenmc.pedrohack.render.notification.NotificationType;
import me.numenmc.pedrohack.render.notification.Notifications;
import me.numenmc.pedrohack.systems.ActionModule;
import me.numenmc.pedrohack.systems.Categories;
import me.numenmc.pedrohack.systems.SessionConfig;

public class Panic extends ActionModule {
    public Panic() {
        super("panic", "Disable modules and delete all HUD elements");
    }

    @Override
    public void onExecute() {
        Categories.getAllTogglableModules().forEach(module -> module.setEnabled(false));
        SessionConfig.loadedHudElements.clear();

        Notifications.pushNotification(NotificationType.GENERIC, "Disabled all modules and HUD elements");
    }
}
