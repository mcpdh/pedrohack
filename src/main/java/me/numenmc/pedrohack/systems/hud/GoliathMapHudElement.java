package me.numenmc.pedrohack.systems.hud;

import me.numenmc.pedrohack.Pedrohack;
import me.numenmc.pedrohack.render.RenderUtil;
import me.numenmc.pedrohack.render.Theme;
import me.numenmc.pedrohack.systems.HudElement;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GoliathMapHudElement extends HudElement {
    public enum Region {
        EU_CENTRAL("EU Central"),
        EU_WEST("EU West"),
        NA_EAST("NA East"),
        NA_WEST("NA West"),
        ASIA("Asia"),
        OCEANIA("Oceania");

        private final String displayName;

        Region(String n) {
            this.displayName = n;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public record Goliath(Region region, int number, int row, int col) {}

    private static final List<Goliath> goliaths = new ArrayList<>();

    static {
        goliaths.add(new Goliath(Region.OCEANIA, 82, 0, 0));
        goliaths.add(new Goliath(Region.NA_WEST, 100, 0, 1));
        goliaths.add(new Goliath(Region.NA_WEST, 101, 0, 2));
        goliaths.add(new Goliath(Region.NA_WEST, 102, 0, 3));
        goliaths.add(new Goliath(Region.NA_EAST, 103, 0, 4));
        goliaths.add(new Goliath(Region.NA_EAST, 104, 0, 5));
        goliaths.add(new Goliath(Region.NA_EAST, 105, 0, 6));
        goliaths.add(new Goliath(Region.NA_EAST, 106, 0, 7));
        goliaths.add(new Goliath(Region.NA_EAST, 91, 0, 8));

        goliaths.add(new Goliath(Region.OCEANIA, 83, 1, 0));
        goliaths.add(new Goliath(Region.NA_WEST, 44, 1, 1));
        goliaths.add(new Goliath(Region.NA_WEST, 75, 1, 2));
        goliaths.add(new Goliath(Region.NA_WEST, 42, 1, 3));
        goliaths.add(new Goliath(Region.NA_EAST, 41, 1, 4));
        goliaths.add(new Goliath(Region.NA_EAST, 40, 1, 5));
        goliaths.add(new Goliath(Region.NA_EAST, 39, 1, 6));
        goliaths.add(new Goliath(Region.NA_EAST, 38, 1, 7));
        goliaths.add(new Goliath(Region.NA_EAST, 92, 1, 8));

        goliaths.add(new Goliath(Region.OCEANIA, 84, 2, 0));
        goliaths.add(new Goliath(Region.NA_WEST, 45, 2, 1));
        goliaths.add(new Goliath(Region.NA_WEST, 14, 2, 2));
        goliaths.add(new Goliath(Region.NA_WEST, 13, 2, 3));
        goliaths.add(new Goliath(Region.NA_EAST, 12, 2, 4));
        goliaths.add(new Goliath(Region.NA_EAST, 11, 2, 5));
        goliaths.add(new Goliath(Region.NA_EAST, 10, 2, 6));
        goliaths.add(new Goliath(Region.NA_EAST, 37, 2, 7));
        goliaths.add(new Goliath(Region.NA_EAST, 93, 2, 8));

        goliaths.add(new Goliath(Region.OCEANIA, 85, 3, 0));
        goliaths.add(new Goliath(Region.OCEANIA, 46, 3, 1));
        goliaths.add(new Goliath(Region.OCEANIA, 74, 3, 2));
        goliaths.add(new Goliath(Region.NA_WEST, 3, 3, 3));
        goliaths.add(new Goliath(Region.NA_EAST, 2, 3, 4));
        goliaths.add(new Goliath(Region.NA_EAST, 1, 3, 5));
        goliaths.add(new Goliath(Region.NA_EAST, 25, 3, 6));
        goliaths.add(new Goliath(Region.NA_EAST, 36, 3, 7));
        goliaths.add(new Goliath(Region.NA_EAST, 94, 3, 8));

        goliaths.add(new Goliath(Region.ASIA, 86, 4, 0));
        goliaths.add(new Goliath(Region.ASIA, 47, 4, 1));
        goliaths.add(new Goliath(Region.ASIA, 72, 4, 2));
        goliaths.add(new Goliath(Region.ASIA, 71, 4, 3));
        goliaths.add(new Goliath(Region.NA_EAST, 5, 4, 4));
        goliaths.add(new Goliath(Region.NA_EAST, 4, 4, 5));
        goliaths.add(new Goliath(Region.NA_EAST, 24, 4, 6));
        goliaths.add(new Goliath(Region.NA_EAST, 35, 4, 7));
        goliaths.add(new Goliath(Region.NA_EAST, 95, 4, 8));

        goliaths.add(new Goliath(Region.ASIA, 87, 5, 0));
        goliaths.add(new Goliath(Region.EU_WEST, 51, 5, 1));
        goliaths.add(new Goliath(Region.EU_WEST, 17, 5, 2));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 9, 5, 3));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 8, 5, 4));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 7, 5, 5));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 23, 5, 6));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 34, 5, 7));
        goliaths.add(new Goliath(Region.NA_EAST, 96, 5, 8));

        goliaths.add(new Goliath(Region.ASIA, 88, 6, 0));
        goliaths.add(new Goliath(Region.EU_WEST, 54, 6, 1));
        goliaths.add(new Goliath(Region.EU_WEST, 18, 6, 2));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 61, 6, 3));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 62, 6, 4));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 21, 6, 5));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 22, 6, 6));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 33, 6, 7));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 97, 6, 8));

        goliaths.add(new Goliath(Region.EU_CENTRAL, 89, 7, 0));
        goliaths.add(new Goliath(Region.EU_WEST, 26, 7, 1));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 27, 7, 2));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 28, 7, 3));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 29, 7, 4));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 30, 7, 5));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 59, 7, 6));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 32, 7, 7));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 98, 7, 8));

        goliaths.add(new Goliath(Region.EU_CENTRAL, 90, 8, 0));
        goliaths.add(new Goliath(Region.EU_WEST, 107, 8, 1));
        goliaths.add(new Goliath(Region.EU_WEST, 108, 8, 2));
        goliaths.add(new Goliath(Region.EU_WEST, 109, 8, 3));
        goliaths.add(new Goliath(Region.EU_WEST, 110, 8, 4));
        goliaths.add(new Goliath(Region.EU_WEST, 111, 8, 5));
        goliaths.add(new Goliath(Region.EU_WEST, 112, 8, 6));
        goliaths.add(new Goliath(Region.EU_WEST, 113, 8, 7));
        goliaths.add(new Goliath(Region.EU_CENTRAL, 99, 8, 8));
    }

    private static final int goliathBlockWidth = 50_000;
    private static final int renderWidth = 12;

    private static final Map<Region, Integer> REGION_COLORS = new EnumMap<>(Region.class);

    static {
        REGION_COLORS.put(Region.EU_CENTRAL, 0xFFb7da8a);
        REGION_COLORS.put(Region.EU_WEST,    0xFF00a663);
        REGION_COLORS.put(Region.NA_EAST,    0xFF4fadea);
        REGION_COLORS.put(Region.NA_WEST,    0xFF2f6eba);
        REGION_COLORS.put(Region.ASIA,       0xFFf5c242);
        REGION_COLORS.put(Region.OCEANIA,    0xFFfc8803);
    }

    final int GAP = 1;
    final int OUTLINE = 1;
    final int cellOuter = renderWidth + GAP;

    int cols = 9, rows = 9;
    int gridPixelW = cols * cellOuter - GAP + OUTLINE * 2;
    int gridPixelH = rows * cellOuter - GAP + OUTLINE * 2;

    final int leftMargin = 50;

    @Override
    public int getWidth() {
        return (9 * cellOuter - GAP + OUTLINE * 2) + leftMargin;
    }

    @Override
    public int getHeight() {
        return 9 * cellOuter - GAP + OUTLINE * 2;
    }

    private long lastGoliathCheck = Util.getMillis();
    private Goliath currentGoliath = null;
    private int playerX = 0;
    private int playerZ = 0;

    private static final Identifier dartImage = Identifier.fromNamespaceAndPath(Pedrohack.id, "textures/gui/dart.png");

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        long now = Util.getMillis();
        if (currentGoliath == null || now - lastGoliathCheck > 1000) {
            LocalPlayer pl = Minecraft.getInstance().player;
            if (pl != null) {
                playerX = (int) Math.round(pl.position().x);
                playerZ = (int) Math.round(pl.position().z);
                currentGoliath = getGoliathAt(playerX, playerZ);
            }
            lastGoliathCheck = now;
        }

        graphics.fill(0, 0, gridPixelW + leftMargin, gridPixelH + 1, RenderUtil.withAlpha(Theme.BACKGROUND, translucentBackground.get() ? 100 : 255));

        Region[] regions = Region.values();
        for (int i = 0; i < regions.length; i++) {
            Region region = regions[i];
            RenderUtil.leftText(graphics, leftMargin - 2, 4 + i * 12, region.getDisplayName(), REGION_COLORS.get(region), false);
        }

        if (currentGoliath != null) {
            RenderUtil.leftText(graphics, leftMargin - 2, gridPixelH - 10, "#" + currentGoliath.number, Theme.FOREGROUND, false);
            RenderUtil.leftText(graphics, leftMargin - 2, gridPixelH - 10 - 12, currentGoliath.region.getDisplayName(), Theme.FOREGROUND, false);
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(leftMargin, 0f);

        for (Goliath g : goliaths) {
            int x1 = OUTLINE + g.col() * cellOuter;
            int y1 = OUTLINE + g.row() * cellOuter;
            int x2 = x1 + renderWidth;
            int y2 = y1 + renderWidth;

            int color = REGION_COLORS.get(g.region());
            graphics.fill(x1, y1, x2, y2, color);

            String text = String.valueOf(g.number());
            graphics.pose().pushMatrix();
            graphics.pose().translate(
                    x1 + renderWidth / 2f,
                    y1 + 4f
            );
            graphics.pose().scale(0.5f, 0.5f);
            graphics.centeredText(
                    Minecraft.getInstance().font,
                    text,
                    0,
                    0,
                    Theme.FOREGROUND
            );
            graphics.pose().popMatrix();
        }

        if (currentGoliath != null) {
            int col = currentGoliath.col();
            int row = currentGoliath.row();

            float cellX = OUTLINE + col * cellOuter;
            float cellY = OUTLINE + row * cellOuter;

            double goliathOriginX = col * goliathBlockWidth - 225_000.0;
            double goliathOriginZ = row * goliathBlockWidth - 225_000.0;
            float fracX = (float) ((playerX - goliathOriginX) / goliathBlockWidth);
            float fracZ = (float) ((playerZ - goliathOriginZ) / goliathBlockWidth);

            float dartX = cellX + fracX * renderWidth;
            float dartY = cellY + fracZ * renderWidth;

            int dartSize = 8;

            LocalPlayer pl = Minecraft.getInstance().player;
            float yaw = pl != null ? pl.getYRot() : 0f;

            graphics.pose().pushMatrix();
            graphics.pose().translate(dartX, dartY);
            graphics.pose().rotate((float) Math.toRadians(yaw));
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    dartImage,
                    -dartSize / 2, -dartSize / 2,  // offset so rotation is around center
                    0f, 0f,
                    dartSize, dartSize,
                    32, 32,
                    32, 32
            );
            graphics.pose().popMatrix();
        }

        graphics.pose().popMatrix();
    }

    SettingCategory mainCategory = SettingCategory.createDefault();

    BoolSetting translucentBackground = new BoolSetting.Builder()
            .name("translucent-background")
            .description("Render the map background as translucent")
            .defaultValue(true)
            .build();

    public GoliathMapHudElement() {
        super();
        mainCategory.add(translucentBackground);

        addSettingCategory(mainCategory);
    }

    public static Goliath getGoliathAt(double playerX, double playerZ) {
        int col = (int) Math.floor((playerX + 225000) / goliathBlockWidth);
        int row = (int) Math.floor((playerZ + 225000) / goliathBlockWidth);

        return goliaths.stream()
                .filter(g -> g.row() == row && g.col() == col)
                .findFirst()
                .orElse(null);
    }
}
