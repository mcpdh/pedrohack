package me.numenmc.pedrohack.systems.modules.player;

import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.TickEvent;
import me.numenmc.pedrohack.systems.settings.IntSetting;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

public class Freecam extends Module {
    SettingCategory mainCategory = SettingCategory.createDefault();

    IntSetting moveSpeed = new IntSetting.Builder()
            .name("speed")
            .description("The speed at which the camera moves at")
            .defaultValue(5)
            .min(1)
            .max(50)
            .build();

    private double x, y, z, prevX, prevY, prevZ;
    private float yaw, pitch, lastYaw, lastPitch;

    private CameraType rememberedCameraType;
    private double rememberedFovEffectScale;
    private boolean rememberedBobView;

    public Freecam() {
        super("freecam", "Detaches your camera from the player.");

        mainCategory.add(moveSpeed);
        addSettingCategory(mainCategory);
    }

    @Override
    protected void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            setEnabled(false);
            return;
        }

        x = prevX = mc.player.getX();
        y = prevY = mc.player.getEyeY();
        z = prevZ = mc.player.getZ();
        yaw = lastYaw = mc.player.getYRot();
        pitch = lastPitch = mc.player.getXRot();

        rememberedCameraType = mc.options.getCameraType();

        rememberedFovEffectScale = mc.options.fovEffectScale().get();
        rememberedBobView = mc.options.bobView().get();
        mc.options.fovEffectScale().set(0.0);
        mc.options.bobView().set(false);

        mc.smartCull = false;
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();

        mc.smartCull = true;
        if (rememberedCameraType != null) {
            mc.options.setCameraType(rememberedCameraType);
        }

        mc.options.fovEffectScale().set(rememberedFovEffectScale);
        mc.options.bobView().set(rememberedBobView);

        if (mc.player != null) {
            mc.player.input = new KeyboardInput(mc.options);
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!mc.options.getCameraType().isFirstPerson()) {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }

        ClientInput frozen = new ClientInput();
        frozen.keyPresses = new Input(false, false, false, false, false, false, false);
        mc.player.input = frozen;

        boolean forward = mc.options.keyUp.isDown();
        boolean backward = mc.options.keyDown.isDown();
        boolean right = mc.options.keyRight.isDown();
        boolean left = mc.options.keyLeft.isDown();
        boolean up = mc.options.keyJump.isDown();
        boolean down = mc.options.keyShift.isDown();

        Vec3 fwd = Vec3.directionFromRotation(0, yaw);
        Vec3 rgt = Vec3.directionFromRotation(0, yaw + 90);
        double speed = ((double) moveSpeed.get() / 10) * (mc.options.keySprint.isDown() ? 3.0 : 1.0);

        double dx = 0, dz = 0;
        if (forward) { dx += fwd.x; dz += fwd.z; }
        if (backward) { dx -= fwd.x; dz -= fwd.z; }
        if (right) { dx += rgt.x; dz += rgt.z; }
        if (left) { dx -= rgt.x; dz -= rgt.z; }

        Vec3 horizontal = new Vec3(dx, 0, dz);
        if (horizontal.lengthSqr() > 0) {
            horizontal = horizontal.normalize().scale(speed);
        }

        double dy = 0;
        if (up) dy += speed;
        if (down) dy -= speed;

        prevX = x;
        prevY = y;
        prevZ = z;
        x += horizontal.x;
        y += dy;
        z += horizontal.z;
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        lastYaw = yaw;
        lastPitch = pitch;

        yaw += (float) deltaX;
        pitch = Mth.clamp(pitch + (float) deltaY, -90, 90);
    }

    public double getX(float partialTick) {
        return Mth.lerp(partialTick, prevX, x);
    }

    public double getY(float partialTick) {
        return Mth.lerp(partialTick, prevY, y);
    }

    public double getZ(float partialTick) {
        return Mth.lerp(partialTick, prevZ, z);
    }

    public double getYaw(float partialTick) {
        return Mth.lerp(partialTick, lastYaw, yaw);
    }

    public double getPitch(float partialTick) {
        return Mth.lerp(partialTick, lastPitch, pitch);
    }
}
