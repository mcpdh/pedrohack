package me.numenmc.pedrohack.systems.hud;

import me.numenmc.pedrohack.Pedrohack;
import me.numenmc.pedrohack.render.Theme;
import me.numenmc.pedrohack.systems.HudElement;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class BrandingHudElement extends HudElement {
    private final Component text = Theme.Font("Pedrohack 1.0 Minecraft 26.2");
    private final Identifier logo = Identifier.fromNamespaceAndPath(Pedrohack.id, "textures/gui/pedrohack.png");
    private final int logoWidth  = 144;
    private final int logoHeight = 81;

    SettingCategory mainCategory = SettingCategory.createDefault();
    BoolSetting showLogo = new BoolSetting.Builder()
            .name("show-logo")
            .description("Should the branding logo be visible?")
            .defaultValue(true)
            .build();

    public BrandingHudElement() {
        super();
        mainCategory.add(showLogo);
        addSettingCategory(mainCategory);
    }

    @Override
    public int getWidth() {
        return Math.max(Minecraft.getInstance().font.width(text), logoWidth);
    }

    @Override
    public int getHeight() {
        return showLogo.get() ? 12 + logoHeight : 10;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        int textWidth  = mc.font.width(text);
        int totalWidth = getWidth();

        int textX = 0;
        int logoX = 0;

        switch (getHorizontalAnchor()) {
            case CENTER -> {
                textX = (totalWidth - textWidth)  / 2;
                logoX = (totalWidth - logoWidth)  / 2;
            }
            case RIGHT -> {
                textX = totalWidth - textWidth;
                logoX = totalWidth - logoWidth;
            }
            case LEFT -> {}
        }

        graphics.text(mc.font, text, textX, 0, Theme.FOREGROUND, true);

        if (showLogo.get()) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    logo,
                    logoX, 12,    // draw position
                    0f, 0f,       // u, v
                    logoWidth, logoHeight,   // draw size (144x81)
                    359, 201,     // src region to sample from texture
                    359, 201      // full texture size
            );
        }
    }
}
