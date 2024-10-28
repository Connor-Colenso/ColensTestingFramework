package com.myname.mymodid.commands.instructions;

import com.myname.mymodid.items.RegisterItems;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;

public class AddItemInstructions {

    public static void add(ICommandSender sender) {
        if (sender instanceof EntityPlayer player) {
            ItemStack heldItemStack = player.getHeldItem();

            if (heldItemStack == null) {
                player.addChatMessage(new ChatComponentText("You are not holding any item."));
                return;
            }

            // Create a new ItemStack of CTFAddItemTag
            ItemStack newTagItemStack = new ItemStack(RegisterItems.CTFAddItemTag);

            // Create an NBT Tag Compound for saving the held item
            NBTTagCompound nbtData = new NBTTagCompound();

            // Store held item information into an NBT list
            NBTTagList storedItems = new NBTTagList();
            NBTTagCompound heldItemNBT = new NBTTagCompound();
            heldItemStack.writeToNBT(heldItemNBT);  // Serialise the held item to NBT
            storedItems.appendTag(heldItemNBT);

            // Add the storedItems list to newTagItemStack's NBT data
            nbtData.setTag("StoredItems", storedItems);
            newTagItemStack.setTagCompound(nbtData);

            // Add the new item to the player's inventory
            if (player.inventory.addItemStackToInventory(newTagItemStack)) {
                player.addChatMessage(new ChatComponentText("Item saved: " + heldItemStack.getDisplayName()));
            } else {
                player.addChatMessage(new ChatComponentText("Inventory full. Could not save item."));
            }
        } else {
            // Console users are invalid.
            sender.addChatMessage(new ChatComponentText("Command can only be used by a player."));
        }
    }
}
