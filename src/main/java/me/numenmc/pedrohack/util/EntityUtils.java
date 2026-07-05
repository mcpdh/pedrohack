package me.numenmc.pedrohack.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;

public class EntityUtils {
    private static final Minecraft mc = Minecraft.getInstance();

    public static int getPing(Player player) {
        if (mc.getConnection() == null) return 0;

        PlayerInfo playerListEntry = mc.getConnection().getPlayerInfo(player.getUUID());
        if (playerListEntry == null) return 0;
        return playerListEntry.getLatency();
    }
}
