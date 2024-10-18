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
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

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

            JsonObject jo = NBTToJson.serialize(createTestNBT());
            NBTTagCompound tagCompound = JsonToNBT.deserialize(jo);

            createStructure(MinecraftServer.getServer().getEntityWorld());
            hasRun1 = true;
        }
    }

    public static NBTTagCompound createTestNBT() {
        // Root NBTTagCompound
        NBTTagCompound rootCompound = new NBTTagCompound();

        // Adding different types of NBT tags
        rootCompound.setString("stringKey", "Hello, NBT!");
        rootCompound.setInteger("intKey", 42);
        rootCompound.setDouble("doubleKey", 123.456);
        rootCompound.setFloat("floatKey", 12.34f);
        rootCompound.setByte("byteKey", (byte) 7);
        rootCompound.setShort("shortKey", (short) 100);
        rootCompound.setLong("longKey", 987654321L);

        // Adding a byte array
        byte[] byteArray = new byte[] {1, 2, 3, 4, 5};
        rootCompound.setByteArray("byteArrayKey", byteArray);

        // Adding an int array
        int[] intArray = new int[] {10, 20, 30, 40};
        rootCompound.setIntArray("intArrayKey", intArray);

        // Adding a nested NBTTagCompound
        NBTTagCompound nestedCompound = new NBTTagCompound();
        nestedCompound.setString("nestedStringKey", "Inside Nested Compound");
        nestedCompound.setInteger("nestedIntKey", 1000);
        nestedCompound.setDouble("nestedDoubleKey", 654.321);

        // Add the nested compound to the root
        rootCompound.setTag("nestedCompoundKey", nestedCompound);

        // Adding another level of nested compound inside the first nested compound
        NBTTagCompound deepNestedCompound = new NBTTagCompound();
        deepNestedCompound.setString("deepNestedStringKey", "Deeper still");
        deepNestedCompound.setLong("deepNestedLongKey", 99999999L);

        // Add the deep nested compound to the first nested compound
        nestedCompound.setTag("deepNestedCompoundKey", deepNestedCompound);

        return rootCompound;
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
                this.tile = JsonToNBT.deserialize(jsonElement.getAsJsonObject("nbt"));
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
//            TileEntity tileEntity = createTileEntity(pair.tile);
//            world.setTileEntity(x, y, z, tileEntity);
        }
    }

    // Placeholder for TileEntity creation logic
    private TileEntity createTileEntity(TileEntity tile) {
        // Implement logic to create and return a new TileEntity instance based on the given tile data
        return null; // Replace with actual implementation
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



    public static void setTileEntity(World world, int x, int y, int z, TileEntity aTileEntity) {

        if (world.getChunkFromBlockCoords(x, z) != null) {
            world.setTileEntity(x, y, z, aTileEntity);  // Update tile entity
        }
    }
}
