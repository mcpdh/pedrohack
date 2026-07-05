package me.numenmc.pedrohack.systems.modules.player;

import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.TickEvent;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import me.numenmc.pedrohack.systems.settings.IntSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;

public class AutoRelogChunks extends Module {
    SettingCategory mainCategory = SettingCategory.createDefault();

    IntSetting relogYLevel = new IntSetting.Builder()
            .name("relog-y-level")
            .description("The y-level to disconnect at.")
            .defaultValue(-8)
            .min(-64)
            .max(64)
            .build();

    IntSetting cooldownSeconds = new IntSetting.Builder()
            .name("relog-cooldown")
            .description("The cooldown in seconds before being allowed to relog again. Prevents being stuck in a relog loop.")
            .min(1)
            .max(300)
            .defaultValue(60)
            .build();

    BoolSetting disableOnRelog = new BoolSetting.Builder()
            .name("disable-on-relog")
            .description("Disable the module once the relog has occurred.")
            .defaultValue(false)
            .build();

    public AutoRelogChunks() {
        super("auto-relog-chunks", "Automatically relog at a certain y-level to perform the relog method.");

        mainCategory.add(relogYLevel);
        mainCategory.add(cooldownSeconds);
        mainCategory.add(disableOnRelog);

        addSettingCategory(mainCategory);
    }

    private int cooldownTicks = 0;

    @Override
    protected void onDisable() {
        cooldownTicks = 0;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (mc.player.getBlockY() == relogYLevel.get()) {
            cooldownTicks = cooldownSeconds.get() * 20;
            if (disableOnRelog.get()) setEnabled(false);

            mc.player.connection.handleDisconnect(
                    new ClientboundDisconnectPacket(
                            Component.literal("Disconnected by " + this.getDisplayName())
                    )
            );
        }
    }
}
