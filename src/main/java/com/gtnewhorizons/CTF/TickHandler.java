package com.gtnewhorizons.CTF;

import static com.gtnewhorizons.CTF.MyMod.jsons;

import net.minecraft.server.MinecraftServer;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.tests.Test;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class TickHandler {

    private static boolean hasBuilt;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTick(TickEvent.ServerTickEvent event) {
        if (event.side.isClient() || event.phase == TickEvent.Phase.START) return;
        if (MinecraftServer.getServer()
            .getTickCounter() == 1) return;

        Test test = MyMod.tests.get(0);

        if (!hasBuilt) {

            for (Test testBuild : MyMod.tests) {
                testBuild.buildStructure();
            }

            hasBuilt = true;
            return;
        }

        for (Test testBuild : MyMod.tests) {
            testBuild.runProcedures();
        }
    }

    public static void registerTests() {

        for (JsonObject json : jsons) {

            for (int i = 0; i < 60; i++) {
                Test testObj = new Test(json);
                MyMod.tests.add(testObj);
            }
        }
    }

}
