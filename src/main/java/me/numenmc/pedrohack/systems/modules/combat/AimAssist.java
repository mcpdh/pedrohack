package me.numenmc.pedrohack.systems.modules.combat;

import me.numenmc.pedrohack.systems.Categories;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.TickEvent;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import me.numenmc.pedrohack.systems.settings.EntityTypesSetting;
import me.numenmc.pedrohack.systems.settings.EnumSetting;
import me.numenmc.pedrohack.systems.settings.IntSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.function.Consumer;

public class AimAssist extends Module {
    SettingCategory mainCategory = SettingCategory.createDefault();

    public enum AimAssistMode {
        Ease,
        Snap;

        @Override
        public String toString() {
            if (this == Snap) return "Snap (detectable)";
            else return super.toString();
        }
    }

    public EnumSetting<AimAssistMode> aimAssistMode = new EnumSetting.Builder<>(AimAssistMode.class)
            .name("mode")
            .description("The mode to operate the module in")
            .defaultValue(AimAssistMode.Ease)
            .build();

    public EntityTypesSetting targets = new EntityTypesSetting.Builder()
            .name("targets")
            .description("The entities to target with the module")
            .defaultValue(EntityTypes.PLAYER)
            .build();

    public IntSetting maxDistance = new IntSetting.Builder()
            .name("max-distance")
            .description("The maximum distance to target entities from")
            .defaultValue(6)
            .min(1)
            .max(128)
            .build();

    public BoolSetting requireMouseMove = new BoolSetting.Builder()
            .name("act-on-mouse")
            .description("Only apply forces to the mouse when there are already deltas on it")
            .defaultValue(true)
            .build();

    public AimAssist() {
        super("aim-assist", "Apply additional mouse vectors to fix aim");

        mainCategory.add(aimAssistMode);
        mainCategory.add(targets);
        mainCategory.add(maxDistance);
        mainCategory.add(requireMouseMove);

        addSettingCategory(mainCategory);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null)
            return;

        if (aimAssistMode.get() == AimAssistMode.Snap) {
            Entity target = mc.level.getEntities(
                            mc.player,
                            mc.player.getBoundingBox().inflate(Categories.COMBAT.AIM_ASSIST.maxDistance.get()),
                            e -> Categories.COMBAT.AIM_ASSIST.targets.get().contains(e.getType()) && e != mc.player
                    ).stream()
                    .min(Comparator.comparingDouble(e -> e.distanceToSqr(mc.player)))
                    .orElse(null);

            if (target == null) return;

            lookAt(mc.player, target.getEyePosition(mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)));
        }
    }

    private void lookAt(LocalPlayer player, Vec3 target) {
        Vec3 eye = player.getEyePosition();

        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;

        float yaw =
                (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;

        float pitch =
                (float) -Math.toDegrees(
                        Math.atan2(
                                dy,
                                Math.sqrt(dx * dx + dz * dz)
                        )
                );

        player.setYRot(yaw);
        player.setXRot(pitch);

        player.yRotO = yaw;
        player.xRotO = pitch;

        player.setYHeadRot(yaw);
        player.setYBodyRot(yaw);
    }

    public void onUpdateMouse(double accumulatedDX, double accumulatedDY, Consumer<Double> setDX, Consumer<Double> setDY) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null)
            return;

        if (Categories.COMBAT.AIM_ASSIST.requireMouseMove.get() && accumulatedDX == 0 && accumulatedDY == 0)
            return;

        if (Categories.COMBAT.AIM_ASSIST.aimAssistMode.get() == AimAssistMode.Ease) {
            Entity target = mc.level.getEntities(
                            mc.player,
                            mc.player.getBoundingBox().inflate(Categories.COMBAT.AIM_ASSIST.maxDistance.get()),
                            e -> Categories.COMBAT.AIM_ASSIST.targets.get().contains(e.getType()) && e != mc.player
                    ).stream()
                    .min(Comparator.comparingDouble(e -> e.distanceToSqr(mc.player)))
                    .orElse(null);

            if (target == null)
                return;

            Vec3 eye = mc.player.getEyePosition();

            Vec3 toTarget = target.getEyePosition(mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)).subtract(eye);

            double horizontal = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);

            float desiredYaw = (float) Math.toDegrees(Math.atan2(-toTarget.x, toTarget.z));
            float desiredPitch = (float) -Math.toDegrees(Math.atan2(toTarget.y, horizontal));

            float yawError = Mth.wrapDegrees(desiredYaw - mc.player.getYRot());
            float pitchError = desiredPitch - mc.player.getXRot();

            double falloff = Math.exp(-Math.abs(yawError) * 0.01);
            double falloffP = Math.exp(-Math.abs(pitchError) * 0.01);

            setDX.accept(accumulatedDX + yawError * (0.15 + falloff));
            setDY.accept(accumulatedDY + pitchError * (0.15 + falloffP));
        }
    }
}
