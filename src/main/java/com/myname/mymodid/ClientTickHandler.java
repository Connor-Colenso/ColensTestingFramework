package com.myname.mymodid;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.myname.mymodid.MyMod.autoLoadWorld;
import static com.myname.mymodid.MyMod.jsons;

public class ClientTickHandler {

    private boolean hasRun = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        Minecraft mc = Minecraft.getMinecraft();

        // Check if we are in the main menu and this hasn't run yet
        if (mc.currentScreen instanceof GuiMainMenu && !hasRun) {
            System.out.println("In the main menu, before world load.");
            hasRun = true;
            autoLoadWorld();
        }
    }

    boolean hasRun1 = false;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !hasRun1) {
            createStructure(MinecraftServer.getServer().getEntityWorld());
            hasRun1 = true;
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
                this.tile = NBTConverter.decodeFromString(jsonElement.get("data").getAsString());
            } else {
                this.tile = null; // No TileEntity if jsonElement is null
            }
        }
    }


    private void createStructure(World world) {
        HashMap<String, BlockTilePair> keyMap = new HashMap<>();

        for (JsonObject json : jsons) {
            JsonObject structure = json.getAsJsonObject("structure");
            JsonArray build = structure.getAsJsonArray("build");
            JsonObject keys = structure.getAsJsonObject("keys");

            buildKeyMap(keys, keyMap);

            // Starting position
            int startX = 0;
            int startY = 10;
            int startZ = 0;

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
    }

    // Method to place a block in the world at the specified coordinates
    private void placeBlockInWorld(World world, int x, int y, int z, BlockTilePair pair) {
        // Set the block in the world
        world.setBlock(x, y, z, pair.block, pair.meta, 2);

        // If there's a TileEntity, create it and add it to the world
        if (pair.tile != null) {
            NBTTagCompound copy = (NBTTagCompound) pair.tile.copy();
            copy.setInteger("x", x);
            copy.setInteger("y", y);
            copy.setInteger("z", z);

            TileEntity tileEntity = createTileEntity(pair.tile);
            world.setTileEntity(x, y, z, tileEntity);
        }
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


    private void buildKeyMap(JsonObject keys, HashMap<String, BlockTilePair> keyMap) {
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



//    public static void setTileEntity(World world, int x, int y, int z, TileEntity aTileEntity) {
//
//        if (world.getChunkFromBlockCoords(x, z) != null) {
//            world.setTileEntity(x, y, z, aTileEntity);  // Update tile entity
//        }
//    }
}
