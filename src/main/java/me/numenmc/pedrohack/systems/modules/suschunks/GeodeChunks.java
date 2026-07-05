package me.numenmc.pedrohack.systems.modules.suschunks;

import me.numenmc.pedrohack.render.RenderUtil;
import me.numenmc.pedrohack.render.world.Color;
import me.numenmc.pedrohack.render.world.Render3D;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.ChunkLoadEvent;
import me.numenmc.pedrohack.systems.event.events.ChunkUnloadEvent;
import me.numenmc.pedrohack.systems.event.events.Render3DEvent;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import me.numenmc.pedrohack.systems.settings.ColorSetting;
import me.numenmc.pedrohack.systems.settings.EnumSetting;
import me.numenmc.pedrohack.systems.settings.IntSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GeodeChunks extends Module {

    // Chunks confirmed to contain a geode — used as input for the cluster pass
    private final Map<ChunkPos, BlockPos> geodeChunks = new HashMap<>(); // chunk -> approximate geode centre
    // Chunks that passed the density rule and should be rendered
    private final Set<ChunkPos> flaggedChunks = new HashSet<>();

    // ── Settings ──────────────────────────────────────────────────────────────

    SettingCategory mainCategory = SettingCategory.createDefault();

    IntSetting minGeodeBlocks = new IntSetting.Builder()
            .name("min-geode-blocks")
            .description("Minimum adjacent amethyst and shell instances to confirm a geode in a chunk")
            .defaultValue(6)
            .min(1)
            .max(64)
            .onChange(v -> rebuild())
            .build();

    IntSetting minGeodes = new IntSetting.Builder()
            .name("min-geodes")
            .description("Minimum geodes within the range to flag a chunk")
            .defaultValue(5)
            .min(1)
            .max(20)
            .onChange(v -> rerunDensityPass())
            .build();

    IntSetting chunkRange = new IntSetting.Builder()
            .name("chunk-range")
            .description("Radius in chunks to search for nearby geodes")
            .defaultValue(7)
            .min(1)
            .max(32)
            .onChange(v -> rerunDensityPass())
            .build();

    SettingCategory renderCategory = new SettingCategory("Render");

    public enum GeodeRenderMode { Filled, Outlined }

    ColorSetting renderColor = new ColorSetting.Builder()
            .name("box-color")
            .description("Color of the chunk highlight")
            .defaultValue(new Color(180, 100, 255, 100))
            .build();

    EnumSetting<GeodeRenderMode> renderMode = new EnumSetting.Builder<>(GeodeRenderMode.class)
            .name("render-mode")
            .description("How flagged chunks are rendered")
            .defaultValue(GeodeRenderMode.Filled)
            .build();

    IntSetting renderHeight = new IntSetting.Builder()
            .name("render-height")
            .description("Y level the chunk box renders at")
            .defaultValue(63)
            .min(-64)
            .max(319)
            .build();

    BoolSetting traceChunks = new BoolSetting.Builder()
            .name("trace-chunks")
            .description("Draw tracer lines to flagged chunks")
            .defaultValue(true)
            .build();

    // ── Constructor ───────────────────────────────────────────────────────────

    public GeodeChunks() {
        super("geode-chunks", "Highlight chunks where multiple geodes are close together");

        mainCategory.add(minGeodeBlocks);
        mainCategory.add(minGeodes);
        mainCategory.add(chunkRange);

        renderCategory.add(renderColor);
        renderCategory.add(renderMode);
        renderCategory.add(renderHeight);
        renderCategory.add(traceChunks);

        addSettingCategory(mainCategory);
        addSettingCategory(renderCategory);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        rebuild();
    }

    @Override
    public void onDisable() {
        geodeChunks.clear();
        flaggedChunks.clear();
    }

    // ── Chunk events ──────────────────────────────────────────────────────────

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        BlockPos centre = findGeodeCentre(event.chunk);
        if (centre != null) {
            geodeChunks.put(event.chunk.getPos(), centre);
        }
        rerunDensityPass();
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        geodeChunks.remove(event.chunk.getPos());
        flaggedChunks.remove(event.chunk.getPos());
        rerunDensityPass();
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        int y = renderHeight.get();

        for (ChunkPos chunk : flaggedChunks) {
            double x1 = chunk.getMinBlockX();
            double z1 = chunk.getMinBlockZ();
            double x2 = x1 + 16;
            double z2 = z1 + 16;

            if (renderMode.get() == GeodeRenderMode.Filled) {
                Render3D.drawBoxFilled(event.tris, x1, y, z1, x2, y, z2, renderColor.get());
            } else {
                Render3D.drawBoxLines(event.lines, x1, y, z1, x2, y, z2, renderColor.get());
            }

            if (traceChunks.get()) {
                Render3D.drawLine3D(event.lines,
                        RenderUtil.center.x, RenderUtil.center.y, RenderUtil.center.z,
                        x1 + 8, y, z1 + 8, renderColor.get());
            }
        }
    }

    // ── Core logic ────────────────────────────────────────────────────────────

    private void rebuild() {
        geodeChunks.clear();
        flaggedChunks.clear();

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        int view    = mc.options.renderDistance().get();
        int centerX = mc.player.getBlockX() >> 4;
        int centerZ = mc.player.getBlockZ() >> 4;

        for (int cx = centerX - view; cx <= centerX + view; cx++) {
            for (int cz = centerZ - view; cz <= centerZ + view; cz++) {
                LevelChunk chunk = mc.level.getChunkSource().getChunk(cx, cz, false);
                if (chunk == null) continue;
                BlockPos centre = findGeodeCentre(chunk);
                if (centre != null) {
                    geodeChunks.put(chunk.getPos(), centre);
                }
            }
        }

        rerunDensityPass();
    }

    /**
     * For each chunk that contains a geode, count how many other geode-chunks
     * have their centre within chunkRange chunks (XZ only). If the count including
     * itself reaches minGeodes, flag the chunk.
     */
    private void rerunDensityPass() {
        flaggedChunks.clear();

        if (geodeChunks.size() < minGeodes.get()) return;

        double rangeBlocks = chunkRange.get() * 16.0;
        double radiusSq = rangeBlocks * rangeBlocks;

        for (Map.Entry<ChunkPos, BlockPos> entry : geodeChunks.entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            BlockPos centre   = entry.getValue();

            int nearbyCount = 0;
            for (BlockPos other : geodeChunks.values()) {
                double dx = centre.getX() - other.getX();
                double dz = centre.getZ() - other.getZ();
                if (dx * dx + dz * dz <= radiusSq) {
                    nearbyCount++;
                }
            }

            // nearbyCount includes self, so threshold is minGeodes
            if (nearbyCount >= minGeodes.get()) {
                flaggedChunks.add(chunkPos);
            }
        }
    }

    /**
     * Scans the chunk for amethyst blocks adjacent to calcite or smooth basalt.
     * Returns the average XZ position of all qualifying amethyst blocks as the
     * geode's centre, or null if no geode is found.
     */
    private BlockPos findGeodeCentre(LevelChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int adjacencyCount = 0;
        int threshold = minGeodeBlocks.get();
        long sumX = 0, sumY = 0, sumZ = 0;

        for (int sectionIndex = 0; sectionIndex < chunk.getSectionsCount(); sectionIndex++) {
            LevelChunkSection section = chunk.getSection(sectionIndex);
            if (section.hasOnlyAir()) continue;

            boolean hasAmethyst = section.maybeHas(s -> s.is(Blocks.AMETHYST_BLOCK));
            boolean hasCalcite  = section.maybeHas(s -> s.is(Blocks.CALCITE));
            boolean hasBasalt   = section.maybeHas(s -> s.is(Blocks.SMOOTH_BASALT));
            if (!hasAmethyst && !hasCalcite && !hasBasalt) continue;

            int minY = chunk.getSectionYFromSectionIndex(sectionIndex) << 4;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (!section.getBlockState(x, y, z).is(Blocks.AMETHYST_BLOCK)) continue;

                        int worldX = chunkPos.getMinBlockX() + x;
                        int worldY = minY + y;
                        int worldZ = chunkPos.getMinBlockZ() + z;

                        if (hasGeodeNeighbour(chunk, worldX, worldY, worldZ)) {
                            adjacencyCount++;
                            sumX += worldX;
                            sumY += worldY;
                            sumZ += worldZ;
                        }
                    }
                }
            }
        }

        if (adjacencyCount < threshold) return null;

        // Return the centroid of all qualifying amethyst blocks
        return new BlockPos(
                (int) (sumX / adjacencyCount),
                (int) (sumY / adjacencyCount),
                (int) (sumZ / adjacencyCount)
        );
    }

    private boolean hasGeodeNeighbour(LevelChunk chunk, int worldX, int worldY, int worldZ) {
        for (Direction dir : Direction.values()) {
            int nx = worldX + dir.getStepX();
            int ny = worldY + dir.getStepY();
            int nz = worldZ + dir.getStepZ();

            int localX = nx - chunk.getPos().getMinBlockX();
            int localZ = nz - chunk.getPos().getMinBlockZ();
            if (localX < 0 || localX > 15 || localZ < 0 || localZ > 15) continue;

            BlockState neighbour = chunk.getBlockState(new BlockPos(nx, ny, nz));
            if (neighbour.is(Blocks.CALCITE) || neighbour.is(Blocks.SMOOTH_BASALT)) {
                return true;
            }
        }
        return false;
    }
}
