package com.myname.mymodid;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.myname.mymodid.conditionals.registry.ConditionalFunction;
import com.myname.mymodid.conditionals.registry.RegisterConditionals;
import com.myname.mymodid.procedures.AddItems;
import com.myname.mymodid.procedures.CheckTile;
import com.myname.mymodid.procedures.Procedure;
import com.myname.mymodid.procedures.RunTicks;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.myname.mymodid.CommonTestFields.ENCODED_NBT;
import static com.myname.mymodid.CommonTestFields.STRUCTURE;
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

            if (procedure instanceof AddItems addItems) {
                // Delete instruction.
                test.procedureList.poll();

                // Retrieve the WorldServer instance and TileEntity at specified coordinates
                WorldServer worldServer = MinecraftServer.getServer().worldServers[0];
                TileEntity te = worldServer.getTileEntity(test.startX + addItems.x, test.startY + addItems.y, test.startZ + addItems.z);

                // Ensure the TileEntity is not null and is an instance of IInventory
                if (te instanceof IInventory) {
                    IInventory inventory = (IInventory) te;
                    List<ItemStack> items = addItems.itemsToAdd;

                    // Iterate over items and add them to the inventory
                    for (ItemStack itemStack : items) {
                        boolean added = false;

                        // Try to find an empty slot or stack existing items if they are the same type
                        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                            ItemStack existingStack = inventory.getStackInSlot(slot);

                            // If slot is empty, set the item stack and mark it as added
                            if (existingStack == null) {
                                inventory.setInventorySlotContents(slot, itemStack);
                                added = true;
                                break;
                            }
                            // If the slot contains the same item and has space, merge stacks
                            else if (existingStack.isItemEqual(itemStack) && existingStack.stackSize < existingStack.getMaxStackSize()) {
                                int spaceLeft = existingStack.getMaxStackSize() - existingStack.stackSize;
                                int amountToAdd = Math.min(itemStack.stackSize, spaceLeft);
                                existingStack.stackSize += amountToAdd;
                                itemStack.stackSize -= amountToAdd;
                                added = true;

                                // Stop if we've added the entire stack
                                if (itemStack.stackSize <= 0) break;
                            }
                        }

                        // If inventory is full and item was not added, handle overflow or notify
                        if (!added) {
                            // Handle overflow or log that inventory is full
                        }
                    }

                    // Mark inventory as dirty to ensure changes are saved
                    inventory.markDirty();
                }

            }

            if (procedure instanceof CheckTile checkTile) {
                // Delete instruction.
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
            else if (type.equals("addItems"))  {

                AddItems addItems = new AddItems();

                addItems.x = instruction.get("x").getAsInt();
                addItems.y = instruction.get("y").getAsInt();
                addItems.z = instruction.get("z").getAsInt();

                // Parse each item from the "items" array
                JsonArray itemsArray = instruction.getAsJsonArray("items");
                for (int itemIndex = 0; itemIndex < itemsArray.size(); itemIndex++) {
                    JsonObject itemObj = itemsArray.get(itemIndex).getAsJsonObject();

                    // Get the registry name, stack size, and metadata.
                    String registryName = itemObj.get("registryName").getAsString();
                    int stackSize = itemObj.get("stackSize").getAsInt();
                    int metadata = itemObj.get("metadata").getAsInt();

                    // Decode the NBT data, if provided
                    String[] splitReg = registryName.split(":");
                    Item item = GameRegistry.findItem(splitReg[0], splitReg[1]);

                    if (item != null) {
                        ItemStack itemStack = new ItemStack(item, stackSize, metadata);

                        // Check if "encodedNBT" is provided and decode it
                        if (itemObj.has("encodedNBT")) {
                            String encodedNBT = itemObj.get("encodedNBT").getAsString();
                            NBTTagCompound nbtTagCompound = NBTConverter.decodeFromString(encodedNBT);
                            itemStack.setTagCompound(nbtTagCompound);
                        }

                        addItems.itemsToAdd.add(itemStack);
                    }
                }

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
