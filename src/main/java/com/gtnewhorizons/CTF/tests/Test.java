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

import com.gtnewhorizons.CTF.utils.RegionUtils;
import net.minecraft.util.AxisAlignedBB;

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
import com.gtnewhorizons.CTF.utils.RandomNums;

public class Test {

    private static final List<AxisAlignedBB> existingTests = new ArrayList<>();
    private final TestSettings testSettings = new TestSettings();
    private final List<String> messageList = new ArrayList<>();

    // Start with one chunk, expand from there, if needed.
    private static AxisAlignedBB totalTestAreaBounds = AxisAlignedBB.getBoundingBox(0,0,0, 15, 255, 15);

    // Amount of times we had to try fit this test into the framework.
    private int totalTestFitRetries;

    public int startX;
    public int startY;
    public int startZ;

    private int xLength;
    private int yLength;
    private int zLength;

    public int bufferZone;
    public boolean failed = false;

    public JsonObject json;
    public HashMap<String, BlockTilePair> keyMap = new HashMap<>();
    public Queue<Procedure> procedureList = new LinkedList<>();

    public Test(JsonObject json) {
        this.json = json;

        testSettings.addGameruleInfo(json);
        processStructureInfo(json);
        processProcedureInfo(json);

        boolean isPositionValid = false;
        AxisAlignedBB testBounds = null;

        while (!isPositionValid) {
            // Generate random coordinates
            startX = RandomNums.getRandomIntInRange(totalTestAreaBounds.minX, totalTestAreaBounds.maxX);
            startY = RandomNums.getRandomIntInRange(totalTestAreaBounds.minY, totalTestAreaBounds.maxY);
            startZ = RandomNums.getRandomIntInRange(totalTestAreaBounds.minZ, totalTestAreaBounds.maxZ);

            // Increment attempt counter.
            totalTestFitRetries++;

            // Create the bounding box
            testBounds = AxisAlignedBB.getBoundingBox(
                startX - bufferZone,
                startY - bufferZone,
                startZ - bufferZone,
                startX + xLength + bufferZone,
                startY + yLength + bufferZone,
                startZ + zLength + bufferZone);

            // Check for intersection
            isPositionValid = true; // Assume no intersection
            for (AxisAlignedBB existingTest : existingTests) {
                if (existingTest.intersectsWith(testBounds)) {
                    isPositionValid = false; // Found an intersection, regenerate coordinates
                    break;
                }
            }

            // Check to ensure the entire test can fit within the actual world bounds, we don't want it trying to generate at y:250 if the test is 20 blocks tall...
            if (!RegionUtils.isFullyContained(totalTestAreaBounds, testBounds)) {
                isPositionValid = false;
            }

            // Todo: config?
            if (totalTestFitRetries > 5) {
                // Try again, but lets resize the area to be bigger.
                totalTestFitRetries = 0;

                // Actual resizing. This will increase the area by one chunk. in each positive direction.
                // E.g.
                // Original area:
                // X
                // New area:
                // XX
                // XX
                // Then again:
                // XXX
                // XXX
                // XXX
                // etc.

                totalTestAreaBounds = AxisAlignedBB.getBoundingBox(
                    totalTestAreaBounds.minX,
                    totalTestAreaBounds.minY,
                    totalTestAreaBounds.minZ,
                    totalTestAreaBounds.maxX + 16,
                    totalTestAreaBounds.maxY,
                    totalTestAreaBounds.maxZ + 16
                );
            }
        }

        // Add the valid bounding box to the list
        existingTests.add(testBounds);
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
                bufferZone = testConfig.get("bufferZone")
                    .getAsInt();
            }
        }
    }

    private void getSizes() {
        // Get the "build" array from the structure JSON.
        JsonArray build = getStructure().getAsJsonArray("build");

        // Determine the yLength based on the number of layers (height)
        yLength = build.size();

        // Ensure the build array is not empty to prevent errors
        if (yLength == 0) {
            return;
        }

        // Determine the zLength based on the number of rows in the first layer
        JsonArray firstLayer = build.get(0)
            .getAsJsonArray();
        zLength = firstLayer.size();

        // Determine the xLength based on the length of the first row in the first layer
        String firstRowData = firstLayer.get(0)
            .getAsString();
        xLength = firstRowData.length();

        if (xLength == 0 || yLength == 0 || zLength == 0) {
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

    public static AxisAlignedBB getTotalTestAreaBounds() {
        return totalTestAreaBounds;
    }
}
