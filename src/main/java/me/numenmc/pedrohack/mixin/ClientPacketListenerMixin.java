package me.numenmc.pedrohack.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.numenmc.pedrohack.commands.Commands;
import me.numenmc.pedrohack.render.notification.NotificationType;
import me.numenmc.pedrohack.render.notification.Notifications;
import me.numenmc.pedrohack.util.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {
    protected ClientPacketListenerMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci, @Local(argsOnly = true, name = "content") LocalRef<String> messageRef) {
        if (message.startsWith(Commands.PREFIX)) {
            try {
                Commands.dispatch(message.substring(Commands.PREFIX.length()));
            } catch (CommandSyntaxException e) {
                ChatUtils.sendError(e.getMessage());
                Notifications.pushNotification(NotificationType.ERROR, "A command task failed");
            }

            minecraft.gui.hud.getChat().addRecentChat(message);
            ci.cancel();
        }
    }

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void onHandleLoginTail(ClientboundLoginPacket packet, CallbackInfo ci, @Share("worldNotNull") LocalBooleanRef worldNotNull) {
        Commands.onJoin();
    }
}
