package me.numenmc.pedrohack.util;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class VecUtils {
    public static void copy(Vector3d original, Vec3 minecraft) {
        original.x = minecraft.x;
        original.y = minecraft.y;
        original.z = minecraft.z;
    }
}
