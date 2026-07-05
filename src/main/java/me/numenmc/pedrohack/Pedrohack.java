package me.numenmc.pedrohack;

import com.mojang.blaze3d.platform.InputConstants;
import me.numenmc.pedrohack.commands.Commands;
import me.numenmc.pedrohack.systems.Categories;
import me.numenmc.pedrohack.systems.SessionConfig;
import me.numenmc.pedrohack.systems.event.EventBus;
import me.numenmc.pedrohack.systems.event.events.*;
import me.numenmc.pedrohack.systems.screens.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pedrohack implements ClientModInitializer {
    public static final String id = "pedrohack";
    public static final Logger log = LoggerFactory.getLogger(Pedrohack.id);

    private final KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(id, "category"));
    private final KeyMapping openGui = new KeyMapping("Open GUI", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category);
    private final KeyMapping openCommands = new KeyMapping("Open Commands", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SEMICOLON, category);

    @Override
    public void onInitializeClient() {
        Pedrohack.log.info("Welcome");

        Commands.init();

        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, Identifier.fromNamespaceAndPath(Pedrohack.id, "hud"), (graphics, deltaTracker) -> {
//            if (!(Minecraft.getInstance().gui.screen() instanceof PedrohackScreen)) {
//                for (HudElement element : SessionConfig.loadedHudElements) {
//                    int resolvedX = element.resolvedX(5, graphics.guiWidth() - 5);
//                    int resolvedY = element.resolvedY(5, graphics.guiHeight() - 5);
//
//                    graphics.pose().pushMatrix().translate(resolvedX, resolvedY);
//                    element.render(graphics);
//                    graphics.pose().popMatrix();
//                }
//            }

//            NotificationRenderer.render(graphics);
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CROSSHAIR, Identifier.fromNamespaceAndPath(Pedrohack.id, "render"), (graphics, deltaTracker) -> {
            EventBus.post(new Render2DEvent(graphics, deltaTracker));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.consumeClick()) {
                client.gui.setScreen(new ClickGuiScreen());
            }

            while (openCommands.consumeClick()) {
                client.gui.setScreen(new ChatScreen(Commands.PREFIX, true));
            }

            EventBus.post(new TickEvent());
        });

        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> EventBus.post(new ChunkLoadEvent(level, chunk)));
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> EventBus.post(new ChunkUnloadEvent(level, chunk)));
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((be, w) -> EventBus.post(new BlockEntityLoadEvent(w, be)));
        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((be, w) -> EventBus.post(new BlockEntityUnloadEvent(w, be)));
        ClientPlayConnectionEvents.DISCONNECT.register((a, b) -> EventBus.post(new DisconnectEvent()));

        SessionConfig.load();
        Categories.reloadKeybinds();
    }
}
