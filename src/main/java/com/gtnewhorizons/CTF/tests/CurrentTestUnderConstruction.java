package com.gtnewhorizons.CTF.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.ui.MainController;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;

public class CurrentTestUnderConstruction {

//    private static HashMap<String, Test> tests = new HashMap<>();
    private static HashMap<String, JsonObject> testJsons = new HashMap<>();

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
}
