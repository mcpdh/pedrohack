package me.numenmc.pedrohack.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import me.numenmc.pedrohack.commands.Commands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
    @Shadow
    private @Nullable ParseResults<ClientSuggestionProvider> currentParse;

    @Shadow
    @Final
    private EditBox input;

    @Shadow
    private CommandSuggestions.SuggestionsList suggestions;

    @Shadow
    private @Nullable CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    protected abstract void updateUsageInfo(ParseResults<ClientSuggestionProvider> currentParse, Suggestions suggestions);

    @Shadow
    private boolean keepSuggestions;

    @Inject(method = "updateCommandInfo",
            at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false),
            cancellable = true
    )
    public void updateCommandInfo(CallbackInfo ci, @Local(name = "reader") StringReader reader) {
        Minecraft mc = Minecraft.getInstance();
        int length = Commands.PREFIX.length();

        if (reader.canRead(length) && reader.getString().startsWith(Commands.PREFIX, reader.getCursor())) {
            reader.setCursor(reader.getCursor() + length);

            if (this.currentParse == null) {
                this.currentParse = Commands.DISPATCHER.parse(reader, mc.getConnection().getSuggestionsProvider());
            }

            int cursor = input.getCursorPosition();
            if (cursor >= length && (this.suggestions == null || !this.keepSuggestions)) {
                this.pendingSuggestions = Commands.DISPATCHER.getCompletionSuggestions(this.currentParse, cursor);
                this.pendingSuggestions.thenAccept(suggestionResult -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.updateUsageInfo(this.currentParse, suggestionResult);
                    }
                });
            }

            ci.cancel();
        }
    }
}
