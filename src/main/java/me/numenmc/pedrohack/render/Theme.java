package me.numenmc.pedrohack.render;

import me.numenmc.pedrohack.Pedrohack;
import me.numenmc.pedrohack.systems.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public class Theme {
    public enum Fonts {
        LATO("lato", "Lato"),
        COMFORTAA("comfortaa", "Comfortaa"),
        ARIMO("arimo", "Arimo"),
        JETBRAINS_MONO("jetbrainsmono", "JetBrains Mono");

        private final String id;
        private final String name;

        Fonts(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    private static FontDescription getFont() {
        return new FontDescription.Resource(Identifier.fromNamespaceAndPath(Pedrohack.id, Config.APPEARANCE.uiFont.get().getId()));
    }

    public static Component Font(String text) {
        return Component.literal(text).withStyle(style -> style.withFont(getFont()));
    }

    public static Component Font(Component text) {
        return (Component.empty().append(text)).withStyle(style -> style.withFont(getFont()));
    }

    public static final int PRIMARY = 0xFF960909;

    public static final int PRIMARY_DARKEN_1 = 0xFF7A0808;
    public static final int PRIMARY_DARKEN_2 = 0xFF5E0606;

    public static final int PRIMARY_LIGHTEN_1 = 0xFFB32828;
    public static final int PRIMARY_LIGHTEN_2 = 0xFFD24A4A;

    public static final int SECONDARY = 0xFF1F1F1F;

    public static final int FOREGROUND = 0xFFFFFFFF;

    public static final int BACKGROUND = 0xFF121212;

    public static class Icons {
        public static Identifier MDI_CUBE = Identifier.fromNamespaceAndPath(Pedrohack.id, "textures/gui/icons/cube.png");
        public static Identifier MDI_COG = Identifier.fromNamespaceAndPath(Pedrohack.id, "textures/gui/icons/cog.png");
        public static Identifier MDI_MONITOR_SMALL = Identifier.fromNamespaceAndPath(Pedrohack.id, "textures/gui/icons/monitor-small.png");
        public static Identifier MDI_ROBOT = Identifier.fromNamespaceAndPath(Pedrohack.id, "textures/gui/icons/robot.png");
        public static Identifier MDI_SHAPE_PLUS = Identifier.fromNamespaceAndPath(Pedrohack.id, "textures/gui/icons/shape-plus.png");
        public static Identifier SM_MDI_WRENCH = Identifier.fromNamespaceAndPath(Pedrohack.id, "textures/gui/icons/sm_wrench.png");
    }

    public static final double SCROLL_FACTOR = 10d;
}
