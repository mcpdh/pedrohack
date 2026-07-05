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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.*;

public class ClusterChunks extends Module {

    // Raw cluster positions per chunk — populated on chunk load, never flagged here
    private final Map<ChunkPos, List<BlockPos>> clusterCache = new HashMap<>();
    private final Set<ChunkPos> flaggedChunks = new HashSet<>();

    // ── Settings ──────────────────────────────────────────────────────────────

    SettingCategory mainCategory = SettingCategory.createDefault();

    IntSetting minClusters = new IntSetting.Builder()
            .name("min-clusters")
            .description("Minimum fully grown amethyst clusters for a geode to qualify")
            .defaultValue(10)
            .min(1)
            .max(100)
            .onChange(v -> rerunGeodePass())
            .build();

    IntSetting minGeodes = new IntSetting.Builder()
            .name("min-geodes")
            .description("Minimum qualifying geodes that must be within chunk range of each other")
            .defaultValue(5)
            .min(1)
            .max(20)
            .onChange(v -> rerunGeodePass())
            .build();

    IntSetting chunkRange = new IntSetting.Builder()
            .name("chunk-range")
            .description("Radius in chunks within which qualifying geodes must cluster")
            .defaultValue(8)
            .min(1)
            .max(32)
            .onChange(v -> rerunGeodePass())
            .build();

    SettingCategory renderCategory = new SettingCategory("Render");

    public enum ClusterRenderMode { Filled, Outlined }

    ColorSetting renderColor = new ColorSetting.Builder()
            .name("box-color")
            .description("Color of the chunk highlight")
            .defaultValue(new Color(0, 200, 255, 100))
            .build();

    EnumSetting<ClusterRenderMode> renderMode = new EnumSetting.Builder<>(ClusterRenderMode.class)
            .name("render-mode")
            .description("How flagged chunks are rendered")
            .defaultValue(ClusterRenderMode.Filled)
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
            .description("Should tracer lines be drawn to the chunks?")
            .defaultValue(true)
            .build();

    // ── Constructor ───────────────────────────────────────────────────────────

    public ClusterChunks() {
        super("cluster-chunks", "Highlight chunks containing geodes with fully grown clusters", true);

        mainCategory.add(minClusters);
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
        clusterCache.clear();
        flaggedChunks.clear();
    }

    // ── Chunk events ──────────────────────────────────────────────────────────

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        indexChunk(event.chunk);
        rerunGeodePass();
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        ChunkPos pos = event.chunk.getPos();
        clusterCache.remove(pos);
        flaggedChunks.remove(pos);
        rerunGeodePass();
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

            if (renderMode.get() == ClusterRenderMode.Filled) {
                Render3D.drawBoxFilled(event.tris, x1, y, z1, x2, y, z2, renderColor.get());
            } else {
                Render3D.drawBoxLines(event.lines, x1, y, z1, x2, y, z2, renderColor.get());
            }

            if (traceChunks.get()) {
                Render3D.drawLine3D(event.lines, RenderUtil.center.x, RenderUtil.center.y, RenderUtil.center.z,
                        x1, y, z1, renderColor.get());
            }
        }
    }

    // ── Core logic ────────────────────────────────────────────────────────────

    private void rebuild() {
        clusterCache.clear();
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
                indexChunk(chunk);
            }
        }

        rerunGeodePass();
    }

    /**
     * Scan a chunk for fully grown amethyst clusters and store their positions.
     * No flagging decisions are made here.
     */
    private void indexChunk(LevelChunk chunk) {
        ChunkPos pos = chunk.getPos();
        List<BlockPos> positions = new ArrayList<>();

        for (int sectionIndex = 0; sectionIndex < chunk.getSectionsCount(); sectionIndex++) {
            LevelChunkSection section = chunk.getSection(sectionIndex);
            if (section.hasOnlyAir()) continue;
            if (!section.maybeHas(state -> state.is(Blocks.AMETHYST_CLUSTER))) continue;

            int minY = chunk.getSectionYFromSectionIndex(sectionIndex) << 4;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (!section.getBlockState(x, y, z).is(Blocks.AMETHYST_CLUSTER)) continue;
                        positions.add(new BlockPos(
                                pos.getMinBlockX() + x,
                                minY + y,
                                pos.getMinBlockZ() + z
                        ));
                    }
                }
            }
        }

        if (positions.isEmpty()) {
            clusterCache.remove(pos);
        } else {
            clusterCache.put(pos, positions);
        }
    }

    private void rerunGeodePass() {
        flaggedChunks.clear();

        // Flatten all cluster positions from all loaded chunks
        List<BlockPos> allClusters = new ArrayList<>();
        for (List<BlockPos> positions : clusterCache.values()) {
            allClusters.addAll(positions);
        }

        // Step 1: group into geodes by proximity (20-block radius)
        List<List<BlockPos>> allGeodes = spatialCluster(allClusters, 400); // 20^2

        // Step 2: keep only geodes with enough clusters (rule 1)
        List<List<BlockPos>> qualifyingGeodes = new ArrayList<>();
        for (List<BlockPos> geode : allGeodes) {
            if (geode.size() >= minClusters.get()) {
                qualifyingGeodes.add(geode);
            }
        }

        if (qualifyingGeodes.size() < minGeodes.get()) {
            // Can't possibly satisfy rule 2 — nothing to flag
            return;
        }

        // Compute centroid of each qualifying geode for the range check
        // (XZ only — chunks are vertical columns)
        double[] cx = new double[qualifyingGeodes.size()];
        double[] cz = new double[qualifyingGeodes.size()];
        for (int i = 0; i < qualifyingGeodes.size(); i++) {
            List<BlockPos> geode = qualifyingGeodes.get(i);
            double sumX = 0, sumZ = 0;
            for (BlockPos p : geode) { sumX += p.getX(); sumZ += p.getZ(); }
            cx[i] = sumX / geode.size();
            cz[i] = sumZ / geode.size();
        }

        // Step 3: for each qualifying geode, count peers within chunkRange chunks
        double rangeBlocks = chunkRange.get() * 16.0;
        double radiusSq = rangeBlocks * rangeBlocks;

        for (int i = 0; i < qualifyingGeodes.size(); i++) {
            int nearbyCount = 0;
            for (int j = 0; j < qualifyingGeodes.size(); j++) {
                double dx = cx[i] - cx[j];
                double dz = cz[i] - cz[j];
                if (dx * dx + dz * dz <= radiusSq) {
                    nearbyCount++;
                }
            }

            // nearbyCount includes the geode itself, so threshold is minGeodes
            if (nearbyCount < minGeodes.get()) continue;

            // Step 4: flag every chunk that contains any block of this geode
            for (BlockPos p : qualifyingGeodes.get(i)) {
                ChunkPos cp = ChunkPos.containing(p);
                flaggedChunks.add(cp);
            }
        }
    }

    private List<List<BlockPos>> spatialCluster(List<BlockPos> positions, int distSqThreshold) {
        List<List<BlockPos>> groups = new ArrayList<>();

        for (BlockPos pos : positions) {
            List<BlockPos> matched = null;

            outer:
            for (List<BlockPos> group : groups) {
                for (BlockPos member : group) {
                    if (member.distSqr(pos) <= distSqThreshold) {
                        matched = group;
                        break outer;
                    }
                }
            }

            if (matched != null) {
                matched.add(pos);
            } else {
                List<BlockPos> newGroup = new ArrayList<>();
                newGroup.add(pos);
                groups.add(newGroup);
            }
        }

        return groups;
    }
}
