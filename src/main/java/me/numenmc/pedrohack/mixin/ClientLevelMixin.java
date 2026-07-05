package me.numenmc.pedrohack.mixin;

import me.numenmc.pedrohack.systems.event.EventBus;
import me.numenmc.pedrohack.systems.event.events.EntityAddedEvent;
import me.numenmc.pedrohack.systems.event.events.EntityRemovedEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Shadow
    @Nullable
    public abstract Entity getEntity(int id);

    @Inject(method = "addEntity", at = @At("TAIL"))
    private void onAddEntity(Entity entity, CallbackInfo ci) {
        if (entity != null) EventBus.post(new EntityAddedEvent(entity));
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void onRemoveEntity(int id, Entity.RemovalReason reason, CallbackInfo ci) {
        if (getEntity(id) != null)
            EventBus.post(new EntityRemovedEvent(getEntity(id)));
    }
}
