package com.gtnewhorizons.CTF.items;

import static com.gtnewhorizons.CTF.commands.CommandInitTest.currentTest;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.ENCODED_NBT;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isRegionNotDefined;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isTestNotStarted;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.utils.RegionUtils;
import com.gtnewhorizons.CTF.utils.nbt.NBTConverter;

import cpw.mods.fml.common.registry.GameRegistry;

public class CTFAddItemTag extends Item {

    CTFAddItemTag() {
        this.setUnlocalizedName("CTFAddItemTag");
        this.setTextureName("minecraft:diamond");
        this.setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (world.isRemote) return false;
        if (!player.isSneaking()) return false;
        if (isTestNotStarted()) {
            notifyPlayer(player, EnumChatFormatting.RED + "There is no valid test in construction!");
            return false;
        }

        if (isRegionNotDefined()) {
            notifyPlayer(
                player,
                EnumChatFormatting.RED + "Region is not yet defined. Use the CTF wand to select a valid region.");
            return true;
        }

        if (RegionUtils.isOutsideRegion(x, y, z)) {
            handleInventoryAbsorption(stack, player, world, x, y, z);
            return true;
        }

        addItemInstruction(stack, player, x, y, z);
        return true;
    }

    private void handleInventoryAbsorption(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (!(tileEntity instanceof IInventory)) {
            notifyPlayer(
                player,
                EnumChatFormatting.RED + "You must use this on an inventory block outside the selected region.");
            return;
        }

        IInventory inventory = (IInventory) tileEntity;
        NBTTagCompound nbtData = stack.getTagCompound();
        if (nbtData == null) {
            nbtData = new NBTTagCompound();
            stack.setTagCompound(nbtData);
        }

        NBTTagList storedItems = nbtData.hasKey("StoredItems") ? nbtData.getTagList("StoredItems", 10)
            : new NBTTagList();
        absorbInventoryItems(inventory, storedItems);
        if (storedItems.tagCount() > 0) {
            nbtData.setTag("StoredItems", storedItems);
            notifyPlayer(player, EnumChatFormatting.GREEN + "Items absorbed into the tag item for later deployment.");
        } else {
            notifyPlayer(player, EnumChatFormatting.RED + "No items to absorb from this inventory.");
        }
        return;
    }

    private void absorbInventoryItems(IInventory inventory, NBTTagList storedItems) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (itemStack != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemStack.writeToNBT(itemTag);
                storedItems.appendTag(itemTag);
            }
        }
    }

    private void addItemInstruction(ItemStack stack, EntityPlayer player, int x, int y, int z) {
        int relativeX = x - Math.min(firstPosition[0], secondPosition[0]);
        int relativeY = y - Math.min(firstPosition[1], secondPosition[1]);
        int relativeZ = z - Math.min(firstPosition[2], secondPosition[2]);

        JsonObject instruction = createInstructionJson(relativeX, relativeY, relativeZ, stack);
        JsonArray instructionsArray = currentTest.getAsJsonArray(INSTRUCTIONS);
        instructionsArray.add(instruction);

        notifyPlayer(
            player,
            EnumChatFormatting.GREEN + "Item instructions added for block at ("
                + relativeX
                + ", "
                + relativeY
                + ", "
                + relativeZ
                + ").");
    }

    private JsonObject createInstructionJson(int x, int y, int z, ItemStack stack) {
        JsonObject instruction = new JsonObject();
        instruction.addProperty("type", "addItems");
        instruction.addProperty("optionalLabel", "Adds an item to the inventory of the block.");
        instruction.addProperty("x", x);
        instruction.addProperty("y", y);
        instruction.addProperty("z", z);

        JsonArray itemsArray = new JsonArray();
        NBTTagCompound nbtData = stack.getTagCompound();
        if (nbtData != null && nbtData.hasKey("StoredItems", 9)) {
            NBTTagList storedItems = nbtData.getTagList("StoredItems", 10);
            for (int i = 0; i < storedItems.tagCount(); i++) {
                JsonObject itemData = createItemDataJson(storedItems.getCompoundTagAt(i));
                itemsArray.add(itemData);
            }
        }
        instruction.add("items", itemsArray);
        return instruction;
    }

    private JsonObject createItemDataJson(NBTTagCompound heldItemNBT) {
        ItemStack heldItemStack = ItemStack.loadItemStackFromNBT(heldItemNBT);
        JsonObject itemData = new JsonObject();
        GameRegistry.UniqueIdentifier uniqueIdentifier = GameRegistry.findUniqueIdentifierFor(heldItemStack.getItem());
        itemData.addProperty("registryName", uniqueIdentifier.modId + ":" + uniqueIdentifier.name);
        itemData.addProperty("stackSize", heldItemStack.stackSize);
        itemData.addProperty("metadata", heldItemStack.getItemDamage());

        if (heldItemStack.getTagCompound() != null) {
            String encodedNBT = NBTConverter.encodeToString(heldItemStack.getTagCompound());
            itemData.addProperty(ENCODED_NBT, encodedNBT);
        }
        return itemData;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        // Get the NBT data
        if (stack.hasTagCompound()) {
            NBTTagList storedItems = stack.getTagCompound()
                .getTagList("StoredItems", 10);
            for (int i = 0; i < storedItems.tagCount(); i++) {

                NBTTagCompound heldItemNBT = storedItems.getCompoundTagAt(i);
                ItemStack heldItemStack = ItemStack.loadItemStackFromNBT(heldItemNBT);
                if (heldItemStack == null) {
                    tooltip.add(EnumChatFormatting.RED + "ERROR ITEM! Something has gone wrong.");
                } else {
                    tooltip.add(
                        heldItemStack.getDisplayName() + ":"
                            + heldItemStack.getItemDamage()
                            + " x "
                            + heldItemStack.stackSize);
                }
            }

            // Call super to add any additional information
            super.addInformation(stack, player, tooltip, advanced);
        }
    }
}
