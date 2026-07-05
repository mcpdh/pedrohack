package me.numenmc.pedrohack.systems.event.events;

import me.numenmc.pedrohack.systems.event.Event;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityUnloadEvent extends Event {
    public final ClientLevel level;
    public final BlockEntity blockEntity;

    public BlockEntityUnloadEvent(ClientLevel level, BlockEntity blockEntity) {
        this.level = level;
        this.blockEntity = blockEntity;
    }
}
