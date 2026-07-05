package me.numenmc.pedrohack.systems.modules.misc;

import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import me.numenmc.pedrohack.systems.settings.StringSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public class NameProtect extends Module {
    SettingCategory mainCategory = SettingCategory.createDefault();

    public BoolSetting reloadScoreboardTitle = new BoolSetting.Builder()
            .name("mutate-scoreboard-title")
            .description("Reload the scoreboard title on enable/disable. May break formatting.")
            .defaultValue(true)
            .onChange(v -> refreshScoreboard())
            .build();

    public StringSetting replacementName = new StringSetting.Builder()
            .name("replacement-name")
            .description("The name to replace your real IGN with.")
            .defaultValue("archivePedro")
            .maxLength(16)
            .onChange(v -> { if (reloadScoreboardTitle.get()) refreshScoreboard(); })
            .build();

    public BoolSetting replaceUsernameText = new BoolSetting.Builder()
            .name("hide-username")
            .description("Replace all occurrences of the username with the new one.")
            .defaultValue(true)
            .onChange(v -> { if (reloadScoreboardTitle.get()) refreshScoreboard(); })
            .build();

    public BoolSetting mutateClientSkin = new BoolSetting.Builder()
            .name("mutate-client-skin")
            .description("Will the client's skin be overridden?")
            .defaultValue(true)
            .build();

    public NameProtect() {
        super("name-protect", "Prevent the game from displaying your actual game profile.");

        mainCategory.add(replacementName);
        mainCategory.add(replaceUsernameText);
        mainCategory.add(mutateClientSkin);
        mainCategory.add(reloadScoreboardTitle);

        addSettingCategory(mainCategory);
    }

    public String replaceName(String string) {
        if (string != null && this.isEnabled() && this.replaceUsernameText.get()) {
            return string.replace(Minecraft.getInstance().getUser().getName(), replacementName.get());
        }

        return string;
    }

    private void refreshScoreboard() {
        if (!reloadScoreboardTitle.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Scoreboard scoreboard = mc.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);

        if (objective == null) return;

        objective.setDisplayName(Component.literal(objective.getDisplayName().getString()));
    }

    @Override
    protected void onEnable() {
        refreshScoreboard();
    }

    @Override
    protected void onDisable() {
        refreshScoreboard();
    }
}
