package com.gtnewhorizons.CTF.procedures;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.ENCODED_NBT;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.MyMod;
import com.gtnewhorizons.CTF.tests.Test;
import com.gtnewhorizons.CTF.tests.TestManager;
import com.gtnewhorizons.CTF.utils.nbt.NBTConverter;

import cpw.mods.fml.common.registry.GameRegistry;

public class AddItems extends Procedure {

    public List<ItemStack> itemsToAdd = new ArrayList<>();
    public int x;
    public int y;
    public int z;

    public AddItems(JsonObject instruction) {
        super(instruction);

        x = instruction.get("x")
            .getAsInt();
        y = instruction.get("y")
            .getAsInt();
        z = instruction.get("z")
            .getAsInt();

        // Parse each item from the "items" array
        JsonArray itemsArray = instruction.getAsJsonArray("items");
        for (int itemIndex = 0; itemIndex < itemsArray.size(); itemIndex++) {
            JsonObject itemObj = itemsArray.get(itemIndex)
                .getAsJsonObject();

            // Get the registry name, stack size, and metadata.
            String registryName = itemObj.get("registryName")
                .getAsString();
            int stackSize = itemObj.get("stackSize")
                .getAsInt();
            int metadata = itemObj.get("metadata")
                .getAsInt();

            // Decode the NBT data, if provided
            String[] splitReg = registryName.split(":");
            Item item = GameRegistry.findItem(splitReg[0], splitReg[1]);

            if (item != null) {
                ItemStack itemStack = new ItemStack(item, stackSize, metadata);

                // Check if "encodedNBT" is provided and decode it
                if (itemObj.has(ENCODED_NBT)) {
                    String encodedNBT = itemObj.get(ENCODED_NBT)
                        .getAsString();
                    NBTTagCompound nbtTagCompound = NBTConverter.decodeFromString(encodedNBT);
                    itemStack.setTagCompound(nbtTagCompound);
                }

                itemsToAdd.add(itemStack);
            }
        }

    }

    public void handleEventCustom(Test test) {

        // Retrieve the WorldServer instance and TileEntity at specified coordinates
        World dimension = TestManager.getWorldByDimensionId(test.getDimensionID());
        TileEntity te = dimension
            .getTileEntity(test.getStartStructureX() + x, test.getStartStructureY() + y, test.getStartStructureZ() + z);

        if (te == null) MyMod.CTF_LOG.info(
            "Could not add item(s) at ({}, {}, {}) as tile entity was null.",
            test.getStartStructureX() + x,
            test.getStartStructureY() + y,
            test.getStartStructureZ() + z);

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
                    else if (existingStack.isItemEqual(itemStack)
                        && existingStack.stackSize < existingStack.getMaxStackSize()) {
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

    @Override
    public String toString() {
        if (itemsToAdd.size() == 1) {
            return "Add item " + itemsToAdd.get(0)
                .getDisplayName();
        } else {
            return "Add items " + itemsToAdd.get(0)
                .getDisplayName() + " & " + itemsToAdd.size() + " others.";
        }
    }

}
