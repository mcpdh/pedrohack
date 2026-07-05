package me.numenmc.pedrohack.systems.modules.combat;

import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.SettingCategory;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.ScreenRenderEvent;
import me.numenmc.pedrohack.systems.settings.IntSetting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;

import java.util.Random;

public class InvTotem extends Module {

    private enum Stage {
        WAIT,
        EXECUTE
    }

    private Stage stage = Stage.WAIT;

    private long actionAt = 0;

    private int selectedSlot = -1;

    private boolean done = false;

    private final Random random = new Random();

    SettingCategory mainCategory = SettingCategory.createDefault();

    IntSetting minWaitMs = new IntSetting.Builder()
            .name("min-wait-ms")
            .min(0)
            .max(3000)
            .defaultValue(220)
            .build();

    IntSetting maxWaitMs = new IntSetting.Builder()
            .name("max-wait-ms")
            .min(0)
            .max(3000)
            .defaultValue(360)
            .build();

    public InvTotem() {
        super(
                "inv-totem",
                "Swap totems into off hand when inventory opens.",
                true
        );

        mainCategory.add(minWaitMs);
        mainCategory.add(maxWaitMs);

        addSettingCategory(mainCategory);
    }

    @EventHandler
    public void onScreenRender(ScreenRenderEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.gameMode == null)
            return;

        if (!(event.screen instanceof InventoryScreen)) {
            reset();
            return;
        }

        if (done && mc.player.getOffhandItem().isEmpty()) {
            reset();
        }

        if (done)
            return;

        long now = System.nanoTime();

        switch (stage) {

            case WAIT -> {
                if (actionAt == 0) {
                    actionAt = now + ms(nextDelay(minWaitMs, maxWaitMs));
                }

                if (now < actionAt)
                    return;

                int invIndex = findTotem(mc);

                if (invIndex == -1) {
                    done = true;
                    return;
                }

                selectedSlot = inventoryIndexToSlotId(invIndex);

                if (selectedSlot == -1) {
                    done = true;
                    return;
                }

                click(mc, selectedSlot);

                stage = Stage.EXECUTE;
                actionAt = now + ms(60); // fixed short settle delay
            }

            case EXECUTE -> {
                if (now < actionAt)
                    return;

                if (mc.player.containerMenu.getCarried().isEmpty()) {
                    reset();
                    return;
                }

                click(mc, 45);

                if (!mc.player.containerMenu.getCarried().isEmpty()) {
                    click(mc, selectedSlot);
                }

                done = true;
            }
        }
    }

    private int findTotem(Minecraft mc) {
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            if (mc.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING))
                return i;
        }
        return -1;
    }

    private void click(Minecraft mc, int slot) {
        mc.gameMode.handleContainerInput(
                mc.player.containerMenu.containerId,
                slot,
                0,
                ContainerInput.PICKUP,
                mc.player
        );
    }

    private int inventoryIndexToSlotId(int index) {
        if (index <= 8)
            return index + 36;

        if (index <= 35)
            return index;

        return -1;
    }

    private int nextDelay(IntSetting min, IntSetting max) {
        int lo = min.get();
        int hi = Math.max(lo, max.get());

        return random.nextInt(hi - lo + 1) + lo;
    }

    private long ms(long ms) {
        return ms * 1_000_000L;
    }

    private void reset() {
        stage = Stage.WAIT;
        actionAt = 0;
        selectedSlot = -1;
        done = false;
    }
}
