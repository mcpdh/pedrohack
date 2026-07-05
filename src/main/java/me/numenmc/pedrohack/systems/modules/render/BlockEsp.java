package me.numenmc.pedrohack.systems.modules.render;

import me.numenmc.pedrohack.render.RenderUtil;
import me.numenmc.pedrohack.render.world.Color;
import me.numenmc.pedrohack.render.world.Render3D;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.ChunkLoadEvent;
import me.numenmc.pedrohack.systems.event.events.ChunkUnloadEvent;
import me.numenmc.pedrohack.systems.event.events.PacketReceiveEvent;
import me.numenmc.pedrohack.systems.event.events.Render3DEvent;
import me.numenmc.pedrohack.systems.settings.BlockTypesSetting;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import me.numenmc.pedrohack.systems.settings.ColorSetting;
import me.numenmc.pedrohack.systems.settings.IntSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockEsp extends Module {
    private final Map<Long, Set<BlockPos>> foundBlocks =
            Collections.synchronizedMap(new HashMap<>());

    private final ExecutorService scanWorker =
            Executors.newSingleThreadExecutor();

    private final Color fill =
            new Color(100, 100, 255, 40);

    SettingCategory mainCategory =
            SettingCategory.createDefault();

    BlockTypesSetting targets =
            new BlockTypesSetting.Builder()
                    .name("targets")
                    .description("The blocks to look for")
                    .defaultValue(Blocks.NETHER_PORTAL)
                    .onChange(v -> {
                        if (!isEnabled()) return;

                        synchronized (foundBlocks) {
                            foundBlocks.clear();
                        }

                        scanLoadedChunks();
                    })
                    .build();

    SettingCategory renderCategory =
            new SettingCategory("Render");

    ColorSetting color =
            new ColorSetting.Builder()
                    .name("color")
                    .description("The color to render")
                    .defaultValue(new Color(100, 100, 255))
                    .build();

    BoolSetting lines =
            new BoolSetting.Builder()
                    .name("render-lines")
                    .defaultValue(true)
                    .build();

    BoolSetting trace =
            new BoolSetting.Builder()
                    .name("trace")
                    .defaultValue(false)
                    .build();

    IntSetting renderMaxBlocks = new IntSetting.Builder()
            .name("render-max-blocks")
            .description("Maximum number of blocks rendered at once.")
            .defaultValue(50000)
            .min(100)
            .max(500000)
            .build();

    IntSetting chunkMaxBlocks = new IntSetting.Builder()
            .name("chunk-max-blocks")
            .description("Maximum matching blocks stored per chunk.")
            .defaultValue(500)
            .min(10)
            .max(10000)
            .build();

    public BlockEsp() {
        super("block-esp", "Highlight specific blocks");

        mainCategory.add(targets);
        mainCategory.add(chunkMaxBlocks);

        renderCategory.add(color);
        renderCategory.add(lines);
        renderCategory.add(trace);

        renderCategory.add(renderMaxBlocks);

        addSettingCategory(mainCategory);
        addSettingCategory(renderCategory);
    }

    @Override
    public void onEnable() {
        synchronized (foundBlocks) {
            foundBlocks.clear();
        }

        scanLoadedChunks();
    }

    @Override
    public void onDisable() {
        synchronized (foundBlocks) {
            foundBlocks.clear();
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!isEnabled()) return;

        submitScan(event.chunk);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        foundBlocks.remove(
                chunkKey(
                        event.chunk.getPos().x(),
                        event.chunk.getPos().z()
                )
        );
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled()) return;

        Color outline = color.get();

        int rendered = 0;
        int maxRendered = renderMaxBlocks.get();

        fill.r = outline.r;
        fill.g = outline.g;
        fill.b = outline.b;
        fill.a = 40;

        synchronized (foundBlocks) {
            for (Set<BlockPos> positions : foundBlocks.values()) {
                if (rendered >= maxRendered) {
                    return;
                }

                rendered++;

                for (BlockPos pos : positions) {

                    double x1 = pos.getX();
                    double y1 = pos.getY();
                    double z1 = pos.getZ();

                    double x2 = x1 + 1;
                    double y2 = y1 + 1;
                    double z2 = z1 + 1;

                    if (lines.get()) {
                        Render3D.drawBoxLines(
                                event.lines,
                                x1, y1, z1,
                                x2, y2, z2,
                                outline
                        );
                    }

                    Render3D.drawBoxFilled(
                            event.tris,
                            x1, y1, z1,
                            x2, y2, z2,
                            fill
                    );

                    if (trace.get()) {
                        Render3D.drawLine3D(
                                event.lines,

                                RenderUtil.center.x,
                                RenderUtil.center.y,
                                RenderUtil.center.z,

                                x1 + 0.5,
                                y1 + 0.5,
                                z1 + 0.5,

                                outline
                        );
                    }
                }
            }
        }
    }

    private void scanLoadedChunks() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) {
            return;
        }

        ClientChunkCache cache =
                mc.level.getChunkSource();

        ChunkPos center =
                mc.player.chunkPosition();

        int radius =
                mc.options.renderDistance().get();

        for (int cx = center.x() - radius;
             cx <= center.x() + radius;
             cx++) {

            for (int cz = center.z() - radius;
                 cz <= center.z() + radius;
                 cz++) {

                LevelChunk chunk =
                        cache.getChunk(
                                cx,
                                cz,
                                ChunkStatus.FULL,
                                false
                        );

                if (chunk != null) {
                    submitScan(chunk);
                }
            }
        }
    }

    private void submitScan(LevelChunk chunk) {
        scanWorker.submit(() -> {
            if (!isEnabled()) {
                return;
            }

            scanChunk(chunk);
        });
    }

    @EventHandler
    public void onPacket(PacketReceiveEvent event) {
        if (!isEnabled()) return;

        if (event.packet instanceof ClientboundBlockUpdatePacket packet) {
            onSingleBlockUpdate(
                    packet.getPos(),
                    packet.getBlockState().getBlock()
            );
        }

        if (event.packet instanceof ClientboundSectionBlocksUpdatePacket packet) {
            packet.runUpdates((pos, state) -> {
                onSingleBlockUpdate(
                        pos,
                        state.getBlock()
                );
            });
        }
    }

    private void onSingleBlockUpdate(
            BlockPos pos,
            Block block
    ) {
        long key =
                chunkKey(
                        pos.getX() >> 4,
                        pos.getZ() >> 4
                );

        synchronized (foundBlocks) {

            Set<BlockPos> set =
                    foundBlocks.computeIfAbsent(
                            key,
                            k -> new HashSet<>()
                    );

            boolean target =
                    targets.get()
                            .contains(block);

            if (target) {

                if (set.size() <
                        chunkMaxBlocks.get()) {

                    set.add(
                            pos.immutable()
                    );
                }

            } else {
                set.remove(pos);

                if (set.isEmpty()) {
                    foundBlocks.remove(key);
                }
            }
        }
    }

    private void scanChunk(LevelChunk chunk) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) {
            return;
        }

        Set<BlockPos> found =
                new HashSet<>(
                        Math.min(
                                chunkMaxBlocks.get(),
                                1024
                        )
                );

        Set<Block> targetBlocks =
                new HashSet<>(targets.get());

        BlockPos.MutableBlockPos pos =
                new BlockPos.MutableBlockPos();

        int minX =
                chunk.getPos().getMinBlockX();

        int maxX =
                chunk.getPos().getMaxBlockX();

        int minZ =
                chunk.getPos().getMinBlockZ();

        int maxZ =
                chunk.getPos().getMaxBlockZ();

        int minY =
                mc.level.getMinY();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {

                int top =
                        chunk.getHeight(
                                Heightmap.Types.WORLD_SURFACE,
                                x & 15,
                                z & 15
                        );

                for (int y = minY; y < top; y++) {

                    pos.set(x, y, z);

                    if (targetBlocks.contains(chunk.getBlockState(pos).getBlock())) {
                        found.add(pos.immutable());

                        if (found.size() >= chunkMaxBlocks.get()) {
                            foundBlocks.put(
                                    chunkKey(
                                            chunk.getPos().x(),
                                            chunk.getPos().z()
                                    ),
                                    found
                            );

                            return;
                        }
                    }
                }
            }
        }

        foundBlocks.put(
                chunkKey(
                        chunk.getPos().x(),
                        chunk.getPos().z()
                ),
                found
        );
    }

    private long chunkKey(
            int x,
            int z
    ) {
        return ((long) x << 32)
                | (z & 0xFFFFFFFFL);
    }
}
