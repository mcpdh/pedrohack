package me.numenmc.pedrohack.systems.hud;

import me.numenmc.pedrohack.render.Theme;
import me.numenmc.pedrohack.systems.Categories;
import me.numenmc.pedrohack.systems.HudElement;
import me.numenmc.pedrohack.systems.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class EnabledModulesHudElement extends HudElement {

    private record Entry(Module module, Component text, int width) {}

    private final List<Entry> cached = new ArrayList<>();
    private String cacheKey = "";
    private int cachedMaxWidth = 0;

    private void rebuildCache() {
        Font font = Minecraft.getInstance().font;

        String newKey = Categories.getAllTogglableModules().stream()
                .map(m -> m.getDisplayName() + ":" + m.isEnabled())
                .reduce("", (a, b) -> a + "|" + b);

        if (Objects.equals(cacheKey, newKey)) {
            return;
        }

        cacheKey = newKey;

        cached.clear();
        cachedMaxWidth = 0;

        for (Module module : Categories.getAllTogglableModules()) {
            if (!module.isEnabled()) continue;

            Component text = Theme.Font(module.getDisplayName());
            int width = font.width(text);

            cached.add(new Entry(module, text, width));
            cachedMaxWidth = Math.max(cachedMaxWidth, width);
        }

        switch (getVerticalAnchor()) {
            case CENTER ->
                    cached.sort(Comparator.comparing(
                            e -> e.module().getDisplayName(),
                            String.CASE_INSENSITIVE_ORDER
                    ));

            case TOP ->
                    cached.sort(Comparator.comparingInt(Entry::width).reversed());

            case BOTTOM ->
                    cached.sort(Comparator.comparingInt(Entry::width));
        }
    }

    @Override
    public int getWidth() {
        rebuildCache();
        return cachedMaxWidth;
    }

    @Override
    public int getHeight() {
        rebuildCache();
        return cached.size() * 12;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        rebuildCache();

        int y = 0;

        for (Entry entry : cached) {
            int x = switch (getHorizontalAnchor()) {
                case LEFT -> 0;

                case CENTER ->
                        (cachedMaxWidth - entry.width()) / 2;

                case RIGHT ->
                        cachedMaxWidth - entry.width();
            };

            graphics.text(
                    Minecraft.getInstance().font,
                    entry.text(),
                    x,
                    y,
                    Theme.FOREGROUND,
                    true
            );

            y += 12;
        }
    }
}
