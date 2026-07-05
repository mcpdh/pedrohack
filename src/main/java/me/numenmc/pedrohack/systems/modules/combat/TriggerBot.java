package me.numenmc.pedrohack.systems.modules.combat;

import me.numenmc.pedrohack.mixin.KeyMappingAccessor;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.TickEvent;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import me.numenmc.pedrohack.systems.settings.EntityTypesSetting;
import me.numenmc.pedrohack.systems.settings.IntSetting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

public class TriggerBot extends Module {
    SettingCategory mainCategory = SettingCategory.createDefault();

    EntityTypesSetting targets = new EntityTypesSetting.Builder()
            .name("targets")
            .description("The entities that should be targeted by the module")
            .defaultValue(EntityTypes.PLAYER)
            .build();

    IntSetting maxRange = new IntSetting.Builder()
            .name("max-range")
            .description("The maximum range in blocks to attack entities at")
            .defaultValue(4)
            .min(0)
            .max(5)
            .build();

    BoolSetting waitForCooldown = new BoolSetting.Builder()
            .name("wait-for-cooldown")
            .description("Should clicks wait for the attack cooldown to end?")
            .defaultValue(true)
            .build();

    BoolSetting clickOnFall = new BoolSetting.Builder()
            .name("click-on-fall")
            .description("Only click when the player is falling, to force critical hits.")
            .defaultValue(false)
            .build();

    public TriggerBot() {
        super("trigger-bot", "Automatically kills entities");

        mainCategory.add(targets);
        mainCategory.add(maxRange);
        mainCategory.add(waitForCooldown);
        mainCategory.add(clickOnFall);

        addSettingCategory(mainCategory);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) return;
        if (mc.gameMode == null) return;
        if (mc.gui.screen() != null) return;

        if (mc.hitResult instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();

            if (!targets.get().contains(entity.getType())) return;
            if (mc.player.distanceTo(entity) > maxRange.get()) return;
            if (!mc.player.hasLineOfSight(entity)) return;

            if (entity instanceof LivingEntity livingEntity && livingEntity.deathTime > 0f) return;
            if (!entity.isAlive()) return;

            boolean falling =
                    !mc.player.onGround()
                            && mc.player.getDeltaMovement().y < -0.08;

            if (clickOnFall.get() && !falling)
                return;

            float cooldown = mc.player.getAttackStrengthScale(0);
            if (cooldown >= 1f || !waitForCooldown.get()) {
                KeyMapping.click(((KeyMappingAccessor) mc.options.keyAttack).getKey());
            }
        }
    }
}
