package com.myname.mymodid.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.myname.mymodid.NBTConverter;
import com.myname.mymodid.Test;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.myname.mymodid.TickHandler.addStructureInfo;
import static com.myname.mymodid.events.CTFWandEventHandler.firstPosition;
import static com.myname.mymodid.events.CTFWandEventHandler.secondPosition;

public class CommandCaptureStructure extends CommandBase {
    @Override
    public String getCommandName() {
        return "getStructure";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/getStructure";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        JsonObject structure = captureStructureJson();
        JsonObject overallJson = new JsonObject();
        overallJson.add("structure", structure);
        overallJson.addProperty("testName", "Blah blah");
        overallJson.addProperty("testName", "Blah blah");
        overallJson.add("instructions", new JsonArray());
        saveJsonToFile(overallJson, "/Users/connorcolenso/Desktop/output.json");



        Test testObj = new Test();
        addStructureInfo(overallJson, testObj);

        testObj.startX = 25;
        testObj.startY = 25;
        testObj.startZ = 25;

        testObj.buildStructure();
    }

    public JsonObject captureStructureJson() {

        World world = MinecraftServer.getServer().getEntityWorld();

        JsonObject structureJson = new JsonObject();
        JsonArray buildArray = new JsonArray();
        JsonObject keysObject = new JsonObject();
        Map<String, Character> blockToKeyMap = new HashMap<>();
        char currentKey = 'A';

        int startX = Math.min(firstPosition[0], secondPosition[0]);
        int startY = Math.min(firstPosition[1], secondPosition[1]);
        int startZ = Math.min(firstPosition[2], secondPosition[2]);

        int endX = Math.max(firstPosition[0], secondPosition[0]);
        int endY = Math.max(firstPosition[1], secondPosition[1]);
        int endZ = Math.max(firstPosition[2], secondPosition[2]);

        // Loop through each layer in the Y-axis
        for (int y = startY; y <= endY; y++) {
            JsonArray layerArray = new JsonArray();

            // Loop through rows and columns in the X and Z axes
            for (int z = startZ; z <= endZ; z++) {
                StringBuilder rowString = new StringBuilder();

                for (int x = startX; x <= endX; x++) {
                    Block block = world.getBlock(x, y, z );
                    int meta = world.getBlockMetadata(x, y, z);
                    TileEntity tileEntity = world.getTileEntity(x, y, z);

                    // Create a unique key for each block type and metadata combination
                    String blockId = Block.blockRegistry.getNameForObject(block).toString() + ":" + meta;
                    if (!blockToKeyMap.containsKey(blockId)) {
                        blockToKeyMap.put(blockId, currentKey);
                        JsonObject keyData = new JsonObject();
                        keyData.addProperty("block", Block.blockRegistry.getNameForObject(block).toString());
                        keyData.addProperty("meta", meta);

                        // If there's a tile entity, serialize its data to JSON
                        if (tileEntity != null) {
                            NBTTagCompound tileTag = new NBTTagCompound();
                            tileEntity.writeToNBT(tileTag);
                            JsonObject te = new JsonObject();
                            te.addProperty("mappingForDefault", tileTag.getString("id"));
                            te.addProperty("data", NBTConverter.encodeToString(tileTag));

                            keyData.add("tileEntity", te);
                        } else {
                            keyData.add("tileEntity", null);
                        }

                        keysObject.add(String.valueOf(currentKey), keyData);
                        currentKey++;
                    }

                    // Append the character to represent this block in the row
                    rowString.append(blockToKeyMap.get(blockId));
                }
                layerArray.add(rowString.toString());
            }
            buildArray.add(layerArray);
        }

        // Add "build" and "keys" to the structure JSON
        structureJson.add("build", buildArray);
        structureJson.add("keys", keysObject);

        return structureJson;
    }

    public static void saveJsonToFile(JsonObject overallJson, String filePath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(gson.toJson(overallJson));
            System.out.println("Json saved successfully to " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing Json to file: " + e.getMessage());
        }
    }

}
