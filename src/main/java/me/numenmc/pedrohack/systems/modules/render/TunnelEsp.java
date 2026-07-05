package me.numenmc.pedrohack.systems.modules.render;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.numenmc.pedrohack.render.world.Color;
import me.numenmc.pedrohack.render.world.MeshBuilder;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.ChunkLoadEvent;
import me.numenmc.pedrohack.systems.event.events.ChunkUnloadEvent;
import me.numenmc.pedrohack.systems.event.events.DisconnectEvent;
import me.numenmc.pedrohack.systems.event.events.Render3DEvent;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import me.numenmc.pedrohack.systems.settings.ColorSetting;
import me.numenmc.pedrohack.systems.settings.EnumSetting;
import me.numenmc.pedrohack.systems.settings.IntSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TunnelEsp extends Module {
    private static final Direction[] DIRECTIONS = {Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST};

    // Background scanning is serialized on one daemon thread so we never touch BlockState/collision
    // shape lookups from more than one thread at a time, and so a flood of chunk loads (teleport,
    // elytra) just queues up instead of spawning a pile of concurrent scans.
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "tunnel-esp-scanner");
        t.setDaemon(true);
        return t;
    });

    private final SettingCategory general = SettingCategory.createDefault();
    private final SettingCategory colors = new SettingCategory("Colors");

    // Stored as hundredths of a block (1-200) since there's no DoubleSetting here;
    // height.get() / 100.0 gives the same 0.0-2.0 range the meteor version exposed.
    private final IntSetting height = new IntSetting.Builder()
            .name("height")
            .description("Height of the rendered box, in % of a block.")
            .defaultValue(10)
            .min(1)
            .max(200)
            .build();

    private final BoolSetting connected = new BoolSetting.Builder()
            .name("connected")
            .description("If neighbouring holes should be connected.")
            .defaultValue(true)
            .build();

    private final EnumSetting<ShapeMode> shapeMode = new EnumSetting.Builder<>(ShapeMode.class)
            .name("shape-mode")
            .description("How the tunnel boxes are rendered.")
            .defaultValue(ShapeMode.Both)
            .build();

    private final ColorSetting sideColor = new ColorSetting.Builder()
            .name("side-color")
            .description("The side color.")
            .defaultValue(new Color(255, 175, 25, 50))
            .build();

    private final ColorSetting lineColor = new ColorSetting.Builder()
            .name("line-color")
            .description("The line color.")
            .defaultValue(new Color(255, 175, 25, 255))
            .build();

    private final Long2ObjectMap<TChunk> chunks = new Long2ObjectOpenHashMap<>();

    public TunnelEsp() {
        super("tunnel-esp", "Highlights tunnels and vertical shafts.");

        general.add(height);
        general.add(connected);
        general.add(shapeMode);

        colors.add(sideColor);
        colors.add(lineColor);

        addSettingCategory(general);
        addSettingCategory(colors);
    }

    @Override
    public void onEnable() {
        synchronized (chunks) {
            chunks.clear();
        }

        // ChunkLoadEvent only fires for chunks that load *after* this point, so anything already
        // sitting in render distance when the module gets turned on needs an initial sweep here.
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        int view = mc.options.renderDistance().get();
        int camChunkX = mc.player.getBlockX() >> 4;
        int camChunkZ = mc.player.getBlockZ() >> 4;

        for (int cx = camChunkX - view; cx <= camChunkX + view; cx++) {
            for (int cz = camChunkZ - view; cz <= camChunkZ + view; cz++) {
                var chunk = mc.level.getChunkSource().getChunk(cx, cz, false);
                if (chunk != null) queueScan(chunk);
            }
        }
    }

    @Override
    public void onDisable() {
        synchronized (chunks) {
            chunks.clear();
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!isEnabled()) return;
        queueScan(event.chunk);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        long key = ChunkPos.pack(event.chunk.getPos().x(), event.chunk.getPos().z());
        synchronized (chunks) {
            chunks.remove(key);
        }
    }

    @EventHandler
    public void onDisconnect(DisconnectEvent event) {
        synchronized (chunks) {
            chunks.clear();
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled()) return;

        synchronized (chunks) {
            for (TChunk chunk : chunks.values()) chunk.render(event.lines, event.tris);
        }
    }

    private void queueScan(ChunkAccess chunk) {
        long key = ChunkPos.pack(chunk.getPos().x(), chunk.getPos().z());

        TChunk tChunk;
        synchronized (chunks) {
            if (chunks.containsKey(key)) return;
            tChunk = new TChunk(chunk.getPos().x(), chunk.getPos().z());
            chunks.put(key, tChunk);
        }

        EXECUTOR.execute(() -> {
            // Bail if the chunk unloaded again before we got around to scanning it.
            synchronized (chunks) {
                if (!chunks.containsKey(key)) return;
            }

            searchChunk(chunk, tChunk);
        });
    }

    private static int pack(int x, int y, int z) {
        return ((x & 0xFF) << 24) | ((y & 0xFFFF) << 8) | (z & 0xFF);
    }

    private static byte getPackedX(int p) {
        return (byte) (p >> 24 & 0xFF);
    }

    private static short getPackedY(int p) {
        return (short) (p >> 8 & 0xFFFF);
    }

    private static byte getPackedZ(int p) {
        return (byte) (p & 0xFF);
    }

    private void searchChunk(ChunkAccess chunk, TChunk tChunk) {
        Context ctx = new Context();

        // Horizontal tunnels go through the neighbour filter to strip isolated
        // single-block false positives in open terrain.
        IntSet set = new IntOpenHashSet();

        // Shaft cells are accepted immediately — an all-sides-blocked walkable
        // cell underground is inherently unambiguous and has no horizontal
        // neighbours to filter against.
        IntSet shafts = new IntOpenHashSet();

        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();

        int endX = chunk.getPos().getMaxBlockX();
        int endZ = chunk.getPos().getMaxBlockZ();

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                int surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE).getFirstAvailable(x - startX, z - startZ);

                for (short y = (short) mc.level.getMinY(); y < surface; y++) {
                    int packed = pack(x - startX, y, z - startZ);
                    if      (isTunnel(ctx, x, y, z)) set.add(packed);
                    else if (isShaft (ctx, x, y, z)) shafts.add(packed);
                }
            }
        }

        // Shaft positions are pre-accepted; seed the result set with them.
        IntSet positions = new IntOpenHashSet(shafts);

        // Apply the horizontal-neighbour filter only to tunnel candidates.
        // Cells on a chunk edge get a pass since their neighbour may live in an unscanned chunk.
        for (IntIterator it = set.iterator(); it.hasNext(); ) {
            int packed = it.nextInt();

            byte  lx = getPackedX(packed);
            short ly = getPackedY(packed);
            byte  lz = getPackedZ(packed);

            if (lx == 0 || lx == 15 || lz == 0 || lz == 15) {
                positions.add(packed);
            } else {
                boolean has = false;

                for (Direction dir : DIRECTIONS) {
                    if (set.contains(pack(lx + dir.getStepX(), ly, lz + dir.getStepZ()))) {
                        has = true;
                        break;
                    }
                }

                if (has) positions.add(packed);
            }
        }

        tChunk.positions = positions;
    }

    private boolean isTunnel(Context ctx, int x, int y, int z) {
        if (!canWalkIn(ctx, x, y, z)) return false;

        TunnelSide s1 = getTunnelSide(ctx, x + 1, y, z);
        if (s1 == TunnelSide.PartiallyBlocked) return false;

        TunnelSide s2 = getTunnelSide(ctx, x - 1, y, z);
        if (s2 == TunnelSide.PartiallyBlocked) return false;

        TunnelSide s3 = getTunnelSide(ctx, x, y, z + 1);
        if (s3 == TunnelSide.PartiallyBlocked) return false;

        TunnelSide s4 = getTunnelSide(ctx, x, y, z - 1);
        if (s4 == TunnelSide.PartiallyBlocked) return false;

        return (s1 == TunnelSide.Walkable && s2 == TunnelSide.Walkable && s3 == TunnelSide.FullyBlocked && s4 == TunnelSide.FullyBlocked)
                || (s1 == TunnelSide.FullyBlocked && s2 == TunnelSide.FullyBlocked && s3 == TunnelSide.Walkable && s4 == TunnelSide.Walkable);
    }

    // A vertical shaft: walkable position where all four horizontal sides are
    // fully sealed walls (at both foot and head height). canWalkIn already
    // guarantees y+2 is solid, so no extra ceiling check is required.
    private boolean isShaft(Context ctx, int x, int y, int z) {
        if (!canWalkIn(ctx, x, y, z)) return false;

        return getTunnelSide(ctx, x + 1, y, z) == TunnelSide.FullyBlocked
                && getTunnelSide(ctx, x - 1, y, z) == TunnelSide.FullyBlocked
                && getTunnelSide(ctx, x, y, z + 1) == TunnelSide.FullyBlocked
                && getTunnelSide(ctx, x, y, z - 1) == TunnelSide.FullyBlocked;
    }

    private TunnelSide getTunnelSide(Context ctx, int x, int y, int z) {
        if (canWalkIn(ctx, x, y, z)) return TunnelSide.Walkable;
        if (!canWalkThrough(ctx, x, y, z) && !canWalkThrough(ctx, x, y + 1, z)) return TunnelSide.FullyBlocked;
        return TunnelSide.PartiallyBlocked;
    }

    private boolean canWalkOn(Context ctx, int x, int y, int z) {
        BlockState state = ctx.get(x, y, z);

        if (state.isAir()) return false;
        if (!state.getFluidState().isEmpty()) return false;

        return !state.getCollisionShape(ctx.world, new BlockPos(x, y, z)).isEmpty();
    }

    private boolean canWalkThrough(Context ctx, int x, int y, int z) {
        BlockState state = ctx.get(x, y, z);

        if (state.isAir()) return true;
        if (!state.getFluidState().isEmpty()) return false;

        return state.getCollisionShape(ctx.world, new BlockPos(x, y, z)).isEmpty();
    }

    private boolean canWalkIn(Context ctx, int x, int y, int z) {
        if (!canWalkOn(ctx, x, y - 1, z)) return false;
        if (!canWalkThrough(ctx, x, y, z)) return false;
        if (canWalkThrough(ctx, x, y + 2, z)) return false;
        return canWalkThrough(ctx, x, y + 1, z);
    }

    private boolean containsTunnel(TChunk chunk, int x, int y, int z) {
        int key;

        if (x == -1) {
            chunk = chunks.get(ChunkPos.pack(chunk.x - 1, chunk.z));
            key = pack(15, y, z);
        } else if (x == 16) {
            chunk = chunks.get(ChunkPos.pack(chunk.x + 1, chunk.z));
            key = pack(0, y, z);
        } else if (z == -1) {
            chunk = chunks.get(ChunkPos.pack(chunk.x, chunk.z - 1));
            key = pack(x, y, 15);
        } else if (z == 16) {
            chunk = chunks.get(ChunkPos.pack(chunk.x, chunk.z + 1));
            key = pack(x, y, 0);
        } else key = pack(x, y, z);

        return chunk != null && chunk.positions != null && chunk.positions.contains(key);
    }

    private class TChunk {
        private final int x, z;
        private volatile IntSet positions;

        public TChunk(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public void render(MeshBuilder lines, MeshBuilder tris) {
            IntSet pos = positions;
            if (pos == null) return;

            double h = height.get() / 100.0;
            ShapeMode mode = shapeMode.get();
            Color side = sideColor.get();
            Color line = lineColor.get();
            boolean conn = connected.get();

            for (IntIterator it = pos.iterator(); it.hasNext(); ) {
                int p = it.nextInt();

                int lx = getPackedX(p);
                int ly = getPackedY(p);
                int lz = getPackedZ(p);

                boolean connWest  = conn && containsTunnel(this, lx - 1, ly, lz);
                boolean connEast  = conn && containsTunnel(this, lx + 1, ly, lz);
                boolean connNorth = conn && containsTunnel(this, lx, ly, lz - 1);
                boolean connSouth = conn && containsTunnel(this, lx, ly, lz + 1);

                double wx = lx + this.x * 16;
                double wz = lz + this.z * 16;

                renderCell(lines, tris, mode, wx, ly, wz, h, connWest, connEast, connNorth, connSouth, side, line);
            }
        }
    }

    // Draws a 1x1xh box, skipping the wireframe edges/quads that border a connected neighbour so
    // adjoining tunnel cells read as one continuous strip instead of a row of seamed boxes.
    private void renderCell(MeshBuilder lines, MeshBuilder tris, ShapeMode mode,
                            double x, double y, double z, double h,
                            boolean connWest, boolean connEast, boolean connNorth, boolean connSouth,
                            Color side, Color line) {
        double x1 = x, x2 = x + 1;
        double y1 = y, y2 = y + h;
        double z1 = z, z2 = z + 1;

        if (mode != ShapeMode.Sides) {
            lines.ensureCapacity(8, 24);

            int blb = lines.vec3(x1, y1, z1).color(line).next();
            int blf = lines.vec3(x1, y1, z2).color(line).next();
            int brb = lines.vec3(x2, y1, z1).color(line).next();
            int brf = lines.vec3(x2, y1, z2).color(line).next();
            int tlb = lines.vec3(x1, y2, z1).color(line).next();
            int tlf = lines.vec3(x1, y2, z2).color(line).next();
            int trb = lines.vec3(x2, y2, z1).color(line).next();
            int trf = lines.vec3(x2, y2, z2).color(line).next();

            if (!connWest)  { lines.line(blb, blf); lines.line(tlb, tlf); }
            if (!connSouth) { lines.line(blf, brf); lines.line(tlf, trf); }
            if (!connEast)  { lines.line(brf, brb); lines.line(trf, trb); }
            if (!connNorth) { lines.line(brb, blb); lines.line(trb, tlb); }

            if (!(connWest  && connNorth)) lines.line(blb, tlb);
            if (!(connWest  && connSouth)) lines.line(blf, tlf);
            if (!(connEast  && connSouth)) lines.line(brf, trf);
            if (!(connEast  && connNorth)) lines.line(brb, trb);
        }

        if (mode != ShapeMode.Lines) {
            tris.ensureCapacity(8, 36);

            int blb = tris.vec3(x1, y1, z1).color(side).next();
            int blf = tris.vec3(x1, y1, z2).color(side).next();
            int brb = tris.vec3(x2, y1, z1).color(side).next();
            int brf = tris.vec3(x2, y1, z2).color(side).next();
            int tlb = tris.vec3(x1, y2, z1).color(side).next();
            int tlf = tris.vec3(x1, y2, z2).color(side).next();
            int trb = tris.vec3(x2, y2, z1).color(side).next();
            int trf = tris.vec3(x2, y2, z2).color(side).next();

            tris.quad(blb, brb, brf, blf); // bottom
            tris.quad(tlb, tlf, trf, trb); // top
            if (!connWest)  tris.quad(blb, blf, tlf, tlb);
            if (!connEast)  tris.quad(brb, trb, trf, brf);
            if (!connNorth) tris.quad(blb, tlb, trb, brb);
            if (!connSouth) tris.quad(blf, brf, trf, tlf);
        }
    }

    private static class Context {
        private final Level world;
        private ChunkAccess lastChunk;

        public Context() {
            this.world = Minecraft.getInstance().level;
        }

        public BlockState get(int x, int y, int z) {
            if (world.isOutsideBuildHeight(y)) return Blocks.VOID_AIR.defaultBlockState();

            int cx = x >> 4;
            int cz = z >> 4;

            ChunkAccess chunk;

            if (lastChunk != null && lastChunk.getPos().x() == cx && lastChunk.getPos().z() == cz) chunk = lastChunk;
            else chunk = Minecraft.getInstance().level.getChunkSource().getChunk(cx, cz, false);

            if (chunk == null) return Blocks.VOID_AIR.defaultBlockState();

            LevelChunkSection section = chunk.getSections()[chunk.getSectionIndex(y)];
            if (section == null) return Blocks.VOID_AIR.defaultBlockState();

            lastChunk = chunk;
            return section.getBlockState(x & 15, y & 15, z & 15);
        }
    }

    private enum TunnelSide {
        Walkable,
        PartiallyBlocked,
        FullyBlocked
    }

    private enum ShapeMode {
        Lines,
        Sides,
        Both
    }
}
