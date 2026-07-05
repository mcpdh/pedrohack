package me.numenmc.pedrohack.systems.modules.render;

import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.PacketReceiveEvent;
import me.numenmc.pedrohack.systems.event.events.TickEvent;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;

public class WeatherChanger extends Module {
    SettingCategory mainCategory = SettingCategory.createDefault();

    BoolSetting enableRain = new BoolSetting.Builder()
            .name("enable-rain")
            .description("Rain status override")
            .build();

    BoolSetting enableThunder = new BoolSetting.Builder()
            .name("enable-thunder")
            .description("Thunderstorm status override")
            .build();

    private float oldThunderLevel;
    private float oldRainLevel;

    public WeatherChanger() {
        super("weather-changer", "Overrides the weather on the client.");

        mainCategory.add(enableRain);
        mainCategory.add(enableThunder);

        addSettingCategory(mainCategory);
    }

    @Override
    protected void onEnable() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) {
            return;
        }

        oldThunderLevel = mc.level.getThunderLevel(1f);
        oldRainLevel = mc.level.getRainLevel(1f);
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) {
            return;
        }

        mc.level.setRainLevel(oldRainLevel);
        mc.level.setThunderLevel(oldThunderLevel);
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.packet instanceof ClientboundGameEventPacket packet) {
            ClientboundGameEventPacket.Type type = packet.getEvent();

            if (type == ClientboundGameEventPacket.START_RAINING
                    || type == ClientboundGameEventPacket.STOP_RAINING
                    || type == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE
                    || type == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE) {

                if (type == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE) {
                    oldThunderLevel = packet.getParam();
                } else if (type == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE) {
                    oldRainLevel = packet.getParam();
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) {
            return;
        }

        mc.level.setRainLevel(enableRain.get() ? 1 : 0);
        mc.level.setThunderLevel(enableThunder.get() ? 1 : 0);
    }
}
