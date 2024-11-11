package com.gtnewhorizons.CTF;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.procedures.AddFluids;
import com.gtnewhorizons.CTF.procedures.AddItems;
import com.gtnewhorizons.CTF.procedures.CheckTile;
import com.gtnewhorizons.CTF.procedures.Procedure;
import com.gtnewhorizons.CTF.procedures.RunTicks;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gtnewhorizons.CTF.CommonTestFields.ENCODED_NBT;
import static com.gtnewhorizons.CTF.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.CommonTestFields.STRUCTURE;
import static com.gtnewhorizons.CTF.MyMod.autoLoadWorld;
import static com.gtnewhorizons.CTF.MyMod.jsons;

public class TickHandler {

    private boolean hasRun = false;
    private boolean testComplete = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        Minecraft mc = Minecraft.getMinecraft();

        // Check if we are in the main menu and this hasn't run yet
        if (mc.currentScreen instanceof GuiMainMenu && !hasRun) {
            hasRun = true;
            autoLoadWorld();
        }

        if (testComplete) {
            Minecraft.stopIntegratedServer();
            testComplete = false;
        }
    }

    private static boolean hasBuilt;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTick(TickEvent.ServerTickEvent event) {
        if (event.side.isClient() || event.phase == TickEvent.Phase.START) return;
        if (MinecraftServer.getServer().getTickCounter() == 1) return;

        Test test = MyMod.tests.get(0);

        if (!hasBuilt) {
            test.buildStructure();
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
            if (procedure instanceof AddItems addItems) {
                addItems.handleEvent(test);
            } else if (procedure instanceof AddFluids addFluids) {
                addFluids.handleEvent(test);
            } else if (procedure instanceof CheckTile checkTile) {
                checkTile.handleEvent(test);
            } else if (procedure instanceof RunTicks runTicks) {
                if (runTicks.duration == 0) continue;

                runTicks.duration--;
                return;
            }
        }
    }
    public static void registerTests() {

        for (JsonObject json : jsons) {
            Test testObj = new Test();
            addStructureInfo(json, testObj);
            addProcedureInfo(json, testObj);

            MyMod.tests.add(testObj);
        }
    }

    private static void addProcedureInfo(JsonObject json, Test testObj) {
        // Get the "instructions" array from the JSON object
        JsonArray instructions = json.getAsJsonArray(INSTRUCTIONS);

        // Loop through the instructions array
        for (int i = 0; i < instructions.size(); i++) {
            JsonObject instruction = instructions.get(i).getAsJsonObject();

            // Determine the type of procedure
            String type = instruction.get("type").getAsString();

            // Create the appropriate procedure based on the type
            if (type.equals("runTicks")) {
                // Create a new RunTicks procedure and set its duration
                RunTicks runTicks = new RunTicks();
                runTicks.duration = instruction.get("duration").getAsInt();

                // Add the RunTicks procedure to the queue
                testObj.procedureList.add(runTicks);

            } else if (type.equals("checkTile")) {
                // Create a new CheckTile procedure and set its properties
                CheckTile checkTile = new CheckTile();

                if (instruction.has("optionalLabel")) {
                    checkTile.optionalLabel = instruction.get("optionalLabel").getAsString();
                }

                checkTile.funcID = instruction.get("funcRegistry").getAsString();
                checkTile.x = instruction.get("x").getAsInt();
                checkTile.y = instruction.get("y").getAsInt();
                checkTile.z = instruction.get("z").getAsInt();

                // Add the CheckTile procedure to the queue
                testObj.procedureList.add(checkTile);
            }
            else if (type.equals("addItems"))  {
                AddItems addItems = new AddItems(instruction);
                testObj.procedureList.add(addItems);
            }
            else if (type.equals("addFluids"))  {
                AddFluids addItems = new AddFluids(instruction);
                testObj.procedureList.add(addItems);
            }

            // You can add more procedure types here if needed in the future
        }
    }


    static class BlockTilePair {
        Block block;
        NBTTagCompound tile;
        int meta;

        public BlockTilePair(String blockName, int meta, JsonObject jsonElement) {
            // Initialize the meta value
            this.meta = meta;

            // Initialize the Block using the blockName
            this.block = Block.getBlockFromName(blockName); // Assuming there's a method to get Block by name

            // Initialize the TileEntity if the jsonElement is not null
            if (jsonElement != null && !jsonElement.isJsonNull()) {

                // This will usually be encoded data, but if none then just make a blank tile entity and use that.
                String data = jsonElement.get(ENCODED_NBT).getAsString();
                if (data.toLowerCase().equals("default")) {
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setString("id", jsonElement.get("mappingForDefault").getAsString());
                    this.tile = tag;
                    return;
                }

                this.tile = NBTConverter.decodeFromString(data);
            } else {
                this.tile = null; // No TileEntity if jsonElement is null
            }
        }
    }

    public static void addStructureInfo(JsonObject json, Test testObj) {
        JsonObject structure = json.getAsJsonObject(STRUCTURE);
        JsonObject keys = structure.getAsJsonObject("keys");
        HashMap<String, BlockTilePair> keyMap = new HashMap<>();

        buildKeyMap(keys, keyMap);

        testObj.structure = structure;
        testObj.keyMap = keyMap;
    }

    private static void buildKeyMap(JsonObject keys, HashMap<String, BlockTilePair> keyMap) {
        for (Map.Entry<String, JsonElement> entry : keys.entrySet()) {
            String key = entry.getKey();
            JsonObject value = entry.getValue().getAsJsonObject();

            // Extract block and meta from the JSON object
            String block = value.get("block").getAsString();
            int meta = value.get("meta").getAsInt();

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
}