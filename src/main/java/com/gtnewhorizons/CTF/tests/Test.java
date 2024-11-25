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

import com.github.skjolber.packing.api.Box;
import com.github.skjolber.packing.api.StackPlacement;
import com.github.skjolber.packing.api.StackableItem;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.procedures.AddFluids;
import com.gtnewhorizons.CTF.procedures.AddItems;
import com.gtnewhorizons.CTF.procedures.CheckTile;
import com.gtnewhorizons.CTF.procedures.Procedure;
import com.gtnewhorizons.CTF.procedures.RunTicks;
import com.gtnewhorizons.CTF.utils.BlockTilePair;
import com.gtnewhorizons.CTF.utils.PrintUtils;

public class Test {

    private final TestSettings testSettings = new TestSettings();
    private final List<String> messageList = new ArrayList<>();
    public final String uuid = UUID.randomUUID().toString();

    public StackableItem testBounds;

    private int structureStartX;
    private int structureStartY;
    private int structureStartZ;

    private int xStructureLength;
    private int yStructureLength;
    private int zStructureLength;

    private int testBufferZone;

    public boolean failed = false;

    public JsonObject json;
    public HashMap<String, BlockTilePair> keyMap = new HashMap<>();
    public Queue<Procedure> procedureList = new LinkedList<>();

    public Test(JsonObject json) {
        this.json = json;

        testSettings.addGameruleInfo(json);
        processStructureInfo(json);
        processProcedureInfo(json);

        // This will handle the sorting of our tests into a compact format in world.
        testBounds = new StackableItem(Box.newBuilder().withSize(xStructureLength + testBufferZone * 2, yStructureLength + testBufferZone * 2, zStructureLength + testBufferZone * 2).withWeight(1).withId(uuid).build(), 1);

        totalTests++;
    }

    public JsonObject getStructure() {
        return json.getAsJsonObject(STRUCTURE);
    }

    // Setup info.

    private void processProcedureInfo(JsonObject json) {
        // Get the "instructions" array from the JSON object
        JsonArray instructions = json.getAsJsonArray(INSTRUCTIONS);

        // Loop through the instructions array
        for (int i = 0; i < instructions.size(); i++) {
            JsonObject instruction = instructions.get(i)
                .getAsJsonObject();

            // Determine the type of procedure
            String type = instruction.get("type")
                .getAsString();

            // Create the appropriate procedure based on the type
            switch (type) {
                case "runTicks" -> procedureList.add(new RunTicks(instruction));
                case "checkTile" -> procedureList.add(new CheckTile(instruction));
                case "addItems" -> procedureList.add(new AddItems(instruction));
                case "addFluids" -> procedureList.add(new AddFluids(instruction));
            }
        }
    }

    private void processStructureInfo(JsonObject json) {

        buildKeyMap();
        getSizes();

        if (json.has(TEST_CONFIG)) {
            JsonObject testConfig = json.getAsJsonObject(TEST_CONFIG);
            if (testConfig.has("bufferZone")) {
                testBufferZone = testConfig.get("bufferZone")
                    .getAsInt();
            }
        }
    }

    private void getSizes() {
        // Get the "build" array from the structure JSON.
        JsonArray build = getStructure().getAsJsonArray("build");

        // Determine the yLength based on the number of layers (height)
        yStructureLength = build.size();

        // Ensure the build array is not empty to prevent errors
        if (yStructureLength == 0) {
            return;
        }

        // Determine the zLength based on the number of rows in the first layer
        JsonArray firstLayer = build.get(0)
            .getAsJsonArray();
        zStructureLength = firstLayer.size();

        // Determine the xLength based on the length of the first row in the first layer
        String firstRowData = firstLayer.get(0)
            .getAsString();
        xStructureLength = firstRowData.length();

        if (xStructureLength == 0 || yStructureLength == 0 || zStructureLength == 0) {
            throw new RuntimeException("Structure for test had no volume.");
        }
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
        System.out.println(GREEN + getTestName() + RESET);

        for (String message : messageList) {
            System.out.println(message);
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
        structureStartX = placement.getAbsoluteX() + testBufferZone;
        structureStartY = placement.getAbsoluteY() + testBufferZone;
        structureStartZ = placement.getAbsoluteZ() + testBufferZone;
        sp = placement;
    }

    public StackPlacement sp;

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
    }

    public static void printTotalTestsPassedInfo() {
        double percentage = (double) testsPassed / totalTests * 100;
        System.out.printf("Total tests passed: %d/%d (%.2f%%)%n", testsPassed, totalTests, percentage);
    }

    public void recordEndTime() {
        messageList.add("Time taken: " + (System.currentTimeMillis() - testStartTime) + "ms");
        messageList.add("Ticks taken: " + tickCounter);
    }

    public List<String> relevantDebugInfo() {
        List<String> debugInfo = new ArrayList<>();

        // Add test name and UUID for identification
        debugInfo.add("Test Name: " + getTestName());
        debugInfo.add("UUID: " + uuid);

        // Add information about structure dimensions and buffer zone
        debugInfo.add("Structure Dimensions: " + xStructureLength + " x " + yStructureLength + " x " + zStructureLength);
        debugInfo.add("Buffer Zone: " + testBufferZone);

        // Add the current position of the test in the world
        debugInfo.add("Test Position: (" + structureStartX + ", " + structureStartY + ", " + structureStartZ + ")");
        debugInfo.add("Abs Position: (" + sp.getAbsoluteX() + ", " + sp.getAbsoluteY() + ", " + sp.getAbsoluteZ() + ")");
        debugInfo.add("Abs End Position: (" + sp.getAbsoluteEndX() + ", " + sp.getAbsoluteEndY() + ", " + sp.getAbsoluteEndZ() + ")");

        // Add information about procedures
        debugInfo.add("Procedures Remaining: " + procedureList.size());

        // Add information about test status and time
        debugInfo.add("Failed: " + failed);
        debugInfo.add("Ticks Taken: " + tickCounter);
        debugInfo.add("Time Taken: " + (System.currentTimeMillis() - testStartTime) + " ms");

        // Add the total number of tests and passed tests
        debugInfo.add("Total Tests: " + totalTests);
        debugInfo.add("Tests Passed: " + testsPassed);

        // Add messages collected during the test
        debugInfo.add("Messages: ");
        debugInfo.addAll(messageList);

        return debugInfo;
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
        return structureStartX + xStructureLength;
    }

    public int getEndStructureY() {
        return structureStartY + yStructureLength;
    }

    public int getEndStructureZ() {
        return structureStartZ + zStructureLength;
    }

    public int getBufferStartX() {
        return structureStartX - testBufferZone;
    }

    public int getBufferStartY() {
        return structureStartY - testBufferZone;
    }

    public int getBufferStartZ() {
        return structureStartZ - testBufferZone;
    }

    public int getBufferEndX() {
        return structureStartX + xStructureLength + testBufferZone;
    }

    public int getBufferEndY() {
        return structureStartY + yStructureLength + testBufferZone;
    }

    public int getBufferEndZ() {
        return structureStartZ + zStructureLength + testBufferZone;
    }
}
