package com.myname.mymodid.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.myname.mymodid.NBTConverter;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

import static com.myname.mymodid.CommonTestFields.INSTRUCTIONS;
import static com.myname.mymodid.commands.CommandInitTest.currentTest;
import static com.myname.mymodid.events.CTFWandEventHandler.firstPosition;
import static com.myname.mymodid.events.CTFWandEventHandler.secondPosition;

public class CTFAddItemTag extends Item {

    CTFAddItemTag() {
        this.setUnlocalizedName("CTFAddItemTag");
        this.setTextureName("minecraft:diamond");
        this.setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return false;
        if (!player.isSneaking()) return false;
        if (currentTest == null) {
            player.addChatMessage(new ChatComponentText("There is no valid test in construction!"));
            return false;
        }

        // Calculate relative coordinates
        int relativeX = x - Math.min(firstPosition[0], secondPosition[0]);
        int relativeY = y - Math.min(firstPosition[1], secondPosition[1]);
        int relativeZ = z - Math.min(firstPosition[2], secondPosition[2]);

        // Prepare the JSON structure for the instruction
        JsonObject instruction = new JsonObject();
        instruction.addProperty("type", "addItems");
        instruction.addProperty("optionalLabel", "Adds an item to the inventory of the block.");
        instruction.addProperty("x", relativeX);
        instruction.addProperty("y", relativeY);
        instruction.addProperty("z", relativeZ);

        // Prepare the items array
        JsonArray itemsArray = new JsonArray();

        // Extract NBT data from the stack
        NBTTagCompound nbtData = stack.getTagCompound();
        if (nbtData != null && nbtData.hasKey("StoredItems", 9)) { // 9 is the type ID for NBTTagList
            NBTTagList storedItems = nbtData.getTagList("StoredItems", 10); // 10 is the type ID for NBTTagCompound

            for (int i = 0; i < storedItems.tagCount(); i++) {
                NBTTagCompound heldItemNBT = storedItems.getCompoundTagAt(i);
                ItemStack heldItemStack = ItemStack.loadItemStackFromNBT(heldItemNBT); // Deserialize the NBT to ItemStack

                // Prepare JSON data for the held item
                JsonObject itemData = new JsonObject();

                GameRegistry.UniqueIdentifier uniqueIdentifier = GameRegistry.findUniqueIdentifierFor(heldItemStack.getItem());
                itemData.addProperty("registryName", uniqueIdentifier.modId + ":" + uniqueIdentifier.name);

                itemData.addProperty("stackSize", heldItemStack.stackSize);
                itemData.addProperty("metadata", heldItemStack.getItemDamage());

                // Encode NBT data to a string
                if (heldItemStack.getTagCompound() != null){
                    String encodedNBT = NBTConverter.encodeToString(heldItemStack.getTagCompound());
                    itemData.addProperty("encodedNBT", encodedNBT);
                }

                // Add the item data to the items array
                itemsArray.add(itemData);
            }
        }

        // Add the items array to the instruction
        instruction.add("items", itemsArray);

        // Add instruction to current test JSON array
        JsonArray instructionsArray = currentTest.getAsJsonArray(INSTRUCTIONS);
        instructionsArray.add(instruction);

        // Notify player of the addition
        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Item instructions added for block at (" + relativeX + ", " + relativeY + ", " + relativeZ + ")."));

        return true;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        // Get the NBT data
        if (stack.hasTagCompound()) {
            NBTTagList storedItems = stack.getTagCompound().getTagList("StoredItems", 10);
            for (int i = 0; i < storedItems.tagCount(); i++) {

                NBTTagCompound heldItemNBT = storedItems.getCompoundTagAt(i);
                ItemStack heldItemStack = ItemStack.loadItemStackFromNBT(heldItemNBT);
                if (heldItemStack == null) {
                    tooltip.add(EnumChatFormatting.RED + "ERROR ITEM! Something has gone wrong.");
                } else {
                    tooltip.add(heldItemStack.getDisplayName() + ":" + heldItemStack.getItemDamage() + " x " + heldItemStack.stackSize);
                }
            }

            // Call super to add any additional information
            super.addInformation(stack, player, tooltip, advanced);
        }
    }
}
