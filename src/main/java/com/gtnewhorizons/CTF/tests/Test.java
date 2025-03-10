package com.gtnewhorizons.CTF.tests;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.STRUCTURE;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_CONFIG;
import static com.gtnewhorizons.CTF.utils.PrintUtils.GREEN;
import static com.gtnewhorizons.CTF.utils.PrintUtils.RESET;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import net.minecraft.util.EnumChatFormatting;

import com.github.skjolber.packing.api.Box;
import com.github.skjolber.packing.api.StackPlacement;
import com.github.skjolber.packing.api.StackableItem;
import com.github.skjolber.packing.api.StackableSurface;
import com.github.skjolber.packing.api.Surface;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.MyMod;
import com.gtnewhorizons.CTF.procedures.Procedure;
import com.gtnewhorizons.CTF.utils.BlockTilePair;
import com.gtnewhorizons.CTF.utils.JsonUtils;
import com.gtnewhorizons.CTF.utils.PrintUtils;


public final class Test implements IClientSyncTestInfo {

    private final TestSettings testSettings;
    private final List<String> messageList = new ArrayList<>();
    private final String uuid = UUID.randomUUID()
        .toString();

    public final StackableItem testBounds;

    private int structureStartX;
    private int structureStartY;
    private int structureStartZ;

    private int structureLengthX;
    private int structureLengthY;
    private int structureLengthZ;

    private boolean failed = false;

    public JsonObject json;
    public HashMap<String, BlockTilePair> keyMap = new HashMap<>();
    public Queue<Procedure> procedureList = new LinkedList<>();

    public Test(JsonObject json) {
        this.json = json;

        if (json.has(TEST_CONFIG)) {
            JsonObject testConfig = json.get(TEST_CONFIG)
                .getAsJsonObject();

            testSettings = new TestSettings(testConfig);
        } else {
            testSettings = new TestSettings();
        }

        processStructureInfo();
        processProcedureInfo();

        // This will handle the sorting of our tests into a compact format in world.
        StackableSurface stackableSurface = StackableSurface.newBuilder()
            .withSide(new Surface(Surface.Label.TOP), false)
            .build();
        if (testSettings.isPreserveVertical()) {
            testBounds = new StackableItem(
                Box.newBuilder()
                    .withStackableSurface(stackableSurface)
                    .withSize(
                        structureLengthX + testSettings.getBufferZoneInBlocks() * 2,
                        255,
                        structureLengthZ + testSettings.getBufferZoneInBlocks() * 2)
                    .withWeight(1)
                    .withId(uuid)
                    .build(),
                1);
        } else {
            testBounds = new StackableItem(
                Box.newBuilder()
                    .withStackableSurface(stackableSurface)
                    .withSize(
                        structureLengthX + testSettings.getBufferZoneInBlocks() * 2,
                        structureLengthY + testSettings.getBufferZoneInBlocks() * 2,
                        structureLengthZ + testSettings.getBufferZoneInBlocks() * 2)
                    .withWeight(1)
                    .withId(uuid)
                    .build(),
                1);
        }

        totalTests++;
    }

    public TestSettings getSettings() {
        return testSettings;
    }

    public JsonObject getStructure() {
        return json.getAsJsonObject(STRUCTURE);
    }

    // Setup info.

    private void processProcedureInfo() {
        // Get the "instructions" array from the JSON object
        JsonArray instructions = json.getAsJsonArray(INSTRUCTIONS);

        // Loop through the instructions array
        for (int i = 0; i < instructions.size(); i++) {
            JsonObject instruction = instructions.get(i)
                .getAsJsonObject();

            procedureList.add(Procedure.buildProcedureFromJson(instruction));
        }
    }

    private void processStructureInfo() {
        buildKeyMap();
        getSizes();
    }

    private void getSizes() {

        // Get the structure dimensions (X, Y, Z)
        int[] dimensions = JsonUtils.getStructureDimensions(json);

        structureLengthX = dimensions[0];
        structureLengthY = dimensions[1];
        structureLengthZ = dimensions[2];
    }

    private void buildKeyMap() {

        final JsonObject keys = getStructure().getAsJsonObject("keys");

        for (Map.Entry<String, JsonElement> entry : keys.entrySet()) {
            String key = entry.getKey();
            JsonObject value = entry.getValue()
                .getAsJsonObject();

            // Extract block and meta from the JSON object
            String block = value.get("block")
                .getAsString();
            int meta = value.get("meta")
                .getAsInt();

            // Safely handle tileEntity which might be null or JsonNull
            JsonElement tileEntityElement = value.get("tileEntity");
            JsonObject tileEntity = null;
            if (tileEntityElement != null && tileEntityElement.isJsonObject()) {
                tileEntity = tileEntityElement.getAsJsonObject();
            }

            // Create a new BlockTilePair object
            BlockTilePair blockTilePair = new BlockTilePair(block, meta, tileEntity);

            // Put the key and BlockTilePair in the keyMap
            keyMap.put(key, blockTilePair);
        }
    }

