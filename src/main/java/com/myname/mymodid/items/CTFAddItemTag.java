package com.myname.mymodid.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.myname.mymodid.NBTConverter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

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
        instruction.addProperty("type", "addItem");
        instruction.addProperty("optionalLabel", "Adds an item to the inventory of the block.");
        instruction.addProperty("x", relativeX);
        instruction.addProperty("y", relativeY);
        instruction.addProperty("z", relativeZ);

        // Prepare the items array with the details of the current item
        JsonArray itemsArray = new JsonArray();
        JsonObject itemData = new JsonObject();
        itemData.addProperty("registryName", this.getUnlocalizedName().substring(5));
        itemData.addProperty("stackSize", 1);

        // Encode NBT data to a string (assume you have a method `encodeNBTToString`)
        String encodedNBT = NBTConverter.encodeToString(stack.getTagCompound());
        itemData.addProperty("encodedNBT", encodedNBT);

        // Add the item data to the items array and the array to the instruction
        itemsArray.add(itemData);
        instruction.add("items", itemsArray);

        // Add instruction to current test JSON array
        JsonArray instructionsArray = currentTest.getAsJsonArray(INSTRUCTIONS);
        instructionsArray.add(instruction);

        // Notify player of the addition
        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Item instruction added for block at (" + relativeX + ", " + relativeY + ", " + relativeZ + ")."));

        return true;
    }

}
