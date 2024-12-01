package com.gtnewhorizons.CTF.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.ui.MainController;
import com.gtnewhorizons.CTF.utils.JsonUtils;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.STRUCTURE;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_NAME;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;
import static com.gtnewhorizons.CTF.utils.Structure.captureStructureJson;

public class CurrentTestUnderConstruction {

//    private static HashMap<String, Test> tests = new HashMap<>();
    private static ConcurrentHashMap<String, JsonObject> testJsons = new ConcurrentHashMap<>();

    public static void updateTest(String uuid, JsonObject testJson) {
        testJsons.put(uuid, testJson);
//        tests.put(uuid, new Test(testJson));
    }

//    public static Test getTest(String uuid) {
//        return tests.get(uuid);
//    }

    public static JsonObject getTestJson(String playerUUID) {
        return testJsons.getOrDefault(playerUUID, null);
    }

    private static JsonObject getTestJson(EntityPlayer player) {
        return getTestJson(player.getUniqueID().toString());
    }

    public static void addInstruction(EntityPlayer player, JsonObject instruction) {
        if (!instruction.has("type")) throw new RuntimeException("Invalid instruction was appended to the test in construction. " + instruction.toString());
        getInstructions(player).add(instruction);

        MainController.refreshInstructionList();
    }

    public static JsonArray getInstructions(EntityPlayer player) {
        return getTestJson(player).getAsJsonArray(INSTRUCTIONS);
    }

    public static void removeTest(EntityPlayer player) {
//        tests.remove(player.getUniqueID().toString());
        testJsons.remove(player.getUniqueID().toString());
    }

    public static boolean isTestNotStarted(EntityPlayer player) {
        JsonObject currentTest = getTestJson(player);

        return currentTest == null;
    }

    public static void completeTest(EntityPlayer player) {
        // captureStructureJson will obtain it from the static coordinates set by the CTF Wand (though the event
        // CTFWandEventHandler actually sets the coords).
        // The region itself is drawn by RenderCTFWandFrame and the info in it by RenderCTFRegionInfo.
        JsonObject currentTest = CurrentTestUnderConstruction.getTestJson(player);
        currentTest.add(STRUCTURE, captureStructureJson());

        // Save the test.
        JsonUtils.saveJsonToFile(currentTest);
        notifyPlayer(
            player,
            "Test completed and saved to config/CTF/testing/" + currentTest.get(TEST_NAME)
                .getAsString() + ".json");

        resetTest(player);
    }

    public static void resetTest(EntityPlayer player) {

        firstPosition[0] = Integer.MAX_VALUE;
        firstPosition[1] = Integer.MAX_VALUE;
        firstPosition[2] = Integer.MAX_VALUE;

        secondPosition[0] = Integer.MAX_VALUE;
        secondPosition[1] = Integer.MAX_VALUE;
        secondPosition[2] = Integer.MAX_VALUE;

        CurrentTestUnderConstruction.removeTest(player);
    }
}
