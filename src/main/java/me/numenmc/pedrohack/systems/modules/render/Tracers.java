package me.numenmc.pedrohack.systems.modules.render;

import me.numenmc.pedrohack.render.RenderUtil;
import me.numenmc.pedrohack.render.world.Color;
import me.numenmc.pedrohack.render.world.Render3D;
import me.numenmc.pedrohack.systems.Categories;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.config.Config;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.Render3DEvent;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import me.numenmc.pedrohack.systems.settings.ColorSetting;
import me.numenmc.pedrohack.systems.settings.EntityTypesSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public class Tracers extends Module {
    SettingCategory mainCategory = SettingCategory.createDefault();

    EntityTypesSetting entities = new EntityTypesSetting.Builder()
            .name("entities")
            .description("A list of entities that tracer lines should be drawn to.")
            .defaultValue(
                    EntityTypes.VILLAGER,
                    EntityTypes.PLAYER
            )
            .build();

    BoolSetting renderSkeleton = new BoolSetting.Builder()
            .name("render-skeleton")
            .description("Render a second line that shows the bounding box of the entity.")
            .defaultValue(true)
            .build();

    SettingCategory colorsCategory =
            new SettingCategory("Colors");

    // Players
    ColorSetting playerColor =
            new ColorSetting.Builder()
                    .name("player-color")
                    .description("What color players should render as.")
                    .defaultValue(new Color(255, 0, 0, 255))
                    .build();

    BoolSetting useDistanceForPlayerColor =
            new BoolSetting.Builder()
                    .name("use-distance-for-player-color")
                    .description("Change player color based on distance.")
                    .defaultValue(true)
                    .build();

    // Friends
    ColorSetting friendColor =
            new ColorSetting.Builder()
                    .name("friend-color")
                    .description("What color friends should render as.")
                    .defaultValue(new Color(3, 252, 236, 255))
                    .build();

    BoolSetting useDistanceForFriendColor =
            new BoolSetting.Builder()
                    .name("use-distance-for-friend-color")
                    .description("Change friend color based on distance.")
                    .defaultValue(true)
                    .build();

    // Hostile
    ColorSetting hostileColor =
            new ColorSetting.Builder()
                    .name("hostile-color")
                    .description("What color hostile entities should render as.")
                    .defaultValue(new Color(255, 100, 100, 100))
                    .build();

    BoolSetting useDistanceForHostileColor =
            new BoolSetting.Builder()
                    .name("use-distance-for-hostile-color")
                    .description("Change hostile color based on distance.")
                    .defaultValue(false)
                    .build();

    // Passive
    ColorSetting passiveColor =
            new ColorSetting.Builder()
                    .name("passive-color")
                    .description("What color passive entities should render as.")
                    .defaultValue(new Color(100, 255, 100, 100))
                    .build();

    BoolSetting useDistanceForPassiveColor =
            new BoolSetting.Builder()
                    .name("use-distance-for-passive-color")
                    .description("Change passive color based on distance.")
                    .defaultValue(false)
                    .build();

    // Water
    ColorSetting waterAnimalColor =
            new ColorSetting.Builder()
                    .name("water-animal-color")
                    .description("What color water animals should render as.")
                    .defaultValue(new Color(100, 180, 255, 100))
                    .build();

    BoolSetting useDistanceForWaterAnimalColor =
            new BoolSetting.Builder()
                    .name("use-distance-for-water-animal-color")
                    .description("Change water animal color based on distance.")
                    .defaultValue(false)
                    .build();

    // Misc
    ColorSetting miscColor =
            new ColorSetting.Builder()
                    .name("misc-color")
                    .description("What color miscellaneous entities should render as.")
                    .defaultValue(new Color(255, 255, 255, 100))
                    .build();

    BoolSetting useDistanceForMiscColor =
            new BoolSetting.Builder()
                    .name("use-distance-for-misc-color")
                    .description("Change miscellaneous entity color based on distance.")
                    .defaultValue(false)
                    .build();

    public Tracers() {
        super("tracers", "Draws lines to entities.");

        mainCategory.add(entities);
        mainCategory.add(renderSkeleton);

        colorsCategory.add(playerColor);
        colorsCategory.add(useDistanceForPlayerColor);

        colorsCategory.add(friendColor);
        colorsCategory.add(useDistanceForFriendColor);

        colorsCategory.add(hostileColor);
        colorsCategory.add(useDistanceForHostileColor);

        colorsCategory.add(passiveColor);
        colorsCategory.add(useDistanceForPassiveColor);

        colorsCategory.add(waterAnimalColor);
        colorsCategory.add(useDistanceForWaterAnimalColor);

        colorsCategory.add(miscColor);
        colorsCategory.add(useDistanceForMiscColor);

        addSettingCategory(mainCategory);
        addSettingCategory(colorsCategory);
    }

    @EventHandler
    public void on3DRender(Render3DEvent event) {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        for (var entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player && !Categories.PLAYER.FREECAM.isEnabled()) continue;
            if (!entities.get().contains(entity.getType())) continue;

            double x = Mth.lerp(event.partialTick, entity.xOld, entity.getX());
            double y = Mth.lerp(event.partialTick, entity.yOld, entity.getY());
            double z = Mth.lerp(event.partialTick, entity.zOld, entity.getZ());

            Color color = getEntityColor(entity);

            Render3D.drawLine3D(
                    event.lines,
                    RenderUtil.center.x,
                    RenderUtil.center.y,
                    RenderUtil.center.z,
                    x,
                    y + (entity.getBbHeight() / 2.0),
                    z,
                    color
            );

            if (renderSkeleton.get()) {
                Render3D.drawLine3D(
                        event.lines,
                        x,
                        y,
                        z,
                        x,
                        y + entity.getBbHeight(),
                        z,
                        color
                );
            }
        }
    }

    private Color getEntityColor(Entity entity) {
        Minecraft mc = Minecraft.getInstance();

        Color base;
        boolean useDistance;

        switch (entity) {
            case Player player -> {
                if (Config.PLAYER_LISTS.friends.get().contains(entity.getPlainTextName())) {
                    base = friendColor.get();
                    useDistance = useDistanceForFriendColor.get();
                } else {
                    base = playerColor.get();
                    useDistance = useDistanceForPlayerColor.get();
                }
            }
            case Enemy enemy -> {
                base = hostileColor.get();
                useDistance = useDistanceForHostileColor.get();
            }
            case WaterAnimal waterAnimal -> {
                base = waterAnimalColor.get();
                useDistance = useDistanceForWaterAnimalColor.get();
            }
            case Animal animal -> {
                base = passiveColor.get();
                useDistance = useDistanceForPassiveColor.get();
            }
            case null, default -> {
                base = miscColor.get();
                useDistance = useDistanceForMiscColor.get();
            }
        }

        if (!useDistance || mc.player == null) {
            return base;
        }

        return applyDistance(base, mc.player.distanceTo(entity));
    }

    private Color applyDistance(Color base, float distance) {
        float t = Math.clamp(distance / 100f, 0f, 1f);
        int r = (int) (255 * (1f - t));
        int g = (int) (255 * t);
        int b = 0;
        return new Color(r, g, b, base.a);
    }
}
