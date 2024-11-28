package com.gtnewhorizons.CTF;


import com.gtnewhorizons.CTF.tests.TestManager;
import net.minecraft.server.MinecraftServer;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class TickHandler {


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void onWorldTick(TickEvent.ServerTickEvent event) {
        if (event.side.isClient() || event.phase == TickEvent.Phase.START) return;
        if (MinecraftServer.getServer().getTickCounter() == 1) {
            // This is run once and prepares the areas for tests in all dimensions.
            TestManager.loadAllTestChunks();
            TestManager.clearOutTestZones();
            return;
        }

        TestManager.runTick(event);
    }



}
