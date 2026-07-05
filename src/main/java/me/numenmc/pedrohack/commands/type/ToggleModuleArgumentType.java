package me.numenmc.pedrohack.commands.type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.numenmc.pedrohack.systems.Categories;
import me.numenmc.pedrohack.systems.Module;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ToggleModuleArgumentType implements ArgumentType<Module> {
    private static final ToggleModuleArgumentType INSTANCE = new ToggleModuleArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_MODULE = new DynamicCommandExceptionType(name -> Component.literal("There is no module called " + name));

    private static final Collection<String> EXAMPLES = Categories.getAllTogglableModules()
            .stream()
            .limit(3)
            .map(Module::getName)
            .toList();

    public static ToggleModuleArgumentType create() {
        return INSTANCE;
    }

    public static Module get(CommandContext<?> context, String name) {
        return context.getArgument(name, Module.class);
    }

    private ToggleModuleArgumentType() {
    }

    @Override
    public Module parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        Module module = Categories.getTogglableByName(argument);
        if (module == null) throw NO_SUCH_MODULE.create(argument);

        return module;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Categories.getAllTogglableModules().stream().map(Module::getName), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
