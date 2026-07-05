package me.numenmc.pedrohack.render.world;

public class Color {
    public int r, g, b, a;

    public Color(int r, int g, int b, int a) {
        this.r = r; this.g = g; this.b = b; this.a = a;
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public int toARGB() {
        return ((a & 0xFF) << 24)
                | ((r & 0xFF) << 16)
                | ((g & 0xFF) << 8)
                | (b & 0xFF);
    }
}
