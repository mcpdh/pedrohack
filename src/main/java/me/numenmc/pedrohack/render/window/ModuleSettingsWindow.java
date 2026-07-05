package me.numenmc.pedrohack.render.window;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import me.numenmc.pedrohack.imgui.RenderInterface;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.Setting;
import me.numenmc.pedrohack.systems.SettingCategory;

public class ModuleSettingsWindow implements RenderInterface {
    private final Module module;

    public ModuleSettingsWindow(Module module) {
        this.module = module;
    }

    @Override
    public void render(ImGuiIO io) {
        if (ImGui.begin(module.getDisplayName() + "##settings-" + module.getName())) {

            for (SettingCategory category : module.getSettingCategories()) {

                if (ImGui.treeNodeEx(category.getName(),
                        ImGuiTreeNodeFlags.Framed | ImGuiTreeNodeFlags.DefaultOpen)) {

                    if (ImGui.beginTable("SettingsTable##" + module.getName() + "-" + category.getName(), 3)) {
                        ImGui.tableSetupColumn("Label", ImGuiTableColumnFlags.WidthStretch);
                        ImGui.tableSetupColumn("Setting", ImGuiTableColumnFlags.WidthStretch);
                        ImGui.tableSetupColumn("Reset", ImGuiTableColumnFlags.WidthFixed, 40);

                        for (Setting<?> setting : category.getSettings()) {
                            ImGui.tableNextRow();

                            ImGui.tableSetColumnIndex(0);
                            ImGui.setNextItemWidth(ImGui.getColumnWidth());
                            ImGui.text(setting.getDisplayName());

                            ImGui.tableSetColumnIndex(1);
                            setting.render(io, module);

                            ImGui.tableSetColumnIndex(2);
                            if (ImGui.button("Rs##" + module.getName() + setting.getName())) {
                                setting.reset();
                            }
                        }

                        ImGui.endTable();
                    }

                    ImGui.treePop();
                }
            }

            ImGui.end();
        }
    }
}
