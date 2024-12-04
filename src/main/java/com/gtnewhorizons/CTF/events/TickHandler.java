package com.gtnewhorizons.CTF.events;

import net.minecraft.server.MinecraftServer;

import com.gtnewhorizons.CTF.tests.TestManager;
import com.gtnewhorizons.CTF.ui.ClientSideExecutor;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class TickHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void onWorldTick(TickEvent.ServerTickEvent event) {
        if (event.side.isClient() || event.phase == TickEvent.Phase.START) return;
        if (MinecraftServer.getServer()
            .getTickCounter() == 1) {
            // This is run once and prepares the areas for tests in all dimensions.
            TestManager.loadAllTestChunks();
            TestManager.clearOutTestZones();
            return;
        }

        if (MinecraftServer.getServer()
            .getTickCounter() < 1000) {
            return;
        }

        TestManager.runTick(event);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        // Ensure we run only during the END phase (after game logic updates)
        if (event.phase == TickEvent.Phase.START) return;

        // Every client side tick, check if the UI has modified anything and implement that in our json.
        ClientSideExecutor.runQueue();
    }

}
