package me.numenmc.pedrohack.render.world;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.*;

public class MeshBuilder {
    private final VertexFormat format;
    private final int primitiveVerticesSize;
    private final int primitiveIndicesCount;

    private ByteBuffer vertices = null;
    private long verticesPointerStart, verticesPointer;
    private ByteBuffer indices = null;
    private long indicesPointer;
    private int vertexI, indicesCount;
    private boolean building;
    private double cameraX, cameraZ;

    public MeshBuilder(RenderPipeline pipeline) {
        // 26.2: RenderPipeline now supports up to 16 vertex buffer bindings, so a single
        // VertexFormat is no longer exposed directly - fetch the format bound at index 0
        // (the only buffer this mod's pipelines bind), and read the topology separately.
        this.format = pipeline.getVertexFormatBinding(0);
        this.primitiveVerticesSize = format.getVertexSize();
        this.primitiveIndicesCount = pipeline.getPrimitiveTopology().primitiveLength;
    }

    public void begin() {
        if (building) throw new IllegalStateException("begin() called while already building");

        verticesPointer = verticesPointerStart;
        vertexI = 0;
        indicesCount = 0;
        building = true;

        Vec3 cam = Minecraft.getInstance().gameRenderer.mainCamera().position();
        cameraX = cam.x;
        cameraZ = cam.z;
    }

    public MeshBuilder vec3(double x, double y, double z) {
        long p = verticesPointer;
        memPutFloat(p,     (float)(x - cameraX));
        memPutFloat(p + 4, (float) y);
        memPutFloat(p + 8, (float)(z - cameraZ));
        verticesPointer += 12;
        return this;
    }

    public void quad(int i1, int i2, int i3, int i4) {
        long p = indicesPointer + indicesCount * 4L;
        // two triangles from a quad
        memPutInt(p,      i1);
        memPutInt(p + 4,  i2);
        memPutInt(p + 8,  i3);
        memPutInt(p + 12, i1);
        memPutInt(p + 16, i3);
        memPutInt(p + 20, i4);
        indicesCount += 6;
    }

    public void ensureQuadCapacity() {
        ensureCapacity(4, 6);
    }

    public MeshBuilder color(Color c) {
        long p = verticesPointer;
        memPutByte(p,     (byte) c.r);
        memPutByte(p + 1, (byte) c.g);
        memPutByte(p + 2, (byte) c.b);
        memPutByte(p + 3, (byte) c.a);
        verticesPointer += 4;
        return this;
    }

    public int next() {
        return vertexI++;
    }

    public void line(int i1, int i2) {
        long p = indicesPointer + indicesCount * 4L;
        memPutInt(p,     i1);
        memPutInt(p + 4, i2);
        indicesCount += 2;
    }

    public void ensureLineCapacity() {
        ensureCapacity(2, 2);
    }

    public void ensureCapacity(int vertexCount, int indexCount) {
        if (vertices == null || indices == null) {
            allocateBuffers(256, 512);
            return;
        }

        if ((vertexI + vertexCount) * primitiveVerticesSize >= vertices.capacity()) {
            int offset = (int)(verticesPointer - verticesPointerStart);
            int newSize = Math.max(vertices.capacity() * 2, vertices.capacity() + vertexCount * primitiveVerticesSize);
            ByteBuffer newVerts = BufferUtils.createByteBuffer(newSize);
            memCopy(memAddress0(vertices), memAddress0(newVerts), offset);
            vertices = newVerts;
            verticesPointerStart = memAddress0(vertices);
            verticesPointer = verticesPointerStart + offset;
        }

        if ((indicesCount + indexCount) * Integer.BYTES >= indices.capacity()) {
            int newSize = Math.max(indices.capacity() * 2, indices.capacity() + indexCount * Integer.BYTES);
            ByteBuffer newIdx = BufferUtils.createByteBuffer(newSize);
            memCopy(memAddress0(indices), memAddress0(newIdx), indicesCount * 4L);
            indices = newIdx;
            indicesPointer = memAddress0(indices);
        }
    }

    private void allocateBuffers(int vertexCount, int indexCount) {
        vertices = BufferUtils.createByteBuffer(primitiveVerticesSize * vertexCount);
        verticesPointerStart = verticesPointer = memAddress0(vertices);
        indices = BufferUtils.createByteBuffer(indexCount * Integer.BYTES);
        indicesPointer = memAddress0(indices);
    }

    public void end() {
        if (!building) throw new IllegalStateException("end() called while not building");
        building = false;
    }

    public boolean isBuilding() { return building; }

    public GpuBuffer getVertexBuffer() {
        vertices.limit((int)(verticesPointer - verticesPointerStart));
        // 26.2: VertexFormat#uploadImmediateVertexBuffer was removed with no documented
        // one-to-one replacement. Allocating the GpuBuffer directly through the device is the
        // lowest-level equivalent used elsewhere in vanilla for immediate-mode style uploads.
        // VERIFY: confirm GpuBuffer.USAGE_VERTEX is still the correct flag name/value in your
        // mappings, since it isn't called out explicitly in the primer.
        return RenderSystem.getDevice().createBuffer(
                () -> "Pedrohack mesh vertices",
                GpuBuffer.USAGE_VERTEX,
                vertices
        );
    }

    public GpuBuffer getIndexBuffer() {
        indices.limit(indicesCount * Integer.BYTES);
        // See getVertexBuffer() note above - same situation for the removed
        // uploadImmediateIndexBuffer helper.
        return RenderSystem.getDevice().createBuffer(
                () -> "Pedrohack mesh indices",
                GpuBuffer.USAGE_INDEX,
                indices
        );
    }

    public int getIndicesCount() { return indicesCount; }

    public int getPrimitiveIndicesCount() {
        return primitiveIndicesCount;
    }
}
