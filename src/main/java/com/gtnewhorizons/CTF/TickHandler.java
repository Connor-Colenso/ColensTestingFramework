package com.gtnewhorizons.CTF;

import static com.gtnewhorizons.CTF.utils.Structure.buildStructure;

import java.util.ArrayList;


import com.gtnewhorizons.CTF.tests.TestManager;
import com.gtnewhorizons.CTF.utils.PrintUtils;
import net.minecraft.server.MinecraftServer;

import com.gtnewhorizons.CTF.tests.Test;
import com.gtnewhorizons.CTF.tests.TestSettings;

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
            TestManager.clearOutTestZones();
            TestManager.loadAllTestChunks();
            return;
        }

        TestManager.runTick(event);
    }



}
