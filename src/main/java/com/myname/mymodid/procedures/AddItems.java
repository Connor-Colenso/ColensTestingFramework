package com.myname.mymodid.procedures;

import com.myname.mymodid.Test;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;

public class AddItems extends Procedure {
    public List<ItemStack> itemsToAdd = new ArrayList<>();
    public int x;
    public int y;
    public int z;

    public void handleEvent(Test test) {

        // Retrieve the WorldServer instance and TileEntity at specified coordinates
        WorldServer worldServer = MinecraftServer.getServer().worldServers[0];
        TileEntity te = worldServer.getTileEntity(test.startX + x, test.startY + y, test.startZ + z);

        // Ensure the TileEntity is not null and is an instance of IInventory
        if (te instanceof IInventory inventory) {

            // Iterate over items and add them to the inventory
            for (ItemStack itemStackUncopied : itemsToAdd) {

                ItemStack itemStack = itemStackUncopied.copy();

                // Try to find an empty slot or stack existing items if they are the same type
                for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                    ItemStack existingStack = inventory.getStackInSlot(slot);

                    // If slot is empty, set the item stack and mark it as added
                    if (existingStack == null) {
                        inventory.setInventorySlotContents(slot, itemStack);
                        break;
                    }
                    // If the slot contains the same item and has space, merge stacks
                    else if (existingStack.isItemEqual(itemStack) && existingStack.stackSize < existingStack.getMaxStackSize()) {
                        int spaceLeft = existingStack.getMaxStackSize() - existingStack.stackSize;
                        int amountToAdd = Math.min(itemStack.stackSize, spaceLeft);
                        existingStack.stackSize += amountToAdd;
                        itemStack.stackSize -= amountToAdd;

                        // Stop if we've added the entire stack
                        if (itemStack.stackSize <= 0) break;
                    }
                }
            }

            // Mark inventory as dirty to ensure changes are saved
            inventory.markDirty();
        }
    }
}
