package me.numenmc.pedrohack.imgui;

import imgui.ImGuiIO;

@FunctionalInterface
public interface RenderInterface {
    void render(final ImGuiIO io);
}
