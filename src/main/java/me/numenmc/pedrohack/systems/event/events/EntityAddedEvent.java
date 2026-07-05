package me.numenmc.pedrohack.systems.event.events;

import me.numenmc.pedrohack.systems.event.Event;
import net.minecraft.world.entity.Entity;

public class EntityAddedEvent extends Event {
    public Entity entity;

    public EntityAddedEvent(Entity entity) {
        this.entity = entity;
    }
}
