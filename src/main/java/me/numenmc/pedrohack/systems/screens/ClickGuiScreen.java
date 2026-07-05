package me.numenmc.pedrohack.systems.screens;

import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import me.numenmc.pedrohack.imgui.RenderInterface;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImBoolean;
import me.numenmc.pedrohack.render.window.ModuleSettingsWindow;
import me.numenmc.pedrohack.systems.Categories;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.ModuleCategory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;

public final class ClickGuiScreen extends Screen implements RenderInterface {
    private final Set<Module> openSettings = new HashSet<>();

    public ClickGuiScreen() {
        super(Component.literal("ClickGuiScreen"));
    }

    @Override
    public void render(ImGuiIO io) {
        if (ImGui.beginMainMenuBar()) {

            if (ImGui.beginMenu("Modules")) {
                ImGui.menuItem("Click GUI");
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Config")) {
                if (ImGui.menuItem("Save")) {
                    // ...
                }

                if (ImGui.menuItem("Load")) {
                    // ...
                }

                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Help")) {
                ImGui.menuItem("About");
                ImGui.endMenu();
            }

            ImGui.endMainMenuBar();
        }

        if (ImGui.begin("Modules")) {
            ImGui.setWindowSize(300, 600);

            for (ModuleCategory category : Categories.values()) {
                if (ImGui.treeNodeEx(category.getName(), ImGuiTreeNodeFlags.Framed)) {
                    ImGui.beginTable("ModulesTable-" + category.getName(), 2);

                    ImGui.tableSetupColumn("Module", ImGuiTableColumnFlags.WidthStretch);
                    ImGui.tableSetupColumn("Cfg", ImGuiTableColumnFlags.WidthFixed, 40);

                    for (Module module : category.getModules()) {
                        ImGui.tableNextRow();

                        ImGui.tableSetColumnIndex(0);
                        if (ImGui.checkbox(module.getDisplayName(), module.isEnabled())) {
                            module.toggle();
                        }

                        ImGui.tableSetColumnIndex(1);
                        if (ImGui.button("Cfg##" + module.getDisplayName())) {
                            if (openSettings.contains(module)) {
                                openSettings.remove(module);
                            } else {
                                openSettings.add(module);
                            }
                        }
                    }

                    ImGui.endTable();
                    ImGui.treePop();
                }
            }
        }
        ImGui.end();

        for (Module module : openSettings) {
            ModuleSettingsWindow window = module.getSettingsWindow();
            window.render(io);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
