package me.numenmc.pedrohack.util;

import me.numenmc.pedrohack.render.Theme;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ChatUtils {
    private static final Minecraft mc = Minecraft.getInstance();

    public static void sendPlayerMsg(String message, boolean addToHistory) {
        if (mc.player != null) {
            if (addToHistory) mc.gui.hud.getChat().addRecentChat(message);

            if (message.startsWith("/")) mc.player.connection.sendCommand(message.substring(1));
            else mc.player.connection.sendChat(message);
        }
    }

    private static MutableComponent chatPrefix() {
        return Component.literal("[Pedrohack] ")
                .withColor(Theme.PRIMARY_LIGHTEN_1);
    }


    public static void sendFormatted(Component message) {
        mc.gui.hud.getChat().addClientSystemMessage(chatPrefix().append(message));
    }

    public static void sendError(MutableComponent message) {
        sendFormatted(message.withStyle(ChatFormatting.RED));
    }

    public static void sendError(String message) {
        sendError(Component.literal(message));
    }
}
