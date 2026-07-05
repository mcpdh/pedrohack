package me.numenmc.pedrohack.systems.event.events;

import me.numenmc.pedrohack.systems.event.Event;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkUnloadEvent extends Event {
    public final ClientLevel level;
    public final LevelChunk chunk;

    public ChunkUnloadEvent(ClientLevel level, LevelChunk chunk) {
        this.level = level;
        this.chunk = chunk;
    }
}
