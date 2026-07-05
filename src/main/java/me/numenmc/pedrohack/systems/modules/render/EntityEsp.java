package me.numenmc.pedrohack.systems.modules.render;

import me.numenmc.pedrohack.render.RenderUtil;
import me.numenmc.pedrohack.render.world.Color;
import me.numenmc.pedrohack.render.world.Render3D;
import me.numenmc.pedrohack.systems.Categories;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.config.Config;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.Render2DEvent;
import me.numenmc.pedrohack.systems.event.events.Render3DEvent;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import me.numenmc.pedrohack.systems.settings.ColorSetting;
import me.numenmc.pedrohack.systems.settings.EntityTypesSetting;
import me.numenmc.pedrohack.systems.settings.EnumSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EntityEsp extends Module {
    SettingCategory mainCategory = SettingCategory.createDefault();

    EntityTypesSetting entities = new EntityTypesSetting.Builder()
            .name("entities")
            .description("A list of entities that ESP boxes should be drawn around.")
            .defaultValue(EntityTypes.PLAYER, EntityTypes.ITEM, EntityTypes.VILLAGER)
            .build();

    public enum EntityEspMode {
        Box,
        CSGO;

        @Override
        public String toString() {
            if (this == EntityEspMode.CSGO) return "CS:GO";
            return this.name();
        }
    }

    EnumSetting<EntityEspMode> espMode = new EnumSetting.Builder<>(EntityEspMode.class)
            .name("esp-mode")
            .description("The mode ESP should render in.")
            .defaultValue(EntityEspMode.Box)
            .build();

    BoolSetting filled = new BoolSetting.Builder()
            .name("filled")
            .description("Should the ESP boxes be filled?")
            .defaultValue(true)
            .build();

    SettingCategory colorsCategory = new SettingCategory("Colors");

    ColorSetting playerColor = new ColorSetting.Builder()
            .name("player-color")
            .description("What color players should render as.")
            .defaultValue(new Color(255, 0, 0, 255))
            .build();

    ColorSetting friendColor = new ColorSetting.Builder()
            .name("friend-color")
            .description("What color friended players should render as.")
            .defaultValue(new Color(3, 252, 236, 255))
            .build();

    ColorSetting hostileColor = new ColorSetting.Builder()
            .name("hostile-color")
            .description("What color hostile entities should render as.")
            .defaultValue(new Color(255, 100, 100, 200))
            .build();

    ColorSetting passiveColor = new ColorSetting.Builder()
            .name("passive-color")
            .description("What color passive entities should render as.")
            .defaultValue(new Color(100, 255, 100, 200))
            .build();

    ColorSetting waterAnimalColor = new ColorSetting.Builder()
            .name("water-animal-color")
            .description("What color water animals should render as.")
            .defaultValue(new Color(100, 180, 255, 200))
            .build();

    ColorSetting miscColor = new ColorSetting.Builder()
            .name("misc-color")
            .description("What color miscellaneous entities should render as.")
            .defaultValue(new Color(255, 255, 255, 200))
            .build();

    public EntityEsp() {
        super("entity-esp", "Draws boxes around entities.");

        mainCategory.add(entities);
        mainCategory.add(espMode);
        mainCategory.add(filled);

        colorsCategory.add(playerColor);
        colorsCategory.add(friendColor);
        colorsCategory.add(hostileColor);
        colorsCategory.add(passiveColor);
        colorsCategory.add(waterAnimalColor);
        colorsCategory.add(miscColor);

        addSettingCategory(mainCategory);
        addSettingCategory(colorsCategory);
    }

    @EventHandler
    public void on3DRender(Render3DEvent event) {
        if (!isEnabled()) return;
        if (espMode.get() != EntityEspMode.Box) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        for (var entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player && !Categories.PLAYER.FREECAM.isEnabled()) continue;
            if (!entities.get().contains(entity.getType())) continue;

            double x = Mth.lerp(event.partialTick, entity.xOld, entity.getX());
            double y = Mth.lerp(event.partialTick, entity.yOld, entity.getY());
            double z = Mth.lerp(event.partialTick, entity.zOld, entity.getZ());

            Color color    = getEntityColor(entity);
            Color fillColor = new Color(color.r, color.g, color.b, 40);

            double hw = entity.getBbWidth() / 2.0;
            double x1 = x - hw, y1 = y,                    z1 = z - hw;
            double x2 = x + hw, y2 = y + entity.getBbHeight(), z2 = z + hw;

            Render3D.drawBoxLines(event.lines, x1, y1, z1, x2, y2, z2, color);

            if (filled.get()) {
                Render3D.drawBoxFilled(event.tris, x1, y1, z1, x2, y2, z2, fillColor);
            }
        }
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (!isEnabled()) return;
        if (espMode.get() != EntityEspMode.CSGO) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        float pt = event.delta.getGameTimeDeltaPartialTick(true);

        double guiScale = mc.getWindow().getGuiScale();

        event.graphics.pose().pushMatrix().scale(
                (float)(1.0 / guiScale),
                (float)(1.0 / guiScale)
        );

        try {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity == mc.player) continue;
                if (!entities.get().contains(entity.getType())) continue;

                double x = Mth.lerp(pt, entity.xOld, entity.getX());
                double y = Mth.lerp(pt, entity.yOld, entity.getY());
                double z = Mth.lerp(pt, entity.zOld, entity.getZ());

                double hw = entity.getBbWidth() / 2.0;
                double h = entity.getBbHeight();

                float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
                float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
                boolean anyVisible = false;

                double[] cornersX = { x - hw, x + hw };
                double[] cornersY = { y, y + h };
                double[] cornersZ = { z - hw, z + hw };

                for (double cx : cornersX) {
                    for (double cy : cornersY) {
                        for (double cz : cornersZ) {
                            Vec3 screen = RenderUtil.to2D((float) cx, (float) cy, (float) cz);
                            if (screen == null) continue;

                            anyVisible = true;

                            // Convert projected GUI coords → physical pixels
                            float sx = (float)(screen.x * guiScale);
                            float sy = (float)(screen.y * guiScale);

                            minX = Math.min(minX, sx);
                            minY = Math.min(minY, sy);
                            maxX = Math.max(maxX, sx);
                            maxY = Math.max(maxY, sy);
                        }
                    }
                }

                if (!anyVisible) continue;

                int argb = RenderUtil.withAlpha(
                        getEntityColor(entity).toARGB(),
                        255
                );

                int outline = 0xFF000000;

                int x1 = Math.round(minX);
                int y1 = Math.round(minY);
                int x2 = Math.round(maxX);
                int y2 = Math.round(maxY);

                event.graphics.fill(
                        x1 - 1,
                        y1 - 1,
                        x2 + 1,
                        y1,
                        outline
                );
                event.graphics.fill(
                        x1 - 1,
                        y2,
                        x2 + 1,
                        y2 + 1,
                        outline
                );
                event.graphics.fill(
                        x1 - 1,
                        y1,
                        x1,
                        y2,
                        outline
                );
                event.graphics.fill(
                        x2,
                        y1,
                        x2 + 1,
                        y2,
                        outline
                );

                event.graphics.fill(x1, y1, x2, y1 + 1, argb); // top
                event.graphics.fill(x1, y2 - 1, x2, y2, argb); // bottom
                event.graphics.fill(x1, y1, x1 + 1, y2, argb); // left
                event.graphics.fill(x2 - 1, y1, x2, y2, argb); // right
            }
        } finally {
            event.graphics.pose().popMatrix();
        }
    }

    private Color getEntityColor(Entity entity) {
        if (entity instanceof Player) {
            if (Config.PLAYER_LISTS.friends.get().contains(entity.getPlainTextName()))
                return friendColor.get();

            return playerColor.get();
        }
        if (entity instanceof Enemy)       return hostileColor.get();
        if (entity instanceof WaterAnimal) return waterAnimalColor.get();
        if (entity instanceof Animal)      return passiveColor.get();
        return miscColor.get();
    }
}
