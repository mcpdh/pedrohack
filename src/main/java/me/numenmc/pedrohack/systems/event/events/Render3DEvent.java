package me.numenmc.pedrohack.systems.event.events;

import me.numenmc.pedrohack.render.world.MeshBuilder;
import me.numenmc.pedrohack.systems.event.Event;
import org.joml.Matrix4fc;

public class Render3DEvent extends Event {
    public final MeshBuilder lines;
    public final MeshBuilder linesDepth;
    public final MeshBuilder tris;
    public final MeshBuilder trisDepth;
    public final Matrix4fc modelViewMatrix;
    public final float partialTick;

    public Render3DEvent(MeshBuilder lines, MeshBuilder linesDepth,
                         MeshBuilder tris, MeshBuilder trisDepth,
                         Matrix4fc modelViewMatrix, float partialTick) {
        this.lines = lines;
        this.linesDepth = linesDepth;
        this.tris = tris;
        this.trisDepth = trisDepth;
        this.modelViewMatrix = modelViewMatrix;
        this.partialTick = partialTick;
    }
}
