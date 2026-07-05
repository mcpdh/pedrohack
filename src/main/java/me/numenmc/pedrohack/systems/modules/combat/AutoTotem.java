package me.numenmc.pedrohack.systems.modules.combat;

import me.numenmc.pedrohack.systems.Module;
import me.numenmc.pedrohack.systems.event.EventHandler;
import me.numenmc.pedrohack.systems.event.events.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;

public class AutoTotem extends Module {
    public AutoTotem() {
        super("auto-totem", "Automatically place totems into the offhand.", true);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.gameMode == null) return;

        if (!mc.player.containerMenu.getCarried().isEmpty()) return;

        if (!mc.player.getOffhandItem().isEmpty()) return;

        int inventoryIndex = -1;

        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            if (mc.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                inventoryIndex = i;
                break;
            }
        }

        if (inventoryIndex == -1) return;

        int slotId = inventoryIndexToSlotId(inventoryIndex);

        mc.gameMode.handleContainerInput(
                mc.player.containerMenu.containerId,
                slotId,
                0,
                ContainerInput.PICKUP,
                mc.player
        );

        mc.gameMode.handleContainerInput(
                mc.player.containerMenu.containerId,
                45,
                0,
                ContainerInput.PICKUP,
                mc.player
        );

        mc.gameMode.handleContainerInput(
                mc.player.containerMenu.containerId,
                slotId,
                0,
                ContainerInput.PICKUP,
                mc.player
        );
    }

    private int inventoryIndexToSlotId(int index) {
        if (index >= 0 && index <= 8) {
            return index + 36;
        }

        if (index >= 9 && index <= 35) {
            return index;
        }

        if (index == 40) {
            return 45;
        }

        return -1;
    }
}
