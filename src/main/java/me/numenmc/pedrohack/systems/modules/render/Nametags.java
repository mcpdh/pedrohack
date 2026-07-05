package me.numenmc.pedrohack.systems.modules.render;

import me.numenmc.pedrohack.render.RenderUtil;
import me.numenmc.pedrohack.render.Theme;
import me.numenmc.pedrohack.systems.Categories;
import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.config.Config;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.Render2DEvent;
import me.numenmc.pedrohack.systems.settings.BoolSetting;
import me.numenmc.pedrohack.util.EntityUtils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Nametags extends Module {
    SettingCategory mainCategory = SettingCategory.createDefault();
    SettingCategory displayCategory = new SettingCategory("Display");
    SettingCategory infoCategory = new SettingCategory("Info");

    BoolSetting renderPlayers = new BoolSetting.Builder()
            .name("render-player-nametags")
            .description("Should vanilla player nametags be replaced?")
            .defaultValue(true)
            .build();

    BoolSetting renderItems = new BoolSetting.Builder()
            .name("render-item-nametags")
            .description("Should items display nametags?")
            .defaultValue(false)
            .build();

    BoolSetting renderSelf = new BoolSetting.Builder()
            .name("render-self")
            .description("Render your own nametag")
            .defaultValue(true)
            .build();

    BoolSetting multiline = new BoolSetting.Builder()
            .name("multiline")
            .description("Show info on a second line instead of inline.")
            .defaultValue(false)
            .build();

    BoolSetting showName = new BoolSetting.Builder()
            .name("show-name")
            .description("Show the entity's name.")
            .defaultValue(true)
            .build();

    BoolSetting showHealth = new BoolSetting.Builder()
            .name("show-health")
            .description("Show current health and absorption.")
            .defaultValue(false)
            .build();

    BoolSetting showGamemode = new BoolSetting.Builder()
            .name("show-gamemode")
            .description("Show the player's gamemode.")
            .defaultValue(false)
            .build();

    BoolSetting showDistance = new BoolSetting.Builder()
            .name("show-distance")
            .description("Show distance from you to the entity.")
            .defaultValue(true)
            .build();

    BoolSetting showLatency = new BoolSetting.Builder()
            .name("show-latency")
            .description("Show the player's ping in milliseconds.")
            .defaultValue(false)
            .build();

    // NEW: equipment setting
    BoolSetting showEquipment = new BoolSetting.Builder()
            .name("show-equipment")
            .description("Show the player's armor, mainhand, and offhand above their nametag.")
            .defaultValue(false)
            .build();

    public Nametags() {
        super("nametags", "Render information above entities.");

        mainCategory.add(renderPlayers);
        mainCategory.add(renderItems);
        mainCategory.add(renderSelf);

        displayCategory.add(multiline);

        infoCategory.add(showName);
        infoCategory.add(showHealth);
        infoCategory.add(showGamemode);
        infoCategory.add(showDistance);
        infoCategory.add(showLatency);
        infoCategory.add(showEquipment); // NEW

        addSettingCategory(mainCategory);
        addSettingCategory(displayCategory);
        addSettingCategory(infoCategory);
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        for (Entity entity : level.entitiesForRendering()) {
            boolean isPlayer = entity instanceof Player;
            boolean isItem = entity instanceof ItemEntity;

            if (!((renderPlayers.get() && isPlayer) || (renderItems.get() && isItem))) {
                continue;
            }

            if (isPlayer && entity == Minecraft.getInstance().player) {
                boolean freecam = Categories.PLAYER.FREECAM.isEnabled();
                boolean notFirstPerson =
                        Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON;

                if (!renderSelf.get() || (!freecam && !notFirstPerson)) {
                    continue;
                }
            }

            float partialTick = event.delta.getGameTimeDeltaPartialTick(true);
            double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
            double y = Mth.lerp(partialTick, entity.yOld, entity.getY());
            double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());

            Vec3 worldPos = new Vec3(x, y, z).add(0, entity.getBbHeight() + 0.5, 0);
            Vec3 screen = RenderUtil.to2D((float) worldPos.x, (float) worldPos.y, (float) worldPos.z);

            if (screen == null) continue;

            if (entity instanceof Player player) {
                renderPlayerNametag(event.graphics, player, screen.x, screen.y);
            } else if (entity instanceof ItemEntity item) {
                renderItemNametag(event.graphics, item, screen.x, screen.y);
            }
        }
    }

    private void renderPlayerNametag(
            GuiGraphicsExtractor graphics,
            Player player,
            double x,
            double y
    ) {
        var mc = Minecraft.getInstance();
        var font = mc.font;

        Component topLine = Theme.Font(buildTopLine(player));
        Component bottomLine = Theme.Font(buildBottomLine(player, mc));

        var pose = graphics.pose();

        int paddingX = 3;
        int paddingY = 2;
        int lineHeight = font.lineHeight;
        int singleHeight = (multiline.get() ? lineHeight * 2 : lineHeight) + paddingY;

        // Collect equipment items to know how tall the equipment row will be
        List<ItemStack> equipment = showEquipment.get() ? collectEquipment(player) : List.of();
        int itemSize = 18;
        int equipmentRowHeight = equipment.isEmpty() ? 0 : itemSize + 2; // 2px gap below items

        pose.pushMatrix();
        // Shift the whole nametag up by the equipment row height so items sit above it
        pose.translate((float) x, (float) (y - singleHeight - equipmentRowHeight));

        // --- Draw equipment row above the nametag background ---
        if (!equipment.isEmpty()) {
            int totalItemWidth = equipment.size() * itemSize;
            int itemStartX = -totalItemWidth / 2;

            for (int i = 0; i < equipment.size(); i++) {
                graphics.item(
                        equipment.get(i),
                        itemStartX + i * itemSize,
                        -(itemSize + paddingY)
                );

                if (equipment.get(i).count() > 1) {
                    Component countText = Theme.Font(String.valueOf(equipment.get(i).count()));
                    int countTextWidth = Minecraft.getInstance().font.width(countText);

                    graphics.text(
                            Minecraft.getInstance().font,
                            countText,
                            itemStartX + i * itemSize + itemSize - countTextWidth - 2,
                            -(itemSize + paddingY) + 8,
                            Theme.FOREGROUND
                    );
                }

                if (equipment.get(i).isDamageableItem()) {
                    int maxDamage = equipment.get(i).getMaxDamage();
                    int damage = equipment.get(i).getDamageValue();
                    int percent = Math.round((1f - (float) damage / maxDamage) * 100f);

                    int color = percent > 75 ? 0x00FF00 : percent > 50 ? 0xFFFF00 : percent > 25 ? 0xFF8800 : 0xFF0000;

                    graphics.centeredText(
                            Minecraft.getInstance().font,
                            Theme.Font(String.valueOf(percent)),
                            itemStartX + i * itemSize + itemSize / 2 - 2,
                            -(itemSize + paddingY) - 8,
                            RenderUtil.withAlpha(color, 255)
                    );
                }
            }
        }

        // --- Draw the nametag text box (unchanged logic below) ---
        if (!multiline.get()) {
            Component single = Component.empty().append(topLine);

            if (!bottomLine.getString().isEmpty()) {
                single = Component.empty()
                        .append(single)
                        .append(" ")
                        .append(bottomLine);
            }

            single = Theme.Font(single);

            int width = font.width(single);

            RenderUtil.fillRoundedBoth(
                    graphics,
                    -width / 2 - paddingX,
                    -paddingY,
                    width / 2 + paddingX,
                    lineHeight + paddingY,
                    3,
                    RenderUtil.withAlpha(Theme.BACKGROUND, 150),
                    Theme.BACKGROUND
            );

            graphics.centeredText(font, single, 0, 0, Theme.FOREGROUND);

            pose.popMatrix();
            return;
        }

        int topWidth = font.width(topLine);
        int bottomWidth = font.width(bottomLine);
        int maxWidth = Math.max(topWidth, bottomWidth);

        RenderUtil.fillRoundedBoth(
                graphics,
                -maxWidth / 2 - paddingX,
                -paddingY,
                maxWidth / 2 + paddingX,
                (lineHeight * 2) + paddingY,
                3,
                RenderUtil.withAlpha(Theme.BACKGROUND, 150),
                Theme.BACKGROUND
        );

        graphics.centeredText(font, topLine, 0, 0, Theme.FOREGROUND);

        if (!bottomLine.getString().isEmpty()) {
            graphics.centeredText(font, bottomLine, 0, lineHeight, Theme.FOREGROUND);
        }

        pose.popMatrix();
    }

    /**
     * Collects non-empty equipment stacks in display order:
     * Helmet → Chestplate → Leggings → Boots → Mainhand → Offhand
     */
    private List<ItemStack> collectEquipment(Player player) {
        List<ItemStack> items = new ArrayList<>();

        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        }) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) items.add(stack);
        }

        ItemStack mainhand = player.getMainHandItem();
        ItemStack offhand = player.getOffhandItem();
        if (!mainhand.isEmpty()) items.add(mainhand);
        if (!offhand.isEmpty()) items.add(offhand);

        return items;
    }

    private Component buildTopLine(Player player) {
        MutableComponent namePerf = Component.literal(Categories.MISC.NAME_PROTECT.replaceName(player.getName().getString()));

        if (player.isCrouching()) {
            namePerf.withColor(0xFF8800);
        } else if (Config.PLAYER_LISTS.friends.get().contains(player.getName().getString())) {
            namePerf.withColor(0x03FCEC);
        }

        Component name = showName.get() ? namePerf : Component.empty();

        int hp = Math.round(player.getHealth() + player.getAbsorptionAmount());
        int color = hp > 18 ? 0x00FF00 : hp > 12 ? 0xFFFF00 : hp > 6 ? 0xFF8800 : 0xFF0000;

        Component health = showHealth.get()
                ? Component.literal(hp + "HP").withStyle(style -> style.withColor(RenderUtil.withAlpha(color, 255)))
                : Component.empty();

        Component gamemode = showGamemode.get()
                ? Component.literal("[").append(Component.literal(gamemodeAbbreviation(player.gameMode()))).append("]")
                : Component.empty();

        return joinComponents(name, health, gamemode);
    }

    private Component buildBottomLine(Player player, Minecraft mc) {
        if (!showDistance.get() && !showLatency.get()) return Component.empty();

        Component result = Component.empty();
        boolean first = true;

        if (showDistance.get() && mc.player != null) {
            int dist = (int) mc.player.distanceTo(player);
            result = Component.empty().append(result).append(Component.literal(String.valueOf(dist)).append("m"));
            first = false;
        }

        if (showLatency.get()) {
            int ping = EntityUtils.getPing(player);
            int color = ping < 35 ? 0x00FF00 : ping < 100 ? 0xFFFF00 : ping < 200 ? 0xFF8800 : 0xFF0000;

            if (ping >= 0) {
                if (!first) result = Component.empty().append(result).append(" ");
                result = Component.empty().append(result)
                        .append(Component.literal(String.valueOf(ping)).append("ms")
                                .withColor(RenderUtil.withAlpha(color, 255)));
            }
        }

        return result;
    }

    private String gamemodeAbbreviation(GameType gamemode) {
        if (gamemode == null) return "Na";
        return switch (gamemode) {
            case SURVIVAL -> "S";
            case CREATIVE -> "C";
            case ADVENTURE -> "A";
            case SPECTATOR -> "Sp";
        };
    }

    private Component joinComponents(Component... components) {
        Component result = Component.empty();

        for (Component c : components) {
            if (!c.getString().isEmpty()) {
                if (!result.getString().isEmpty()) result = Component.empty().append(result).append(" ");
                result = Component.empty().append(result).append(c);
            }
        }

        return result;
    }

    private void renderItemNametag(
            GuiGraphicsExtractor graphics,
            ItemEntity item,
            double x,
            double y
    ) {
        var font = Minecraft.getInstance().font;

        Component count = Component.literal("x" + item.getItem().getCount())
                .withColor(Theme.PRIMARY_LIGHTEN_1);

        Component text = Theme.Font(
                Component.empty()
                        .append(item.getName())
                        .append(" ")
                        .append(count)
        );

        var pose = graphics.pose();

        pose.pushMatrix();
        pose.translate((float) x, (float) y);

        int textWidth = font.width(text);
        int textHeight = font.lineHeight;
        int paddingX = 3;
        int paddingY = 2;
        int left = -textWidth / 2 - paddingX;
        int top = -paddingY;

        RenderUtil.fillRoundedBoth(
                graphics,
                left,
                top,
                left + textWidth + paddingX * 2,
                top + textHeight + paddingY * 2,
                3,
                RenderUtil.withAlpha(Theme.BACKGROUND, 100),
                Theme.BACKGROUND
        );

        graphics.centeredText(font, text, 0, 0, Theme.FOREGROUND);

        pose.popMatrix();
    }
}
