package me.numenmc.pedrohack.systems.modules.suschunks;

import me.numenmc.pedrohack.render.world.Color;
import me.numenmc.pedrohack.render.world.Render3D;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.ChunkLoadEvent;
import me.numenmc.pedrohack.systems.event.events.ChunkUnloadEvent;
import me.numenmc.pedrohack.systems.event.events.Render3DEvent;
import me.numenmc.pedrohack.systems.settings.ColorSetting;
import me.numenmc.pedrohack.systems.settings.EnumSetting;
import me.numenmc.pedrohack.systems.settings.IntSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.HashSet;
import java.util.Set;

public class KelpChunks extends Module {
    Set<ChunkPos> susChunksCache = new HashSet<>();
    SettingCategory mainCategory = SettingCategory.createDefault();

    public enum KelpChunksRenderMode {
        Filled,
        Outlined
    }

    ColorSetting renderColor = new ColorSetting.Builder()
            .name("box-color")
            .description("The color that the highlight should render")
            .defaultValue(new Color(255, 0, 0, 100))
            .build();

    EnumSetting<KelpChunksRenderMode> renderMode = new EnumSetting.Builder<>(KelpChunksRenderMode.class)
            .name("render-mode")
            .description("How the chunks should be rendered")
            .defaultValue(KelpChunksRenderMode.Filled)
            .build();

    IntSetting renderHeight = new IntSetting.Builder()
            .name("render-height")
            .description("The height that the chunk marking should render at")
            .defaultValue(63)
            .min(-64)
            .max(319)
            .build();

    public KelpChunks() {
        super("kelp-chunks", "Highlight chunks that have fully grown kelp");

        mainCategory.add(renderColor);
        mainCategory.add(renderMode);
        mainCategory.add(renderHeight);

        addSettingCategory(mainCategory);
    }

    private void processChunk(LevelChunk chunk) {
        ChunkPos pos = chunk.getPos();

        susChunksCache.remove(pos);

        if (isKelpChunk(chunk)) {
            susChunksCache.add(pos);
        }
    }

    private void rebuild() {
        susChunksCache.clear();

        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) return;

        int view = mc.options.renderDistance().get();
        int centerX = mc.player.getBlockX() >> 4;
        int centerZ = mc.player.getBlockZ() >> 4;

        for (int cx = centerX - view; cx <= centerX + view; cx++) {
            for (int cz = centerZ - view; cz <= centerZ + view; cz++) {

                LevelChunk chunk =
                        mc.level.getChunkSource()
                                .getChunk(cx, cz, false);

                if (chunk == null)
                    continue;

                processChunk(chunk);
            }
        }
    }

    private boolean isKelpChunk(LevelChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();

        for (int sectionIndex = 0; sectionIndex < chunk.getSectionsCount(); sectionIndex++) {
            LevelChunkSection section = chunk.getSection(sectionIndex);

            if (section.hasOnlyAir()) continue;

            if (!section.maybeHas(state -> state.is(Blocks.KELP))) {
                continue;
            }

            int minY = chunk.getSectionYFromSectionIndex(sectionIndex) << 4;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {

                        BlockState state = section.getBlockState(x, y, z);

                        if (!state.is(Blocks.KELP))
                            continue;

                        int worldX = chunkPos.getMinBlockX() + x;
                        int worldY = minY + y;
                        int worldZ = chunkPos.getMinBlockZ() + z;

                        BlockState above = chunk.getBlockState(
                                new BlockPos(worldX, worldY + 1, worldZ)
                        );

                        if (above.isAir()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void onEnable() {
        rebuild();
    }

    @Override
    public void onDisable() {
        susChunksCache.clear();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        processChunk(event.chunk);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        susChunksCache.remove(event.chunk.getPos());
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        int y = renderHeight.get();

        for (ChunkPos chunk : susChunksCache) {
            double x1 = chunk.getMinBlockX();
            double z1 = chunk.getMinBlockZ();
            double x2 = x1 + 16;
            double z2 = z1 + 16;

            if (renderMode.get() == KelpChunksRenderMode.Filled) {
                Render3D.drawBoxFilled(
                        event.tris,
                        x1, y, z1,
                        x2, y, z2,
                        renderColor.get()
                );
            } else if (renderMode.get() == KelpChunksRenderMode.Outlined) {
                Render3D.drawBoxLines(
                        event.lines,
                        x1, y, z1,
                        x2, y, z2,
                        renderColor.get()
                );
            }
        }
    }
}
