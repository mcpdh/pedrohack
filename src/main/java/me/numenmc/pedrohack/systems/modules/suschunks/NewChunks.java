
package me.numenmc.pedrohack.systems.modules.suschunks;

import me.numenmc.pedrohack.render.world.Color;
import me.numenmc.pedrohack.render.world.Render3D;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.PacketReceiveEvent;
import me.numenmc.pedrohack.systems.event.events.Render3DEvent;
import me.numenmc.pedrohack.systems.settings.ColorSetting;
import me.numenmc.pedrohack.systems.settings.EnumSetting;
import me.numenmc.pedrohack.systems.settings.IntSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NewChunks extends Module {
    SettingCategory mainCategory = SettingCategory.createDefault();

    public enum NewChunksRenderMode {
        Filled,
        Outlined
    }

    ColorSetting renderColor = new ColorSetting.Builder()
            .name("box-color")
            .description("The color that the highlight should render")
            .defaultValue(new Color(255, 0, 0, 100))
            .build();

    EnumSetting<NewChunksRenderMode> renderMode = new EnumSetting.Builder<>(NewChunksRenderMode.class)
            .name("render-mode")
            .description("How the chunks should be rendered")
            .defaultValue(NewChunksRenderMode.Filled)
            .build();

    IntSetting renderHeight = new IntSetting.Builder()
            .name("render-height")
            .description("The height that the chunk marking should render at")
            .defaultValue(63)
            .min(-64)
            .max(319)
            .build();

    private final Set<ChunkPos> newChunks = Collections.synchronizedSet(new HashSet<>());
    private final Set<ChunkPos> oldChunks = Collections.synchronizedSet(new HashSet<>());
    private static final Direction[] searchDirs = new Direction[] { Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.UP };
    private final Executor taskExecutor = Executors.newSingleThreadExecutor();

    public NewChunks() {
        super("new-chunks", "Highlight chunks that have fully grown kelp");

        mainCategory.add(renderColor);
        mainCategory.add(renderMode);
        mainCategory.add(renderHeight);

        addSettingCategory(mainCategory);
    }

    @Override
    public void onDisable() {
        newChunks.clear();
        oldChunks.clear();
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        int y = renderHeight.get();

        for (ChunkPos chunk : newChunks) {
            double x1 = chunk.getMinBlockX();
            double z1 = chunk.getMinBlockZ();
            double x2 = x1 + 16;
            double z2 = z1 + 16;

            if (renderMode.get() == NewChunksRenderMode.Filled) {
                Render3D.drawBoxFilled(
                        event.tris,
                        x1, y, z1,
                        x2, y, z2,
                        renderColor.get()
                );
            } else if (renderMode.get() == NewChunksRenderMode.Outlined) {
                Render3D.drawBoxLines(
                        event.lines,
                        x1, y, z1,
                        x2, y, z2,
                        renderColor.get()
                );
            }
        }
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        if (event.packet instanceof ClientboundSectionBlocksUpdatePacket packet) {
            packet.runUpdates((pos, state) -> {
                if (!state.getFluidState().isEmpty() && !state.getFluidState().isSource()) {
                    ChunkPos chunkPos = ChunkPos.containing(pos);

                    for (Direction dir: searchDirs) {
                        if (mc.level.getBlockState(pos.relative(dir)).getFluidState().isSource() && !oldChunks.contains(chunkPos)) {
                            newChunks.add(chunkPos);
                            return;
                        }
                    }
                }
            });
        } else if (event.packet instanceof ClientboundBlockUpdatePacket packet) {
            if (!packet.getBlockState().getFluidState().isEmpty() && !packet.getBlockState().getFluidState().isSource()) {
                ChunkPos chunkPos = ChunkPos.containing(packet.getPos());

                for (Direction dir: searchDirs) {
                    if (mc.level.getBlockState(packet.getPos().relative(dir)).getFluidState().isSource() && !oldChunks.contains(chunkPos)) {
                        newChunks.add(chunkPos);
                        return;
                    }
                }
            }
        } else if (event.packet instanceof ClientboundLevelChunkWithLightPacket packet) {
            ChunkPos pos = new ChunkPos(packet.getX(), packet.getZ());

            if (!newChunks.contains(pos) && mc.level.getChunkSource().getChunkForLighting(packet.getX(), packet.getZ()) == null) {
                LevelChunk chunk = new LevelChunk(mc.level, pos);

                try {
                    taskExecutor.execute(() -> chunk.replaceWithPacketData(packet.getChunkData().getReadBuffer(), new java.util.HashMap<>(), packet.getChunkData().getBlockEntitiesTagsConsumer(packet.getX(), packet.getZ())));
                } catch (ArrayIndexOutOfBoundsException e) {
                    return;
                }


                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = mc.level.getMinY(); y < mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z); y++) {
                            FluidState fluid = chunk.getFluidState(x, y, z);

                            if (!fluid.isEmpty() && !fluid.isSource()) {
                                oldChunks.add(pos);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
