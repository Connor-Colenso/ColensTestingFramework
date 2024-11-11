package com.gtnewhorizons.CTF.commands.instructions;

import com.gtnewhorizons.CTF.items.RegisterItems;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;

public class AddItemInstructions {

    public static void add(ICommandSender sender) {
        if (sender instanceof EntityPlayer player) {
            ItemStack heldItemStack = player.getHeldItem();

            if (heldItemStack == null) {
                notifyPlayer(player, "You are not holding any item.");
                return;
            }

            // Create a new ItemStack of CTFAddItemTag
            ItemStack itemStack = new ItemStack(RegisterItems.CTFAddItemTag);

            // Create an NBT Tag Compound for saving the held item
            NBTTagCompound nbtData = new NBTTagCompound();

            // Store held item information into an NBT list
            NBTTagList storedItems = new NBTTagList();
            NBTTagCompound heldItemNBT = new NBTTagCompound();
            heldItemStack.writeToNBT(heldItemNBT);  // Serialise the held item to NBT
            storedItems.appendTag(heldItemNBT);

            // Add the storedItems list to itemStack's NBT data
            nbtData.setTag("StoredItems", storedItems);
            itemStack.setTagCompound(nbtData);

            // Add the new item to the player's inventory
            if (player.inventory.addItemStackToInventory(itemStack)) {
                notifyPlayer(player,"Item saved: " + heldItemStack.getDisplayName());
            } else {
                notifyPlayer(player, "Inventory full. Could not save item.");
            }
        } else {
            // Console users are invalid.
            notifyPlayer(sender,"Command can only be used by a player in world.");
        }
    }
}
