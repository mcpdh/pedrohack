package me.numenmc.pedrohack.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.numenmc.pedrohack.render.RenderUtil;
import me.numenmc.pedrohack.systems.Categories;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin {
    @ModifyReturnValue(
            method = "formatNameForTeam",
            at = @At("RETURN")
    )
    private static MutableComponent modifyFormattedName(MutableComponent result) {
        if (!Categories.MISC.SPOOF_STATS.isEnabled()) return result;

        String asText = result.getString();

        if (asText.contains("$") && Categories.MISC.SPOOF_STATS.overrideMoney.get()) {
            return constructNew("$", RenderUtil.withAlpha(0x00FF00, 255), Categories.MISC.SPOOF_STATS.overrideMoneyValue.get());
        }

        if (asText.contains("★") && Categories.MISC.SPOOF_STATS.overrideShards.get()) {
            return constructNew("★", RenderUtil.withAlpha(0xA503FC, 255), Categories.MISC.SPOOF_STATS.overrideShardsValue.get());
        }

        if (asText.contains("🗡") && Categories.MISC.SPOOF_STATS.overrideKills.get()) {
            return constructNew("🗡", RenderUtil.withAlpha(0xFF0000, 255), Categories.MISC.SPOOF_STATS.overrideKillsValue.get());
        }

        if (asText.contains("☠") && Categories.MISC.SPOOF_STATS.overrideDeaths.get()) {
            return constructNew("☠", RenderUtil.withAlpha(0xFC7703, 255), Categories.MISC.SPOOF_STATS.overrideDeathsValue.get());
        }

        if (asText.contains("⌚") && Categories.MISC.SPOOF_STATS.overridePlaytime.get()) {
            return constructNew("⌚", RenderUtil.withAlpha(0xFFE600, 255), Categories.MISC.SPOOF_STATS.overridePlaytimeValue.get());
        }

        return result;
    }

    @Unique
    private static MutableComponent constructNew(String symbol, int color, String data) {
        return Component.empty()
                .append(Component.literal(symbol + " ").withColor(color))
                .append(Component.literal(data));
    }
}
