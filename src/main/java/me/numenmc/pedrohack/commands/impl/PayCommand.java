package me.numenmc.pedrohack.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.numenmc.pedrohack.commands.Command;
import me.numenmc.pedrohack.render.RenderUtil;
import me.numenmc.pedrohack.util.DonutColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public class PayCommand extends Command {
    public PayCommand() {
        super("pay", "Forge a pay command", "fake-pay");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(
                argument("name", StringArgumentType.word())
                        .then(
                                argument("amount", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            String amount = StringArgumentType.getString(ctx, "amount");

                                            Component youPaidComponent =
                                                    Component.literal("You paid")
                                                            .withColor(RenderUtil.withAlpha(DonutColors.GRAY, 255));

                                            Component usernameComponent =
                                                    Component.literal(name)
                                                            .withColor(RenderUtil.withAlpha(DonutColors.BLUE, 255));

                                            Component amountComponent =
                                                    Component.literal("$" + amount.toUpperCase(Locale.ROOT))
                                                            .withColor(RenderUtil.withAlpha(DonutColors.GREEN, 255));

                                            Component periodComponent =
                                                    Component.literal(".")
                                                            .withColor(RenderUtil.withAlpha(DonutColors.GRAY, 255));

                                            Component finalComponent = Component.empty()
                                                    .append(youPaidComponent)
                                                    .append(" ")
                                                    .append(usernameComponent)
                                                    .append(" ")
                                                    .append(amountComponent)
                                                    .append(periodComponent);

                                            Minecraft.getInstance().gui.hud.getChat().addServerSystemMessage(finalComponent);
                                            Minecraft.getInstance().player.sendOverlayMessage(finalComponent);

                                            return SINGLE_SUCCESS;
                                        })
                        )
        );
    }
}
