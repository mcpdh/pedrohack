package me.numenmc.pedrohack.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.numenmc.pedrohack.commands.impl.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandBuildContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Commands {
    public static final List<Command> COMMANDS = new ArrayList<>();
    public static CommandDispatcher<ClientSuggestionProvider> DISPATCHER = new CommandDispatcher<>();

    public static final String PREFIX = ";";

    public static void init() {
        // add
        COMMANDS.add(new ToggleCommand());
        COMMANDS.add(new ToastCommand());
        COMMANDS.add(new PayCommand());
        COMMANDS.add(new KickCommand());
        COMMANDS.add(new ExecuteCommand());
        // end

        COMMANDS.sort(Comparator.comparing(Command::getName));
    }

    public static void dispatch(String message) throws CommandSyntaxException {
        ClientPacketListener networkHandler = Minecraft.getInstance().getConnection();
        if (networkHandler == null) return;
        DISPATCHER.execute(message, networkHandler.getSuggestionsProvider());
    }

    public static Command get(String name) {
        for (Command command : COMMANDS) {
            if (command.getName().equals(name)) {
                return command;
            }
        }

        return null;
    }

    public static void onJoin() {
        ClientPacketListener networkHandler = Minecraft.getInstance().getConnection();
        if (networkHandler == null) return;
        Command.REGISTRY_ACCESS = CommandBuildContext.simple(networkHandler.registryAccess(), networkHandler.enabledFeatures());

        DISPATCHER = new CommandDispatcher<>();
        for (Command command : COMMANDS) {
            command.registerTo(DISPATCHER);
        }
    }
}

