package me.numenmc.pedrohack.systems.modules.misc;

import me.numenmc.pedrohack.render.notification.NotificationType;
import me.numenmc.pedrohack.render.notification.Notifications;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.EntityAddedEvent;
import me.numenmc.pedrohack.systems.event.events.EntityRemovedEvent;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

public class Notifier extends Module {
    SettingCategory mainCategory = SettingCategory.createDefault();

    BoolSetting notifyPlayersEnteringRange = new BoolSetting.Builder()
            .name("notify-for-players-entering-visual-range")
            .description("Create an alert when a player enters visual range")
            .defaultValue(true)
            .build();

    BoolSetting notifyPlayersExitingRange = new BoolSetting.Builder()
            .name("notify-for-players-exiting-visual-range")
            .description("Create an alert when a player exits visual range")
            .defaultValue(false)
            .build();

    SettingCategory notificationCategory = SettingCategory.createDefault();

    BoolSetting notificationPlaySound = new BoolSetting.Builder()
            .name("play-notification-sound")
            .description("Play an alert sound when a notification is created.")
            .defaultValue(true)
            .build();

    public Notifier() {
        super("notifier", "Tells the player when something important happens.");

        mainCategory.add(notifyPlayersEnteringRange);
        mainCategory.add(notifyPlayersExitingRange);

        notificationCategory.add(notificationPlaySound);

        addSettingCategory(mainCategory);
        addSettingCategory(notificationCategory);
    }

    @EventHandler
    public void onEntityAdded(EntityAddedEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (notifyPlayersEnteringRange.get() && event.entity instanceof Player player) {
            if (player == mc.player) return;

            Notifications.pushNotification(NotificationType.HINT, player.getPlainTextName() + " entered your visual range");

            if (notificationPlaySound.get()) {
                playNotificationSound();
            }
        }
    }

    @EventHandler
    public void onEntityRemoved(EntityRemovedEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (notifyPlayersExitingRange.get() && event.entity instanceof Player player) {
            if (player == mc.player) return;

            Notifications.pushNotification(NotificationType.HINT, player.getPlainTextName() + " exited your visual range");

            if (notificationPlaySound.get()) {
                playNotificationSound();
            }
        }
    }

    private void playNotificationSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        mc.player.playSound(
                SoundEvents.NOTE_BLOCK_PLING.value(),
                1.0f,
                1.0f
        );
    }
}
