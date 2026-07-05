package me.numenmc.pedrohack.mixin;

import io.netty.channel.ChannelHandlerContext;
import me.numenmc.pedrohack.systems.event.EventBus;
import me.numenmc.pedrohack.systems.event.events.PacketReceiveEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(Connection.class)
public class ConnectionMixin {
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void onHandlePacket(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClientboundBundlePacket bundle) {
            for (Iterator<Packet<? super ClientGamePacketListener>> it = bundle.subPackets().iterator(); it.hasNext(); ) {
                if (EventBus.post(new PacketReceiveEvent(it.next(), (Connection) (Object) this)).isCancelled())
                    it.remove();
            }
        } else if (EventBus.post(new PacketReceiveEvent(packet, (Connection) (Object) this)).isCancelled())
            ci.cancel();
    }
}
