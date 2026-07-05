package me.numenmc.pedrohack.systems.modules.render;

import me.numenmc.pedrohack.render.RenderUtil;
import me.numenmc.pedrohack.render.world.Color;
import me.numenmc.pedrohack.render.world.Render3D;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.BlockEntityLoadEvent;
import me.numenmc.pedrohack.systems.event.events.BlockEntityUnloadEvent;
import me.numenmc.pedrohack.systems.event.events.DisconnectEvent;
import me.numenmc.pedrohack.systems.event.events.Render3DEvent;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import me.numenmc.pedrohack.systems.settings.ColorSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StorageEsp extends Module {

    private final ConcurrentHashMap<BlockPos, BlockEntityType<?>> blockEntityCache = new ConcurrentHashMap<>();

    private final SettingCategory enableCat = new SettingCategory("Enable");
    private final SettingCategory traceCat = new SettingCategory("Trace");
    private final SettingCategory colorCat = new SettingCategory("Colors");

    private final Map<StorageType, StorageSettings> settingsMap = new EnumMap<>(StorageType.class);

    public StorageEsp() {
        super("storage-esp", "Draws boxes around storage blocks.");

        addSettingCategory(enableCat);
        addSettingCategory(traceCat);
        addSettingCategory(colorCat);

        register(StorageType.CHEST, new Color(255, 160, 0, 255), true);
        register(StorageType.TRAPPED_CHEST, new Color(255, 0, 0, 255), false);
        register(StorageType.BARREL, new Color(255, 160, 0, 255), true);
        register(StorageType.SHULKER, new Color(255, 100, 255, 200), true);
        register(StorageType.ENDER_CHEST, new Color(120, 0, 255, 255), false);

        register(StorageType.HOPPER, new Color(140, 140, 140, 255), false);
        register(StorageType.DISPENSER, new Color(140, 140, 140, 255), false);
        register(StorageType.DROPPER, new Color(140, 140, 140, 255), false);
        register(StorageType.FURNACE, new Color(140, 140, 140, 255), false);
        register(StorageType.BLAST_FURNACE, new Color(140, 140, 140, 255), false);
        register(StorageType.SMOKER, new Color(140, 140, 140, 255), false);
    }

    private void register(StorageType type, Color defaultColor, boolean defaultTrace) {
        StorageSettings s = new StorageSettings();
        String key = type.keyName();

        s.enabled = new BoolSetting.Builder()
                .name("enable-" + key)
                .description("Should " + type.displayName() + "s render?")
                .defaultValue(true)
                .build();

        s.trace = new BoolSetting.Builder()
                .name("trace-" + key)
                .description("Should " + type.displayName() + "s render a tracer?")
                .defaultValue(defaultTrace)
                .build();

        s.color = new ColorSetting.Builder()
                .name(key + "-color")
                .description("The color that the " + type.displayName() + " will render with.")
                .defaultValue(defaultColor)
                .build();

        enableCat.add(s.enabled);
        traceCat.add(s.trace);
        colorCat.add(s.color);

        settingsMap.put(type, s);
    }

    @EventHandler
    public void onDisconnect(DisconnectEvent event) {
        blockEntityCache.clear();
    }

    @EventHandler
    public void onBlockEntityLoad(BlockEntityLoadEvent event) {
        blockEntityCache.put(event.blockEntity.getBlockPos(), event.blockEntity.getType());
    }

    @EventHandler
    public void onBlockEntityUnload(BlockEntityUnloadEvent event) {
        blockEntityCache.remove(event.blockEntity.getBlockPos());
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        blockEntityCache.entrySet().removeIf(entry ->
                mc.level.getBlockEntity(entry.getKey()) == null
        );

        for (var entry : blockEntityCache.entrySet()) {

            StorageType type = of(entry.getValue());
            if (type == null) continue;

            StorageSettings s = settingsMap.get(type);
            if (s == null || !s.enabled.get()) continue;

            BlockPos pos = entry.getKey();

            double x1 = pos.getX();
            double y1 = pos.getY();
            double z1 = pos.getZ();

            double x2 = x1 + 1;
            double y2 = y1 + 1;
            double z2 = z1 + 1;

            Color color = s.color.get();
            Color fill = new Color(color.r, color.g, color.b, 40);

            Render3D.drawBoxLines(event.lines, x1, y1, z1, x2, y2, z2, color);

            if (s.trace.get()) {
                Render3D.drawLine3D(
                        event.lines,
                        RenderUtil.center.x, RenderUtil.center.y, RenderUtil.center.z,
                        x1 + 0.5, y2, z1 + 0.5,
                        color
                );
            }

            Render3D.drawBoxFilled(event.tris, x1, y1, z1, x2, y2, z2, fill);
        }
    }

    @Override
    public void onEnable() {
        blockEntityCache.clear();

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        int view = mc.options.renderDistance().get();
        int camChunkX = mc.player.getBlockX() >> 4;
        int camChunkZ = mc.player.getBlockZ() >> 4;

        for (int cx = camChunkX - view; cx <= camChunkX + view; cx++) {
            for (int cz = camChunkZ - view; cz <= camChunkZ + view; cz++) {
                var chunk = mc.level.getChunkSource().getChunk(cx, cz, false);
                if (chunk == null) continue;

                chunk.getBlockEntities().forEach((pos, be) -> {
                    StorageType t = of(be.getType());
                    if (t != null) {
                        blockEntityCache.put(pos, be.getType());
                    }
                });
            }
        }
    }

    @Override
    public void onDisable() {
        blockEntityCache.clear();
    }

    private StorageType of(BlockEntityType<?> type) {
        if (type == BlockEntityTypes.CHEST) return StorageType.CHEST;
        if (type == BlockEntityTypes.TRAPPED_CHEST) return StorageType.TRAPPED_CHEST;
        if (type == BlockEntityTypes.BARREL) return StorageType.BARREL;
        if (type == BlockEntityTypes.SHULKER_BOX) return StorageType.SHULKER;
        if (type == BlockEntityTypes.ENDER_CHEST) return StorageType.ENDER_CHEST;
        if (type == BlockEntityTypes.HOPPER) return StorageType.HOPPER;
        if (type == BlockEntityTypes.DISPENSER) return StorageType.DISPENSER;
        if (type == BlockEntityTypes.DROPPER) return StorageType.DROPPER;
        if (type == BlockEntityTypes.FURNACE) return StorageType.FURNACE;
        if (type == BlockEntityTypes.BLAST_FURNACE) return StorageType.BLAST_FURNACE;
        if (type == BlockEntityTypes.SMOKER) return StorageType.SMOKER;
        return null;
    }

    private static class StorageSettings {
        BoolSetting enabled;
        BoolSetting trace;
        ColorSetting color;
    }

    private enum StorageType {
        CHEST("Chest"),
        TRAPPED_CHEST("Trapped Chest"),
        BARREL("Barrel"),
        SHULKER("Shulker Box"),
        ENDER_CHEST("Ender Chest"),
        HOPPER("Hopper"),
        DISPENSER("Dispenser"),
        DROPPER("Dropper"),
        FURNACE("Furnace"),
        BLAST_FURNACE("Blast Furnace"),
        SMOKER("Smoker");

        private final String display;

        StorageType(String display) {
            this.display = display;
        }

        public String displayName() {
            return display;
        }

        public String keyName() {
            return name().toLowerCase().replace('_', '-');
        }
    }
}
