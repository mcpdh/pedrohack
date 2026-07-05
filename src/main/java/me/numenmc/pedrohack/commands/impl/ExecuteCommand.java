package me.numenmc.pedrohack.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.numenmc.pedrohack.commands.Command;
import me.numenmc.pedrohack.commands.type.ActionModuleArgumentType;
import me.numenmc.pedrohack.systems.Module;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ExecuteCommand extends Command {
    public ExecuteCommand() {
        super("execute", "Execute specific modules", "exec");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder
                .then(
                        argument("module", ActionModuleArgumentType.create())
                                .executes(ctx -> {
                                    Module m = ActionModuleArgumentType.get(ctx, "module");
                                    m.toggle();
                                    return SINGLE_SUCCESS;
                                })
                );
    }
}
