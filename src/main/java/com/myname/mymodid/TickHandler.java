package com.myname.mymodid;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.myname.mymodid.conditionals.registry.ConditionalFunction;
import com.myname.mymodid.conditionals.registry.RegisterConditionals;
import com.myname.mymodid.procedures.CheckTile;
import com.myname.mymodid.procedures.Procedure;
import com.myname.mymodid.procedures.RunTicks;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.myname.mymodid.MyMod.autoLoadWorld;
import static com.myname.mymodid.MyMod.jsons;

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
//            Minecraft.stopIntegratedServer();
            testComplete = false;
        }
    }


    public static boolean hasBuilt = false;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTick(TickEvent.ServerTickEvent event) {
        if (event.side.isClient()) return;

        Test test = MyMod.tests.get(0);

        if (!hasBuilt && event.phase == TickEvent.Phase.START) {
            test.buildStructure();
            hasBuilt = true;
        }

        if (event.phase == TickEvent.Phase.END) {
            assert test.procedureList.peek() != null;
            Procedure procedure = test.procedureList.peek();

            if (procedure == null) {
                testComplete = true;
                return;
            }

            if (procedure instanceof RunTicks) {
                procedure.duration--;
                if (procedure.duration <= -1) test.procedureList.poll();
                return;
            }

            if (procedure instanceof CheckTile checkTile) {

                test.procedureList.poll();

                ConditionalFunction f = RegisterConditionals.getFunc(checkTile.funcID);
                WorldServer worldServer = MinecraftServer.getServer().worldServers[0];
                TileEntity te = worldServer.getTileEntity(test.startX + checkTile.x, test.startY + checkTile.y, test.startZ + checkTile.z);

                try {
                    if (!f.checkCondition(te, worldServer)) {
                        test.failed = true;

                        if (checkTile.optionalLabel != null) {
                            System.out.println("\u001B[31m" + checkTile.optionalLabel + " FAILED\u001B[0m");
                        } else {
                            System.out.println("\u001B[31mFAILED\u001B[0m");
                        }

                    } else {
                        System.out.println("\u001B[32m" + checkTile.optionalLabel + " PASSED\u001B[0m");
                    }
                } catch (Exception e) {
                    System.out.println("\u001B[31mTest threw exception, which was caught by CTF.\u001B[0m");
                }
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
        JsonArray instructions = json.getAsJsonArray("instructions");

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
                String data = jsonElement.get("data").getAsString();
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
        JsonObject structure = json.getAsJsonObject("structure");
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
