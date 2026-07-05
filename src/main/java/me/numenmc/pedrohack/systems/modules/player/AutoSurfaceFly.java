package me.numenmc.pedrohack.systems.modules.player;

import me.numenmc.pedrohack.mixin.KeyMappingAccessor;
import me.numenmc.pedrohack.render.notification.NotificationType;
import me.numenmc.pedrohack.render.notification.Notifications;
import me.numenmc.pedrohack.render.world.Color;
import me.numenmc.pedrohack.render.world.Render3D;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.Render3DEvent;
import me.numenmc.pedrohack.systems.event.events.TickEvent;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import me.numenmc.pedrohack.systems.settings.ColorSetting;
import me.numenmc.pedrohack.systems.settings.IntSetting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class AutoSurfaceFly extends Module {

    SettingCategory mainCategory = SettingCategory.createDefault();
    SettingCategory renderCategory = new SettingCategory("Render");

    // -------------------------
    // Flight settings
    // -------------------------

    IntSetting targetHoverHeight =
            new IntSetting.Builder()
                    .name("target-hover-height")
                    .description("Desired height above terrain.")
                    .defaultValue(40)
                    .min(5)
                    .max(128)
                    .build();

    IntSetting surfaceTolerance =
            new IntSetting.Builder()
                    .name("surface-tolerance")
                    .description("Allowed deviation before correction engages.")
                    .defaultValue(5)
                    .min(0)
                    .max(50)
                    .build();

    IntSetting lookAhead =
            new IntSetting.Builder()
                    .name("look-ahead")
                    .description("Distance to scan terrain ahead.")
                    .defaultValue(80)
                    .min(10)
                    .max(256)
                    .build();

    IntSetting emergencyHeight =
            new IntSetting.Builder()
                    .name("emergency-height")
                    .description("Emergency pull-up threshold.")
                    .defaultValue(12)
                    .min(3)
                    .max(50)
                    .build();

    IntSetting rocketSpeed =
            new IntSetting.Builder()
                    .name("rocket-speed")
                    .description("Ticks between rocket boosts.")
                    .defaultValue(50)
                    .min(0)
                    .max(200)
                    .build();

    BoolSetting smooth =
            new BoolSetting.Builder()
                    .name("smooth")
                    .description("Enable smoothing for pitch control.")
                    .defaultValue(true)
                    .build();

    // -------------------------
    // Render settings
    // -------------------------

    BoolSetting renderLookaheadDistance =
            new BoolSetting.Builder()
                    .name("render-lookahead-distance")
                    .description("Render lookahead marker.")
                    .defaultValue(false)
                    .build();

    ColorSetting renderLookaheadDistanceColor =
            new ColorSetting.Builder()
                    .name("lookahead-color")
                    .description("Lookahead marker color.")
                    .defaultValue(new Color(0, 0, 255))
                    .build();

    BoolSetting renderSurfaceHeight =
            new BoolSetting.Builder()
                    .name("render-surface-height")
                    .description("Render surface height marker.")
                    .defaultValue(true)
                    .build();

    IntSetting renderSurfaceHeightDistance =
            new IntSetting.Builder()
                    .name("surface-render-distance")
                    .description("Distance for surface rendering.")
                    .defaultValue(25)
                    .min(1)
                    .max(128)
                    .build();

    ColorSetting renderSurfaceHeightColor =
            new ColorSetting.Builder()
                    .name("surface-color")
                    .description("Surface marker color.")
                    .defaultValue(new Color(0, 255, 0))
                    .build();

    // -------------------------
    // State
    // -------------------------

    float rawPitch = 0;
    float smoothedPitch = 0;

    int rocketCooldown = 0;

    public AutoSurfaceFly() {
        super("auto-surface-fly", "Stable surface-based Elytra flight");

        mainCategory.add(targetHoverHeight);
        mainCategory.add(surfaceTolerance);
        mainCategory.add(lookAhead);
        mainCategory.add(emergencyHeight);
        mainCategory.add(rocketSpeed);
        mainCategory.add(smooth);

        renderCategory.add(renderLookaheadDistance);
        renderCategory.add(renderLookaheadDistanceColor);
        renderCategory.add(renderSurfaceHeight);
        renderCategory.add(renderSurfaceHeightDistance);
        renderCategory.add(renderSurfaceHeightColor);

        addSettingCategory(mainCategory);
        addSettingCategory(renderCategory);
    }

    // =========================================================
    // TICK
    // =========================================================

    @EventHandler
    public void onTick(TickEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) return;

        if (mc.level.dimension() != Level.OVERWORLD) {
            Notifications.pushNotification(NotificationType.ERROR,
                    getDisplayName() + " disabled outside overworld");
            setEnabled(false);
            return;
        }

        if (!mc.player.isFallFlying()) {
            Notifications.pushNotification(NotificationType.ERROR,
                    getDisplayName() + " requires Elytra");
            setEnabled(false);
            return;
        }

        if (rocketCooldown > 0) rocketCooldown--;

        double yaw = Math.toRadians(mc.player.getYRot());
        double dirX = -Math.sin(yaw);
        double dirZ = Math.cos(yaw);

        double maxSurface = 0;

        for (int d = 20; d <= lookAhead.get(); d += 10) {
            double x = mc.player.getX() + dirX * d;
            double z = mc.player.getZ() + dirZ * d;

            BlockPos pos = mc.level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                    new BlockPos((int) x, 0, (int) z)
            );

            maxSurface = Math.max(maxSurface, pos.getY());
        }

        double targetY = maxSurface + targetHoverHeight.get();
        double error = targetY - mc.player.getY();
        double tolerance = surfaceTolerance.get();

        // -------------------------
        // RAW pitch target
        // -------------------------

        if (Math.abs(error) <= tolerance) {
            rawPitch = -3;
        } else {
            rawPitch = (float) Mth.clamp(
                    -(error - Math.signum(error) * tolerance) * 0.14,
                    -35,
                    20
            );
        }

        if (mc.player.getY() < maxSurface + emergencyHeight.get()) {
            rawPitch = -40;
        }

        // -------------------------
        // SMOOTHING LAYER (FIX)
        // -------------------------

        float alpha = smooth.get() ? 0.12f : 1.0f;

        smoothedPitch += (rawPitch - smoothedPitch) * alpha;

        // clamp final output
        smoothedPitch = Mth.clamp(smoothedPitch, -45, 25);

        // -------------------------
        // ROCKET LOGIC (unchanged)
        // -------------------------

        if (rocketSpeed.get() > 0 && rocketCooldown <= 0) {
            useRocket();
            rocketCooldown = rocketSpeed.get();
        }
    }

    // =========================================================
    // RENDER CONTROL
    // =========================================================

    @EventHandler
    public void on3DRender(Render3DEvent event) {
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        double yaw = Math.toRadians(mc.player.getYRot());
        double dirX = -Math.sin(yaw);
        double dirZ = Math.cos(yaw);

        double startX = mc.player.getX();
        double startY = mc.player.getY();
        double startZ = mc.player.getZ();

        // Lookahead markers
        if (renderLookaheadDistance.get()) {

            int d = lookAhead.get();

            double x = startX + dirX * d;
            double z = startZ + dirZ * d;

            Color color = renderLookaheadDistanceColor.get();
            Color fillColor = new Color(color.r, color.g, color.b, 40);

            double size = 0.6;

            double x1 = x - size;
            double y1 = startY - size;
            double z1 = z - size;

            double x2 = x + size;
            double y2 = startY + size;
            double z2 = z + size;

            Render3D.drawBoxLines(
                    event.lines,
                    x1, y1, z1,
                    x2, y2, z2,
                    color
            );

            Render3D.drawBoxFilled(
                    event.tris,
                    x1, y1, z1,
                    x2, y2, z2,
                    fillColor
            );
        }

        // Surface markers
        if (renderSurfaceHeight.get()) {

            int d = renderSurfaceHeightDistance.get();

            double x = startX + dirX * d;
            double z = startZ + dirZ * d;

            BlockPos pos = mc.level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                    new BlockPos((int) x, 0, (int) z)
            );

            double y = pos.getY() + targetHoverHeight.get();

            Color color = renderSurfaceHeightColor.get();
            Color fillColor = new Color(color.r, color.g, color.b, 40);

            double size = 0.4;

            double x1 = x - size;
            double y1 = y - surfaceTolerance.get();
            double z1 = z - size;

            double x2 = x + size;
            double y2 = y + surfaceTolerance.get();
            double z2 = z + size;

            Render3D.drawBoxLines(
                    event.lines,
                    x1, y1, z1,
                    x2, y2, z2,
                    color
            );

            Render3D.drawBoxFilled(
                    event.tris,
                    x1, y1, z1,
                    x2, y2, z2,
                    fillColor
            );
        }
    }


    public void onUpdateMouse(
            double dx,
            double dy,
            Consumer<Double> setDX,
            Consumer<Double> setDY
    ) {
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float currentPitch = mc.player.getXRot();
        float pitchError = smoothedPitch - currentPitch;

        double falloff = Math.exp(-Math.abs(pitchError) * 0.04);

        double correction = pitchError * (
                smooth.get() ? 0.10 + falloff : 1.0
        );

        setDY.accept(dy + correction);
    }

    // =========================================================
    // ROCKETS
    // =========================================================

    private void useRocket() {
        Minecraft mc = Minecraft.getInstance();

        Runnable use = () ->
                KeyMapping.click(((KeyMappingAccessor) mc.options.keyUse).getKey());

        if (mc.player == null) return;

        int slot = mc.player.getInventory().getSelectedSlot();
        ItemStack current = mc.player.getInventory().getItem(slot);

        if (current.getItem() instanceof FireworkRocketItem) {
            use.run();
            return;
        }

        int rocketSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);

            if (!stack.isEmpty() && stack.getItem() instanceof FireworkRocketItem) {
                rocketSlot = i;
                break;
            }
        }

        if (rocketSlot == -1) {
            Notifications.pushNotification(NotificationType.ERROR, "No fireworks in hotbar!");
            return;
        }

        mc.player.getInventory().setSelectedSlot(rocketSlot);

        mc.execute(() -> {
            if (mc.player != null && mc.player.isFallFlying()) {
                use.run();
            }
        });
    }
}
