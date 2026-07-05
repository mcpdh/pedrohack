package me.numenmc.pedrohack.systems.event.events;

import me.numenmc.pedrohack.systems.event.Event;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkLoadEvent extends Event {
    public final ClientLevel level;
    public final LevelChunk chunk;

    public ChunkLoadEvent(ClientLevel level, LevelChunk chunk) {
        this.level = level;
        this.chunk = chunk;
    }
}
