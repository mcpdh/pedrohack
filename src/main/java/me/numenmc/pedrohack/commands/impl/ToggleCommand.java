package me.numenmc.pedrohack.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.numenmc.pedrohack.commands.Command;
import me.numenmc.pedrohack.commands.type.ToggleModuleArgumentType;
import me.numenmc.pedrohack.systems.Module;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "Toggle specific modules", "t");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder
                .then(
                        argument("module", ToggleModuleArgumentType.create())
                                .executes(ctx -> {
                                    Module m = ToggleModuleArgumentType.get(ctx, "module");
                                    m.toggle();
                                    m.sendNotification();
                                    return SINGLE_SUCCESS;
                                })
                                .then(literal("on")
                                        .executes(ctx -> {
                                            Module m = ToggleModuleArgumentType.get(ctx, "module");
                                            m.setEnabled(true);
                                            m.sendNotification();
                                            return SINGLE_SUCCESS;
                                        }))
                                .then(literal("off")
                                        .executes(ctx -> {
                                            Module m = ToggleModuleArgumentType.get(ctx, "module");
                                            m.setEnabled(false);
                                            m.sendNotification();
                                            return SINGLE_SUCCESS;
                                        })
                                )
                );
    }
}
