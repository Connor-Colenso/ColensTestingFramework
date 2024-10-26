package com.myname.mymodid.commands.instructions;

import com.myname.mymodid.items.RegisterItems;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

public class CheckTileInstructions {

    public static void add(ICommandSender sender, String[] args) {
        // Check if the sender is a player
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;

            // Create an ItemStack for the CTFTileEntityTag
            ItemStack stick = new ItemStack(RegisterItems.CTFTileEntityTag, 1); // 1 tile entity tag

            // Create and set the NBTTagCompound for the item
            NBTTagCompound tagCompound = new NBTTagCompound();

            // Check if funcRegistry is provided
            if (args.length > 1) {
                String funcRegistry = args[1]; // First argument after the command
                tagCompound.setString("funcRegistry", funcRegistry); // Set funcRegistry in the NBT
            } else {
                player.addChatMessage(new ChatComponentText("Usage: /command funcRegistry [optionalLabel]"));
                return; // Exit if funcRegistry is missing
            }

            // Check if optionalLabel is provided
            if (args.length > 2) {
                String optionalLabel = args[2]; // Second argument after the command
                tagCompound.setString("optionalLabel", optionalLabel); // Set optionalLabel in the NBT
            }

            stick.setTagCompound(tagCompound); // Set the NBT data to the item

            // Give the stick to the player
            if (!player.inventory.addItemStackToInventory(stick)) {
                player.addChatMessage(new ChatComponentText("Inventory full. Could not issue tile entity tag."));
                return; // Exit the method if the item couldn't be added
            }

            // Notify the player that they received the item
            player.addChatMessage(new ChatComponentText("You have been given a tile entity tag. Please select the block you wish to associate with this test."));

        } else {
            // If the sender is not a player (e.g., console), send a message to the console
            sender.addChatMessage(new ChatComponentText("This command can only be issued by a player."));
        }
    }

}
