package com.gtnewhorizons.CTF.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.procedures.Procedure;
import com.gtnewhorizons.CTF.utils.BlockTilePair;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

public class Test {

    private TestSettings testSettings = new TestSettings();

    private static final Random rand = new Random();
    public int startX = rand.nextInt(65) - 32; // Range -32 to 32
    public int startY = rand.nextInt(21) + 4;  // Range 4 to 24
    public int startZ = rand.nextInt(65) - 32; // Range -32 to 32
    public boolean failed = false;

    private final HashMap<String, String> gameruleMap = new HashMap<>();

    public JsonObject structure;
    public HashMap<String, BlockTilePair> keyMap;
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

    public void registerGameRule(String rule, String state) {
        String  doesExist = gameruleMap.putIfAbsent(rule, state);
        if (doesExist != null) throw new RuntimeException("Duplicate gamerule: " + rule);
    }

    public void initGameRulesWorldLoad(WorldServer world) {
        for (Map.Entry<String, String> entry : gameruleMap.entrySet()) {
            world.getGameRules().setOrCreateGameRule(entry.getKey(), entry.getValue());
        }
    }
}
