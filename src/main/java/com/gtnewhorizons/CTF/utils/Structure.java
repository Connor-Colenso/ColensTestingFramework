package com.gtnewhorizons.CTF.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.utils.nbt.NBTConverter;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

import static com.gtnewhorizons.CTF.CommonTestFields.ENCODED_NBT;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;

public class Structure {

    public static JsonObject captureStructureJson() {
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
                    Block block = world.getBlock(x, y, z);
                    int meta = world.getBlockMetadata(x, y, z);
                    TileEntity tileEntity = world.getTileEntity(x, y, z);

                    // Create a unique key for each block type, metadata, and NBT combination
                    String blockId = Block.blockRegistry.getNameForObject(block).toString() + ":" + meta;
                    String nbtData = "";

                    NBTTagCompound tileTag = new NBTTagCompound();
                    if (tileEntity != null) {
                        tileEntity.writeToNBT(tileTag);
                        tileTag.removeTag("x");
                        tileTag.removeTag("y");
                        tileTag.removeTag("z");

                        nbtData = NBTConverter.encodeToString(tileTag);
                    }

                    String uniqueBlockId = blockId + ":" + nbtData;

                    if (!blockToKeyMap.containsKey(uniqueBlockId)) {
                        blockToKeyMap.put(uniqueBlockId, currentKey);
                        JsonObject keyData = new JsonObject();

                        // Use pick block stack for display name if available
                        ItemStack displayStack = block.getPickBlock(null, world, x, y, z);
                        if (displayStack != null) {
                            keyData.addProperty("displayName", displayStack.getDisplayName());
                        } else {
                            keyData.addProperty("displayName", "Air");
                        }

                        keyData.addProperty("block", Block.blockRegistry.getNameForObject(block).toString());
                        keyData.addProperty("meta", meta);

                        // If there's a tile entity, serialize its data to JSON
                        if (tileEntity != null) {
                            JsonObject te = new JsonObject();
                            te.addProperty("mappingForDefault", tileTag.getString("id"));
                            te.addProperty(ENCODED_NBT, nbtData);
                            keyData.add("tileEntity", te);
                        } else {
                            keyData.add("tileEntity", null);
                        }

                        keysObject.add(String.valueOf(currentKey), keyData);
                        currentKey++;
                    }

                    // Append the character representing this block to the row
                    rowString.append(blockToKeyMap.get(uniqueBlockId));
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
