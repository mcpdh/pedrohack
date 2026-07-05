package me.numenmc.pedrohack.systems.event.events;

import me.numenmc.pedrohack.systems.event.CancellableEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

public class PacketReceiveEvent extends CancellableEvent {
    public final Packet<?> packet;
    public final Connection connection;

    public PacketReceiveEvent(Packet<?> packet, Connection connection) {
        this.packet = packet;
        this.connection = connection;
    }
}
