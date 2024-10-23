package com.myname.mymodid;

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
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.Map;

import static com.myname.mymodid.MyMod.autoLoadWorld;
import static com.myname.mymodid.MyMod.jsons;

public class TickHandler {

    private boolean hasRun = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        Minecraft mc = Minecraft.getMinecraft();

        // Check if we are in the main menu and this hasn't run yet
        if (mc.currentScreen instanceof GuiMainMenu && !hasRun) {
            hasRun = true;
            autoLoadWorld();
        }
    }


    public static boolean hasBuilt = false;
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTick(TickEvent.ServerTickEvent event) {

        if (!hasBuilt && event.phase == TickEvent.Phase.START) {
            Test test = MyMod.tests.get(0);
            test.buildStructure();
            hasBuilt = true;
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

    private static void addStructureInfo(JsonObject json, Test testObj) {
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