    public void runProcedures() {
        if (procedureList.isEmpty()) return;

        tickCounter++;

        // Process immediate-duration procedures.
        List<Procedure> toProcess = new ArrayList<>();
        while (procedureList.peek() != null) {
            Procedure currentProcedure = procedureList.peek();
            if (currentProcedure.duration == 0) {
                toProcess.add(procedureList.poll());
            } else {
                // Keep the procedure in the queue for later.
                toProcess.add(currentProcedure);
                break;
            }
        }

        // Execute each procedure
        for (Procedure procedure : toProcess) {
            if (procedure.duration-- > 0) return;

            procedure.handleEvent(this);
        }
    }

    public TestSettings getTestSettings() {
        return testSettings;
    }

    public void addMessage(String colourCode, String message) {
        messageList.add(PrintUtils.colourConsole(colourCode, message));
    }

    public void printAllMessages() {
        PrintUtils.printSeparator();
        MyMod.CTF_LOG.info(GREEN + "{}" + RESET, getTestName());

        for (String message : messageList) {
            MyMod.CTF_LOG.info(message);
        }
    }

    private String getTestName() {
        return json.get("testName")
            .getAsString();
    }

    public boolean isDone() {
        return procedureList.isEmpty();
    }

    public void setPlacement(StackPlacement placement) {
        structureStartX = placement.getAbsoluteX() + testSettings.getBufferZoneInBlocks();
        structureStartY = placement.getAbsoluteY() + testSettings.getBufferZoneInBlocks();
        structureStartZ = placement.getAbsoluteZ() + testSettings.getBufferZoneInBlocks();
    }

    long testStartTime = 0;
    int tickCounter = 1; // Set to one, because we skip the first tick when constructing the test in world.

    public void startTimer() {
        testStartTime = System.currentTimeMillis();
    }

    private static int totalTests;
    private static int testsPassed;

    public void handleFinalConclusion() {
        if (!failed) {
            testsPassed++;
        }

        // This replaces all the console colour codes with minecraft readable ones, so the user can see correctly
        // formatted info.
        for (int i = 0; i < messageList.size(); i++) {
            String message = messageList.get(i);
            for (Map.Entry<String, EnumChatFormatting> entry : PrintUtils.CONSOLE_COLOUR_TO_MINECRAFT_COLOUR
                .entrySet()) {
                message = message.replace(
                    entry.getKey(),
                    entry.getValue()
                        .toString());
            }
            messageList.set(i, message); // Update the modified message in the list
        }
    }

    public static void printTotalTestsPassedInfo() {
        double percentage = (double) testsPassed / totalTests * 100;
        String message = String.format("Total tests passed: %d/%d (%.2f%%)", testsPassed, totalTests, percentage);
        MyMod.CTF_LOG.info(message);
    }

    public void recordEndTime() {
        messageList.add("Time taken: " + (System.currentTimeMillis() - testStartTime) + "ms");
        messageList.add("Ticks taken: " + tickCounter);
    }

    public List<String> relevantDebugInfo() {
        List<String> debugInfo = new ArrayList<>();

        // Add test name and UUID for identification
        debugInfo.add("Test Name: " + getTestName());

        // Add information about structure dimensions and buffer zone
        debugInfo.add(
            "Structure Dimensions: (" + structureLengthX + "x, " + structureLengthY + "y, " + structureLengthZ + "z).");
        debugInfo.add("Buffer Zone: " + testSettings.getBufferZoneInBlocks());

        // Add information about procedures
        debugInfo.add("Procedures Remaining: " + procedureList.size());

        // Add information about test status
        if (failed) {
            debugInfo.add(EnumChatFormatting.RED + "Test failed");
        } else {
            debugInfo.add(EnumChatFormatting.GREEN + "Test passed");
        }

        // Add messages collected during the test
        debugInfo.add("Messages: ");
        debugInfo.addAll(messageList);

        return debugInfo;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public boolean testFailed() {
        return failed;
    }

    public int getStartStructureX() {
        return structureStartX;
    }

    public int getStartStructureY() {
        return structureStartY;
    }

    public int getStartStructureZ() {
        return structureStartZ;
    }

    public int getEndStructureX() {
        return structureStartX + structureLengthX;
    }

    public int getEndStructureY() {
        return structureStartY + structureLengthY;
    }

    public int getEndStructureZ() {
        return structureStartZ + structureLengthZ;
    }

    public int getBufferStartX() {
        return structureStartX - testSettings.getBufferZoneInBlocks();
    }

    public int getBufferStartY() {
        return structureStartY - testSettings.getBufferZoneInBlocks();
    }

    public int getBufferStartZ() {
        return structureStartZ - testSettings.getBufferZoneInBlocks();
    }

    public int getBufferEndX() {
        return structureStartX + structureLengthX + testSettings.getBufferZoneInBlocks();
    }

    public int getBufferEndY() {
        return structureStartY + structureLengthY + testSettings.getBufferZoneInBlocks();
    }

    public int getBufferEndZ() {
        return structureStartZ + structureLengthZ + testSettings.getBufferZoneInBlocks();
    }

    public int getDimensionID() {
        return testSettings.getDimension();
    }

    public void setManualPlacement(double x, double y, double z) {
        structureStartX = (int) x;
        structureStartY = (int) y;
        structureStartZ = (int) z;
    }

    public void didFail() {
        failed = true;
    }
}
