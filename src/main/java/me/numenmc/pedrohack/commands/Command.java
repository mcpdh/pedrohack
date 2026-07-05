package me.numenmc.pedrohack.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.data.registries.VanillaRegistries;

import java.util.List;

public abstract class Command {
    protected static CommandBuildContext REGISTRY_ACCESS = Commands.createValidationContext(VanillaRegistries.createLookup());
    protected static final int SINGLE_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;

    private final String name;
    private final String description;
    private final List<String> aliases;

    public Command(String name, String description, String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = List.of(aliases);
    }

    protected static <T> RequiredArgumentBuilder<ClientSuggestionProvider, T> argument(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected static LiteralArgumentBuilder<ClientSuggestionProvider> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public final void registerTo(CommandDispatcher<ClientSuggestionProvider> dispatcher) {
        register(dispatcher, name);
        for (String alias : aliases) register(dispatcher, alias);
    }

    public void register(CommandDispatcher<ClientSuggestionProvider> dispatcher, String name) {
        LiteralArgumentBuilder<ClientSuggestionProvider> builder = LiteralArgumentBuilder.literal(name);
        build(builder);
        dispatcher.register(builder);
    }

    public abstract void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder);

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCommandExecutable() {
        return me.numenmc.pedrohack.commands.Commands.PREFIX + name;
    }
}
