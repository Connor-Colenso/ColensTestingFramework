package com.gtnewhorizons.CTF;

import static com.gtnewhorizons.CTF.MyMod.jsons;

import com.gtnewhorizons.CTF.tests.TestSettings;
import com.gtnewhorizons.CTF.utils.Json;
import net.minecraft.server.MinecraftServer;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.tests.Test;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class TickHandler {

    private static boolean hasBuilt;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void onWorldTick(TickEvent.ServerTickEvent event) {
        if (event.side.isClient() || event.phase == TickEvent.Phase.START) return;
        if (MinecraftServer.getServer()
            .getTickCounter() == 1) return;

        if (testsMap.isEmpty()) return;

        TestSettings currentSettings = testsMap.keySet().iterator().next();
        ArrayList<Test> currentTests = testsMap.get(currentSettings);

        // Build the test, if we are in the first tick of their run time.
        if (!hasBuilt) {

            for (Test test : currentTests) {
                test.buildStructure();
            }

            hasBuilt = true;
            return;
        }

        // Perform all necessary tasks in this tick for all tests.
        for (Test test : currentTests) {
            test.runProcedures();
        }

        boolean hasCompletedAllTests = true;
        for (Test test : currentTests) {
            hasCompletedAllTests &= test.isDone();
        }

        // Test done, throw away those tests. Move on.
        if (hasCompletedAllTests) {
            testsMap.remove(currentSettings);

            // Reset so we can build more tests.
            hasBuilt = false;
        }

    }

    private static final HashMap<TestSettings, ArrayList<Test>> testsMap = new HashMap<>();

    public static void registerTests() {

        for (JsonObject json : Json.loadAll()) {
            for (int i = 0; i < 4; i++) {
                Test testObj = new Test(json);
                testsMap.putIfAbsent(testObj.getTestSettings(), new ArrayList<>());
                testsMap.get(testObj.getTestSettings()).add(testObj);
            }
        }
    }

}
