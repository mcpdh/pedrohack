package me.numenmc.pedrohack.systems.modules.misc;

import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import me.numenmc.pedrohack.systems.settings.StringSetting;

public class SpoofStats extends Module {
    SettingCategory mainCategory = SettingCategory.createDefault();

    public BoolSetting overrideMoney = new BoolSetting.Builder()
            .name("override-money")
            .description("Override the scoreboard money counter")
            .defaultValue(true)
            .build();
    public StringSetting overrideMoneyValue = new StringSetting.Builder()
            .name("money-value")
            .description("Replacement for scoreboard money counter")
            .defaultValue("1.4T")
            .build();

    public BoolSetting overrideShards = new BoolSetting.Builder()
            .name("override-shards")
            .description("Override the scoreboard shards counter")
            .defaultValue(true)
            .build();
    public StringSetting overrideShardsValue = new StringSetting.Builder()
            .name("shards-value")
            .description("Replacement for scoreboard shards counter")
            .defaultValue("122K")
            .build();

    public BoolSetting overrideKills = new BoolSetting.Builder()
            .name("override-kills")
            .description("Override the scoreboard kills counter")
            .defaultValue(true)
            .build();
    public StringSetting overrideKillsValue = new StringSetting.Builder()
            .name("kills-value")
            .description("Replacement for scoreboard kills counter")
            .defaultValue("2399")
            .build();

    public BoolSetting overrideDeaths = new BoolSetting.Builder()
            .name("override-deaths")
            .description("Override the scoreboard deaths counter")
            .defaultValue(true)
            .build();
    public StringSetting overrideDeathsValue = new StringSetting.Builder()
            .name("deaths-value")
            .description("Replacement for scoreboard deaths counter")
            .defaultValue("942")
            .build();

    public BoolSetting overridePlaytime = new BoolSetting.Builder()
            .name("override-playtime")
            .description("Override the scoreboard playtime counter")
            .defaultValue(true)
            .build();
    public StringSetting overridePlaytimeValue = new StringSetting.Builder()
            .name("playtime-value")
            .description("Replacement for scoreboard playtime counter")
            .defaultValue("189d 13h")
            .build();

    SettingCategory tablistCategory = new SettingCategory("Tablist");

    public BoolSetting overrideTablist = new BoolSetting.Builder()
            .name("override-tablist-money")
            .description("Override the tablist money counter.")
            .defaultValue(false)
            .build();

    public SpoofStats() {
        super("spoof-stats", "Replace DonutSMP scoreboard information.");

        mainCategory.add(overrideMoney);
        mainCategory.add(overrideMoneyValue);

        mainCategory.add(overrideShards);
        mainCategory.add(overrideShardsValue);

        mainCategory.add(overrideKills);
        mainCategory.add(overrideKillsValue);

        mainCategory.add(overrideDeaths);
        mainCategory.add(overrideDeathsValue);

        mainCategory.add(overridePlaytime);
        mainCategory.add(overridePlaytimeValue);

        tablistCategory.add(overrideTablist);

        addSettingCategory(mainCategory);
        addSettingCategory(tablistCategory);
    }
}
