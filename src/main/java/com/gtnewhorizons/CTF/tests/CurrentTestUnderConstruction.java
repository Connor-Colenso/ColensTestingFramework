package com.gtnewhorizons.CTF.tests;

import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.GAMERULES;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.STRUCTURE;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_CONFIG;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_NAME;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;
import static com.gtnewhorizons.CTF.utils.Structure.captureStructureJson;
import static net.minecraft.client.Minecraft.getMinecraft;

import java.util.concurrent.ConcurrentHashMap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.ui.MainController;
import com.gtnewhorizons.CTF.utils.JsonUtils;

public class CurrentTestUnderConstruction {

    private static ConcurrentHashMap<String, JsonObject> testJsons = new ConcurrentHashMap<>();

    public static void updateTest(String uuid, JsonObject testJson) {
        testJsons.put(uuid, testJson);
    }

    public static void updateTest(EntityPlayer player, JsonObject testJson) {
        updateTest(player.getUniqueID().toString(), testJson);
    }

    public static JsonObject getTestJson(String playerUUID) {
        return testJsons.getOrDefault(playerUUID, null);
    }

    private static JsonObject getTestJson(EntityPlayer player) {
        return getTestJson(
            player.getUniqueID()
                .toString());
    }

    public static void addInstruction(EntityPlayer player, JsonObject instruction) {
        if (!instruction.has("type")) throw new RuntimeException(
            "Invalid instruction was appended to the test in construction. " + instruction.toString());
        getInstructions(player).add(instruction);

        MainController.refreshUIWithJson();
    }

    public static JsonArray getInstructions(EntityPlayer player) {
        return getTestJson(player).getAsJsonArray(INSTRUCTIONS);
    }

    public static void removeTest(EntityPlayer player) {
        // tests.remove(player.getUniqueID().toString());
        testJsons.remove(
            player.getUniqueID()
                .toString());
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

    public static void removeInstruction(int selectedIndex) {
        EntityPlayer player = getMinecraft().thePlayer;
        JsonArray instructions = getInstructions(player);

        // Validate the index
        if (selectedIndex < 0 || selectedIndex >= instructions.size()) {
            throw new IllegalArgumentException("Selected index out of bounds. " + selectedIndex);
        }

        JsonArray updatedInstructions = new JsonArray(); // Create a new array

        for (int i = 0; i < instructions.size(); i++) {
            if (i != selectedIndex) { // Skip the item to remove
                updatedInstructions.add(instructions.get(i)); // Copy all other items
            }
        }

        // Replace the instructions array in the player's test JSON
        JsonObject testJson = getTestJson(player);
        testJson.add(INSTRUCTIONS, updatedInstructions); // Update with new array
    }

    public static boolean removeLastInstruction(EntityPlayer player) {

        JsonObject currentTest = getTestJson(player);
        JsonArray instructions = getInstructions(player);

        if (instructions.size() > 0) {
            JsonArray updatedInstructions = new JsonArray();

            for (int i = 0; i < instructions.size(); i++) {
                if (i != instructions.size() - 1) {
                    updatedInstructions.add(instructions.get(i));
                }
            }
            currentTest.add(INSTRUCTIONS, updatedInstructions);
            return true;

        } else {
            return false;
        }
    }

    private static JsonObject getTestConfig(EntityPlayer player) {
        return getTestJson(player).getAsJsonObject(TEST_CONFIG);
    }

    private static JsonObject getGamerules(EntityPlayer player) {
        return getTestConfig(player).getAsJsonObject(GAMERULES);
    }

    public static void setGamerule(EntityPlayer player, String gamerule, boolean value) {
        JsonObject jsonObject = getGamerules(player);
        jsonObject.addProperty(gamerule, value);
    }

    @SideOnly(Side.CLIENT)
    public static void updateFromUI(JsonObject jsonObject) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        updateTest(player, jsonObject);
    }

    @SideOnly(Side.CLIENT)
    public static JsonObject getTestJson() {
        EntityPlayer entityPlayer = Minecraft.getMinecraft().thePlayer;

        return getTestJson(entityPlayer);
    }
}
