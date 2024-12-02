package com.gtnewhorizons.CTF.commands.instructions;

import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.gtnewhorizons.CTF.items.RegisterItems;

public class AddItemInstructions {

    @SuppressWarnings("unused")
    public static JsonObject createProcedure(EntityPlayerMP player, String[] args) {
        ItemStack heldItemStack = player.getHeldItem();

        if (heldItemStack == null) {
            notifyPlayer(player, "You are not holding any item.");
            return null;
        }

        // Create a new ItemStack of CTFAddItemTag
        ItemStack itemStack = new ItemStack(RegisterItems.CTFAddItemTag);

        // Create an NBT Tag Compound for saving the held item
        NBTTagCompound nbtData = new NBTTagCompound();

        // Store held item information into an NBT list
        NBTTagList storedItems = new NBTTagList();
        NBTTagCompound heldItemNBT = new NBTTagCompound();
        heldItemStack.writeToNBT(heldItemNBT); // Serialise the held item to NBT
        storedItems.appendTag(heldItemNBT);

        // Add the storedItems list to itemStack's NBT data
        nbtData.setTag("StoredItems", storedItems);
        itemStack.setTagCompound(nbtData);

        // Add the new item to the player's inventory
        if (player.inventory.addItemStackToInventory(itemStack)) {
            notifyPlayer(player, "Item saved: " + heldItemStack.getDisplayName());
        } else {
            notifyPlayer(player, "Inventory full. Could not save item.");
        }

        return null;
    }
}
