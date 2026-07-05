package me.numenmc.pedrohack.systems;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

public abstract class HudElement {
    public enum HorizontalAnchor { LEFT, CENTER, RIGHT }
    public enum VerticalAnchor   { TOP, CENTER, BOTTOM }

    // Fixed pixel offset from the anchor point
    private float x;
    private float y;
    private HorizontalAnchor horizontalAnchor;
    private VerticalAnchor   verticalAnchor;

    private final List<SettingCategory> settingCategories = new ArrayList<>();

    // Add this method
    protected void addSettingCategory(SettingCategory category) {
        settingCategories.add(category);
    }

    public List<SettingCategory> getSettingCategories() {
        return settingCategories;
    }

    public HudElement() {
        this.x = 0;
        this.y = 0;
        this.horizontalAnchor = HorizontalAnchor.LEFT;
        this.verticalAnchor   = VerticalAnchor.TOP;
    }

    public int resolvedX(int x1, int x2) {
        int areaWidth = x2 - x1;

        int baseX = switch (horizontalAnchor) {
            case LEFT   -> x1;
            case CENTER -> x1 + areaWidth / 2 - getWidth() / 2;
            case RIGHT  -> x2 - getWidth();
        };

        return baseX + (int) x;
    }

    public int resolvedY(int y1, int y2) {
        int areaHeight = y2 - y1;

        int baseY = switch (verticalAnchor) {
            case TOP    -> y1;
            case CENTER -> y1 + areaHeight / 2 - getHeight() / 2;
            case BOTTOM -> y2 - getHeight();
        };

        return baseY + (int) y;
    }

    public void moveTo(float screenX, float screenY, int x1, int y1, int x2, int y2) {
        int areaWidth  = x2 - x1;
        int areaHeight = y2 - y1;

        float localX = screenX - x1;
        float localY = screenY - y1;

        if (localX < areaWidth / 3f) {
            horizontalAnchor = HorizontalAnchor.LEFT;
        } else if (localX < areaWidth * 2f / 3f) {
            horizontalAnchor = HorizontalAnchor.CENTER;
        } else {
            horizontalAnchor = HorizontalAnchor.RIGHT;
        }

        if (localY < areaHeight / 3f) {
            verticalAnchor = VerticalAnchor.TOP;
        } else if (localY < areaHeight * 2f / 3f) {
            verticalAnchor = VerticalAnchor.CENTER;
        } else {
            verticalAnchor = VerticalAnchor.BOTTOM;
        }

        float baseX = switch (horizontalAnchor) {
            case LEFT   -> x1;
            case CENTER -> x1 + areaWidth / 2f - getWidth() / 2f;
            case RIGHT  -> x2 - getWidth();
        };

        float baseY = switch (verticalAnchor) {
            case TOP    -> y1;
            case CENTER -> y1 + areaHeight / 2f - getHeight() / 2f;
            case BOTTOM -> y2 - getHeight();
        };

        this.x = screenX - baseX;
        this.y = screenY - baseY;
    }

    public abstract int getWidth();
    public abstract int getHeight();
    public abstract void render(GuiGraphicsExtractor graphics);

    public float getX() { return x; }
    public float getY() { return y; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public HorizontalAnchor getHorizontalAnchor() { return horizontalAnchor; }
    public VerticalAnchor   getVerticalAnchor()   { return verticalAnchor; }
    public void setHorizontalAnchor(HorizontalAnchor anchor) { this.horizontalAnchor = anchor; }
    public void setVerticalAnchor(VerticalAnchor anchor)     { this.verticalAnchor   = anchor; }

//    protected boolean inEditor() {
//        return Minecraft.getInstance().gui.screen() instanceof HudEditScreen;
//    }

    public JsonObject serialize() {
        JsonObject root = new JsonObject();

        root.addProperty("x", getX());
        root.addProperty("y", getY());
        root.addProperty("h", getHorizontalAnchor().name());
        root.addProperty("v", getVerticalAnchor().name());

        JsonObject settingsRoot = new JsonObject();

        for (SettingCategory category : getSettingCategories()) {
            for (Setting<?> setting : category.getSettings()) {
                settingsRoot.add(setting.getName(), setting.serialize());
            }
        }

        root.add("settings", settingsRoot);
        return root;
    }

    public void deserialize(JsonObject root) {
        if (root.has("x")) {
            try {
                setX(root.get("x").getAsInt());
            } catch (Exception ignored) {}
        }

        if (root.has("y")) {
            try {
                setY(root.get("y").getAsInt());
            } catch (Exception ignored) {}
        }

        if (root.has("h")) {
            try {
                setHorizontalAnchor(
                        HorizontalAnchor.valueOf(root.get("h").getAsString())
                );
            } catch (Exception ignored) {}
        }

        if (root.has("v")) {
            try {
                setVerticalAnchor(
                        VerticalAnchor.valueOf(root.get("v").getAsString())
                );
            } catch (Exception ignored) {}
        }

        if (root.has("settings")) {
            JsonObject settingsRoot = root.getAsJsonObject("settings");

            for (SettingCategory category : getSettingCategories()) {
                for (Setting<?> setting : category.getSettings()) {
                    if (settingsRoot.has(setting.getName())) {
                        try {
                            setting.deserialize(settingsRoot.get(setting.getName()));
                        } catch (Exception ignored) {
                            // corrupt value, skip
                        }
                    }
                }
            }
        }
    }
}
