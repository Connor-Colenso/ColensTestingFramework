package com.myname.mymodid;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.myname.mymodid.procedures.Procedure;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Test {

    private static final Random rand = new Random();
    public int startX = rand.nextInt(65) - 32; // Range -32 to 32
    public int startY = rand.nextInt(21) + 4;  // Range 4 to 24
    public int startZ = rand.nextInt(65) - 32; // Range -32 to 32
    public boolean failed = false;


    JsonObject structure;
    HashMap<String, TickHandler.BlockTilePair> keyMap;
    public Queue<Procedure> procedureList = new LinkedList<>();

    public void buildStructure() {

        final World world = MinecraftServer.getServer().getEntityWorld();

        JsonArray build = structure.getAsJsonArray("build");

        // Loop through the build array
        for (int layer = 0; layer < build.size(); layer++) {
            JsonArray layerArray = build.get(layer).getAsJsonArray();
            for (int row = 0; row < layerArray.size(); row++) {
                String rowData = layerArray.get(row).getAsString();
                for (int col = 0; col < rowData.length(); col++) {
                    char key = rowData.charAt(col);
                    TickHandler.BlockTilePair pair = keyMap.get(String.valueOf(key));

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
    private void placeBlockInWorld(World world, int x, int y, int z, TickHandler.BlockTilePair pair) {
        // Set the block in the world
        world.setBlock(x, y, z, Blocks.air, 0, 2);
        world.setBlock(x, y, z, pair.block, pair.meta, 2);

        // If there's a TileEntity, create it and add it to the world
        if (pair.tile != null) {
            NBTTagCompound copy = (NBTTagCompound) pair.tile.copy();
            copy.setInteger("x", x);
            copy.setInteger("y", y);
            copy.setInteger("z", z);

            TileEntity te = world.getTileEntity(x,y,z);
            te.readFromNBT(copy);
        }

        // Not sure why this needs setting twice, but it does.
        world.setBlockMetadataWithNotify(x, y, z, pair.meta, 2);
    }

    // Placeholder for TileEntity creation logic
    private TileEntity createTileEntity(NBTTagCompound tile) {
        // Create the TileEntity instance using the ID
        TileEntity tileEntity = TileEntity.createAndLoadEntity(tile);

        if (tileEntity != null) {
            // Load the NBT data into the newly created TileEntity
            tileEntity.readFromNBT(tile);
        }

        return tileEntity;
    }


    public boolean areChunksLoadedForStructure() {
        final World world = MinecraftServer.getServer().getEntityWorld();

        JsonArray build = structure.getAsJsonArray("build");

        for (int layer = 0; layer < build.size(); layer++) {
            JsonArray layerArray = build.get(layer).getAsJsonArray();
            for (int row = 0; row < layerArray.size(); row++) {
                String rowData = layerArray.get(row).getAsString();
                for (int col = 0; col < rowData.length(); col++) {
                    // Calculate the block's world coordinates
                    int x = startX + col;
                    int z = startZ + row;

                    // Convert block coordinates to chunk coordinates
                    int chunkX = x >> 4; // Divide by 16
                    int chunkZ = z >> 4;

                    // Check if the chunk is loaded
                    if (!world.getChunkProvider().chunkExists(chunkX, chunkZ)) {
                        return false; // If any chunk is not loaded, return false
                    }
                }
            }
        }
        return true; // All necessary chunks are loaded
    }
}
