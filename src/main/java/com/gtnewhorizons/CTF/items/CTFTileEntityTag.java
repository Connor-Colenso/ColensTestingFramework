package com.gtnewhorizons.CTF.items;

import static com.gtnewhorizons.CTF.commands.CommandInitTest.currentTest;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isRegionNotDefined;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isTestNotStarted;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.utils.RegionUtils;

public class CTFTileEntityTag extends Item {

    CTFTileEntityTag() {
        this.setUnlocalizedName("CTFTileEntityTag");
        this.setTextureName("minecraft:name_tag");
        this.setMaxStackSize(1); // Only one wand can be held in a stack
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (world.isRemote) return false;
        if (!player.isSneaking()) return false;
        if (isTestNotStarted()) {
            notifyPlayer(player, "There is no valid test in construction!");
            return false;
        }

        if (isRegionNotDefined()) {
            notifyPlayer(player, "Region is not yet defined. Use the CTF wand to select a valid region.");
            return true;
        }

        if (RegionUtils.isOutsideRegion(x, y, z)) {
            notifyPlayer(player, "You must use this on a block within the selected CTF region.");
            return true;
        }

        // Calculate relative coordinates
        int relativeX = x - Math.min(firstPosition[0], secondPosition[0]);
        int relativeY = y - Math.min(firstPosition[1], secondPosition[1]);
        int relativeZ = z - Math.min(firstPosition[2], secondPosition[2]);

        // Check if the block has a tile entity
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity == null) {
            // Warn the player if there's no tile entity
            notifyPlayer(player, "Warning: The clicked block has no tile entity. Click again to confirm.");

            NBTTagCompound tag = stack.getTagCompound();

            // Ensure the stack has a tag compound for tracking warnings
            if (tag == null) {
                tag = new NBTTagCompound();
                stack.setTagCompound(tag);
            }

            // Check if the warning was already given
            if (!tag.getBoolean("warned")) {
                tag.setBoolean("warned", true);
            } else {
                // Set the tile entity if the player has already been warned
                setInstructionInTest(player, relativeX, relativeY, relativeZ, stack.getTagCompound());
                notifyPlayer(player, "Tile entity has been set at the clicked block.");
            }
        } else {
            // The block already has a tile entity, set instruction
            setInstructionInTest(player, relativeX, relativeY, relativeZ, stack.getTagCompound());
            notifyPlayer(player, "Tile entity has been set at the clicked block.");
        }

        return true;
    }

    private void setInstructionInTest(EntityPlayer player, int relX, int relY, int relZ, NBTTagCompound tagNBT) {
        JsonArray instructionsArray = currentTest.getAsJsonArray(INSTRUCTIONS);
        JsonObject instruction = new JsonObject();
        instruction.addProperty("type", "checkTile");

        if (tagNBT.hasKey("optionalLabel")) {
            instruction.addProperty("optionalLabel", tagNBT.getString("optionalLabel"));
        }

        instruction.addProperty("x", relX);
        instruction.addProperty("y", relY);
        instruction.addProperty("z", relZ);

        if (tagNBT.hasKey("funcRegistry")) {
            instruction.addProperty("funcRegistry", tagNBT.getString("funcRegistry"));
        } else {
            notifyPlayer(
                player,
                "This tag has not been initialised properly; it may have been spawned in. Please get a new one.");
            return;
        }

        instructionsArray.add(instruction);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        // Get the NBT data
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();

            // Display the funcRegistry
            if (tag.hasKey("funcRegistry")) {
                String funcRegistry = tag.getString("funcRegistry");
                tooltip.add(EnumChatFormatting.GRAY + "Func Registry: " + funcRegistry); // Grey color
            } else {
                tooltip.add(
                    EnumChatFormatting.RED
                        + "This is an error, do NOT spawn me in! I am issued by /addinstruction checktile.");
            }

            // Display the optionalLabel if it exists
            if (tag.hasKey("optionalLabel")) {
                String optionalLabel = tag.getString("optionalLabel");
                tooltip.add(EnumChatFormatting.GRAY + "Optional Label: " + optionalLabel); // Grey color
            }
        } else {
            tooltip.add(
                EnumChatFormatting.RED
                    + "This is an error, do NOT spawn me in! I am issued by /addinstruction checktile.");
        }

        // Call super to add any additional information
        super.addInformation(stack, player, tooltip, advanced);
    }

}
