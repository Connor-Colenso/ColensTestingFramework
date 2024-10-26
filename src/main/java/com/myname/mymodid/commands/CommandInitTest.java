package com.myname.mymodid.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.myname.mymodid.NBTConverter;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

import static com.myname.mymodid.events.CTFWandEventHandler.firstPosition;
import static com.myname.mymodid.events.CTFWandEventHandler.secondPosition;

public class CommandInitTest extends CommandBase {

    public static JsonObject currentTest;

    @Override
    public String getCommandName() {
        return "inittest";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "inittest testname";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText("Must provide a valid test name. You may not use spaces."));
            return;
        }

        if (firstPosition[0] == Integer.MAX_VALUE || secondPosition[0] == Integer.MAX_VALUE) {
            sender.addChatMessage(new ChatComponentText("You have not selected a valid region using the CTF wand."));
            return;
        }

        currentTest = new JsonObject();
        currentTest.addProperty("testName", args[0]);

        // captureStructureJson will obtain it from the static coordinates set by the CTF Wand (though the event CTFWandEventHandler actually sets the coords).
        currentTest.add("structure", captureStructureJson());
        currentTest.add("instructions", new JsonArray());

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

}
