package me.numenmc.pedrohack.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.numenmc.pedrohack.commands.Command;
import me.numenmc.pedrohack.util.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;

public class KickCommand extends Command {
    public KickCommand() {
        super("kick", "Disconnect yourself");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder
                .executes(ctx -> {
                    LocalPlayer player = Minecraft.getInstance().player;

                    if (player == null) {
                        ChatUtils.sendError("Couldn't kick the player because the player is null");
                        return SINGLE_SUCCESS;
                    }

                    player.connection.handleDisconnect(
                            new ClientboundDisconnectPacket(
                                    Component.literal("Disconnected by Pedrohack command")
                            )
                    );

                    return SINGLE_SUCCESS;
                });
    }
}
