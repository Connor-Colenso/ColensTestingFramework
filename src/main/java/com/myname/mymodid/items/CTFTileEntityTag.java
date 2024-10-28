package com.myname.mymodid.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

import static com.myname.mymodid.commands.CommandInitTest.currentTest;
import static com.myname.mymodid.events.CTFWandEventHandler.firstPosition;
import static com.myname.mymodid.events.CTFWandEventHandler.secondPosition;

public class CTFTileEntityTag extends Item {

    CTFTileEntityTag() {
        this.setUnlocalizedName("CTFTileEntityTag");
        this.setTextureName("minecraft:name_tag");
        this.setMaxStackSize(1);  // Only one wand can be held in a stack
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true; // Action is valid and should propagate to server but should handle server side only.

        // Calculate relative coordinates
        int relativeX = x - Math.min(firstPosition[0], secondPosition[0]);
        int relativeY = y - Math.min(firstPosition[1], secondPosition[1]);
        int relativeZ = z - Math.min(firstPosition[2], secondPosition[2]);

        // Check if the block has a tile entity
        if (world.getTileEntity(x, y, z) == null) {
            // Warn the player if there's no tile entity
            player.addChatMessage(new ChatComponentText("Warning: The clicked block has no tile entity."));

            // Check if the warning was already given
            if (stack.getTagCompound() == null) {
                stack.setTagCompound(new NBTTagCompound());
            }

            if (!stack.getTagCompound().getBoolean("warned")) {
                stack.getTagCompound().setBoolean("warned", true);
            } else {
                // Set the tile entity anyway if the player has been warned
                setInstructionInTest(player, relativeX, relativeY, relativeZ, stack.getTagCompound());
                player.addChatMessage(new ChatComponentText("Tile entity has been set at the clicked block."));
            }
        } else {
            // The block already has a tile entity
            setInstructionInTest(player, relativeX, relativeY, relativeZ, stack.getTagCompound());
            player.addChatMessage(new ChatComponentText("Tile entity has been set at the clicked block."));
        }

        return true;
    }

    // Method to set a tile entity (implementation can vary)
    private void setInstructionInTest(EntityPlayer player, int relX, int relY, int relZ, NBTTagCompound tagNBT) {
        JsonArray instructionsArray = currentTest.getAsJsonArray("instructions");
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
            player.addChatMessage(new ChatComponentText("This tag has not been initialised properly; it may have been spawned in. Please get a new one."));
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
                tooltip.add(EnumChatFormatting.RED + "This is an error, do NOT spawn me in! I am issued by /addinstruction checktile.");
            }

            // Display the optionalLabel if it exists
            if (tag.hasKey("optionalLabel")) {
                String optionalLabel = tag.getString("optionalLabel");
                tooltip.add(EnumChatFormatting.GRAY + "Optional Label: " + optionalLabel); // Grey color
            }
        } else {
            tooltip.add(EnumChatFormatting.RED + "This is an error, do NOT spawn me in! I am issued by /addinstruction checktile.");
        }

        // Call super to add any additional information
        super.addInformation(stack, player, tooltip, advanced);
    }

}
