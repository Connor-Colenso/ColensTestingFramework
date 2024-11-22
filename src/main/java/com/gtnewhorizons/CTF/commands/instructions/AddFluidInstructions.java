package com.gtnewhorizons.CTF.commands.instructions;

import com.google.gson.JsonArray;
import com.gtnewhorizons.CTF.items.RegisterItems;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.FLUID_AMOUNT;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.STORED_FLUIDS;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;

public class AddFluidInstructions {

    @SuppressWarnings("unused")
    public static void add(EntityPlayerMP player, String[] args, JsonArray instructionArray) {
        ItemStack heldItemStack = player.getHeldItem();
        notifyPlayer(player, "Note: You must shift click this onto a block to apply the instruction to the current test.");

        // Handle empty hand by giving them an empty fluid tag item.
        if (heldItemStack == null) {
            notifyPlayer(player, "Your hand was empty, adding an empty fluid tag item.");
            addEmptyFluidTagItem(player);
            return;
        }

        // Check if the item implements IFluidContainerItem (i.e., is a fluid container)
        if (heldItemStack.getItem() instanceof IFluidContainerItem) {
            IFluidContainerItem fluidContainer = (IFluidContainerItem) heldItemStack.getItem();
            FluidStack fluidStack = fluidContainer.getFluid(heldItemStack);

            if (fluidStack != null && fluidStack.amount > 0) {
                // Prepare NBTTagList to store fluids
                NBTTagList storedFluids = new NBTTagList();

                // Create NBTTagCompound for the fluid and add to NBTTagList
                NBTTagCompound fluidData = new NBTTagCompound();

                fluidStack.writeToNBT(fluidData); // Built in.
                fluidData.setInteger(FLUID_AMOUNT, fluidStack.amount * heldItemStack.stackSize);
                storedFluids.appendTag(fluidData);

                // Attach stored fluids list to newFluidTag item.
                ItemStack newFluidTag = new ItemStack(RegisterItems.CTFAddFluidsTag);
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setTag(STORED_FLUIDS, storedFluids);
                newFluidTag.setTagCompound(tagCompound);

                notifyPlayer(player, "Fluid instruction tag created for: " + fluidStack.getFluid().getName() + " x " + fluidStack.amount * heldItemStack.stackSize + "mB");

                // Add the newFluidTag item to the player's inventory
                if (player.inventory.addItemStackToInventory(newFluidTag)) {
                    notifyPlayer(player, "Fluid tag item with stored fluid information has been added to your inventory.");
                } else {
                    notifyPlayer(player, "Your inventory is full. Could not add the fluid tag item.");
                }

            } else {
                // No fluid present in the container.
                notifyPlayer(player, "The fluid container either contains no fluid, or the FluidStack had 0mB present.");
                addEmptyFluidTagItem(player);
                return;
            }

        } else {
            // If item is not a fluid container, treat it as an invalid input
            notifyPlayer(player, "The held item is not a fluid container.");
        }
    }

    private static void addEmptyFluidTagItem(EntityPlayerMP player) {
        // Add logic to create and add an empty fluid tag item to the player's inventory
        ItemStack emptyTagItem = new ItemStack(RegisterItems.CTFAddFluidsTag); // Assuming CTFAddFluidsTag is your fluid tag item
        if (player.inventory.addItemStackToInventory(emptyTagItem)) {
            notifyPlayer(player, "To use this item effectively, it must have a valid fluid. You may shift click it on any block containing fluid outside of the selected test region, this will absorb the fluid into the tag.");
        } else {
            notifyPlayer(player, "Your inventory is full. Could not add an empty fluid tag item.");
        }
    }
}
