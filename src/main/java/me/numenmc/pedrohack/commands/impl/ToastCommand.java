package me.numenmc.pedrohack.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.numenmc.pedrohack.commands.Command;
import me.numenmc.pedrohack.render.notification.NotificationType;
import me.numenmc.pedrohack.render.notification.Notifications;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ToastCommand extends Command {
    public ToastCommand() {
        super("toast", "Force a system toast");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder
                .then(
                        argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String message = StringArgumentType.getString(ctx, "message");

                                    Notifications.pushNotification(NotificationType.HINT, message);

                                    return SINGLE_SUCCESS;
                                })
                );
    }
}
