package com.gtnewhorizons.CTF.tests;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.STRUCTURE;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_CONFIG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.gtnewhorizons.CTF.utils.PrintUtils;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.procedures.AddFluids;
import com.gtnewhorizons.CTF.procedures.AddItems;
import com.gtnewhorizons.CTF.procedures.CheckTile;
import com.gtnewhorizons.CTF.procedures.Procedure;
import com.gtnewhorizons.CTF.procedures.RunTicks;
import com.gtnewhorizons.CTF.utils.BlockTilePair;
import com.gtnewhorizons.CTF.utils.RandomNums;

public class Test {

    private static final List<AxisAlignedBB> existingTests = new ArrayList<>();
    private final TestSettings testSettings = new TestSettings();
    private final List<String> messageList = new ArrayList<>();

    public int startX;
    public int startY;
    public int startZ;

    private int xLength;
    private int yLength;
    private int zLength;

    public int bufferZone;
    public boolean failed = false;

    public JsonObject structure;
    public HashMap<String, BlockTilePair> keyMap = new HashMap<>();
    public Queue<Procedure> procedureList = new LinkedList<>();

    public Test(JsonObject json) {
        testSettings.addGameruleInfo(json);
        addStructureInfo(json);
        addProcedureInfo(json);

        boolean isPositionValid = false;
        AxisAlignedBB testBounds = null;

        while (!isPositionValid) {
            // Generate random coordinates
            startX = RandomNums.getRandomIntInRange(-16, 16);
            startY = RandomNums.getRandomIntInRange(4, 128);
            startZ = RandomNums.getRandomIntInRange(-16, 16);

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
        }

        // Add the valid bounding box to the list
        existingTests.add(testBounds);
    }

    public void buildStructure() {

        final World world = MinecraftServer.getServer()
            .getEntityWorld();

        JsonArray build = structure.getAsJsonArray("build");

        // Loop through the build array
        for (int layer = 0; layer < build.size(); layer++) {
            JsonArray layerArray = build.get(layer)
                .getAsJsonArray();
            for (int row = 0; row < layerArray.size(); row++) {
                String rowData = layerArray.get(row)
                    .getAsString();
                for (int col = 0; col < rowData.length(); col++) {
                    char key = rowData.charAt(col);
                    BlockTilePair pair = keyMap.get(String.valueOf(key));

                    // If a corresponding BlockTilePair exists for the key
                    if (pair != null) {
                        // Calculate the position in the world
                        int x = startX + col;
                        int y = startY + layer;
                        int z = startZ + row;

                        // Place the block in the world at the calculated position
                        placeBlockInWorld(world, x, y, z, pair);
                    }
                }
            }
        }
    }

    // Method to place a block in the world at the specified coordinates
    private void placeBlockInWorld(World world, int x, int y, int z, BlockTilePair pair) {
        // Set the block in the world
        world.setBlock(x, y, z, Blocks.air, 0, 2);
        world.setBlock(x, y, z, pair.block, pair.meta, 2);

        // If there's a TileEntity, create it and add it to the world
        if (pair.tile != null) {
            NBTTagCompound copy = (NBTTagCompound) pair.tile.copy();
            copy.setInteger("x", x);
            copy.setInteger("y", y);
            copy.setInteger("z", z);

            TileEntity te = world.getTileEntity(x, y, z);
            te.readFromNBT(copy);
        }

        // Not sure why this needs setting twice, but it does.
        world.setBlockMetadataWithNotify(x, y, z, pair.meta, 2);
    }

    // Setup info.

    private void addProcedureInfo(JsonObject json) {
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

    private void addStructureInfo(JsonObject json) {
        structure = json.getAsJsonObject(STRUCTURE);

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
        JsonArray build = structure.getAsJsonArray("build");

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

        final JsonObject keys = structure.getAsJsonObject("keys");

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

        for (String message : messageList) {
            System.out.println(message);
        }
    }

    public boolean isDone() {
        return procedureList.isEmpty();
    }
}
