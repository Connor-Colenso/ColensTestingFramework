package com.gtnewhorizons.CTF;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.procedures.Procedure;
import com.gtnewhorizons.CTF.tests.Test;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

import static com.gtnewhorizons.CTF.MyMod.jsons;

public class TickHandler {

    private static boolean hasBuilt;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTick(TickEvent.ServerTickEvent event) {
        if (event.side.isClient() || event.phase == TickEvent.Phase.START) return;
        if (MinecraftServer.getServer().getTickCounter() == 1) return;

        Test test = MyMod.tests.get(0);

        if (!hasBuilt) {

            for (Test testBuild : MyMod.tests) {
                testBuild.buildStructure();
            }

            hasBuilt = true;
            return;
        }

        // Process immediate-duration procedures
        List<Procedure> toProcess = new ArrayList<>();
        while (test.procedureList.peek() != null) {
            Procedure currentProcedure = test.procedureList.peek();
            if (currentProcedure.duration == 0) {
                toProcess.add(test.procedureList.poll());
            } else {
                // Keep the procedure in the queue for later
                toProcess.add(currentProcedure);
                break;
            }
        }

        // Execute each procedure
        for (Procedure procedure : toProcess) {
            if (procedure.duration-- > 0) return;

            procedure.handleEvent(test);
        }
    }
    public static void registerTests() {

        for (JsonObject json : jsons) {

            for(int i = 0; i < 15; i++) {
                Test testObj = new Test(json);
                MyMod.tests.add(testObj);
            }
        }
    }

}
