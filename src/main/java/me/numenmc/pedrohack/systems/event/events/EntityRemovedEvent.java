package me.numenmc.pedrohack.systems.event.events;

import me.numenmc.pedrohack.systems.event.Event;
import net.minecraft.world.entity.Entity;

public class EntityRemovedEvent extends Event {
    public Entity entity;

    public EntityRemovedEvent(Entity entity) {
        this.entity = entity;
    }
}
