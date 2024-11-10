package com.gtnewhorizons.CTF.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.NBTConverter;
import com.gtnewhorizons.CTF.utils.RegionUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static com.gtnewhorizons.CTF.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.commands.CommandInitTest.currentTest;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;

public class CTFAddFluidsTag extends Item {

    CTFAddFluidsTag() {
        this.setUnlocalizedName("CTFAddFluidsTag");
        this.setTextureName("minecraft:water");
        this.setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return false;
        if (!player.isSneaking()) return false;
        if (currentTest == null) {
            notifyPlayer(player, EnumChatFormatting.RED + "There is no valid test in construction!");
            return false;
        }

        if (isRegionNotDefined()) {
            notifyPlayer(player, EnumChatFormatting.RED + "Region is not yet defined. Use the CTF wand to select a valid region.");
            return true;
        }

        if (!RegionUtils.isWithinRegion(x, y, z)) {
            handleFluidAbsorption(stack, player, world, x, y, z, ForgeDirection.getOrientation(side));
            return true;
        }

        addFluidInstruction(stack, player, x, y, z);
        return true;
    }

    private boolean isRegionNotDefined() {
        return firstPosition[0] == Integer.MAX_VALUE && secondPosition[0] == Integer.MAX_VALUE;
    }

    private void handleFluidAbsorption(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, ForgeDirection forgeDirection) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (!(tileEntity instanceof IFluidHandler)) {
            notifyPlayer(player, EnumChatFormatting.RED + "You must use this on a fluid tank outside the selected region.");
            return;
        }

        IFluidHandler fluidHandler = (IFluidHandler) tileEntity;
        NBTTagCompound nbtData = stack.getTagCompound();
        if (nbtData == null) {
            nbtData = new NBTTagCompound();
            stack.setTagCompound(nbtData);
        }

        NBTTagList storedFluids = nbtData.hasKey("StoredFluids") ? nbtData.getTagList("StoredFluids", 10) : new NBTTagList();
        absorbFluids(fluidHandler, storedFluids, forgeDirection);
        if (storedFluids.tagCount() > 0) {
            nbtData.setTag("StoredFluids", storedFluids);
            notifyPlayer(player, EnumChatFormatting.GREEN + "Fluids absorbed into the tag item for later deployment.");
        } else {
            notifyPlayer(player, EnumChatFormatting.RED + "No fluids to absorb from this tank.");
        }
    }

    private void absorbFluids(IFluidHandler fluidHandler, NBTTagList storedFluids, ForgeDirection forgeDirection) {

        final FluidTankInfo[] fluidTankInfo = fluidHandler.getTankInfo(forgeDirection);

        for (FluidTankInfo fluidInfo : fluidTankInfo) {
            FluidStack fluidStack = fluidInfo.fluid;
            if (fluidStack != null) {
                NBTTagCompound fluidTag = new NBTTagCompound();
                fluidStack.writeToNBT(fluidTag);
                storedFluids.appendTag(fluidTag);
            }
        }
    }

    private void addFluidInstruction(ItemStack stack, EntityPlayer player, int x, int y, int z) {
        int relativeX = x - Math.min(firstPosition[0], secondPosition[0]);
        int relativeY = y - Math.min(firstPosition[1], secondPosition[1]);
        int relativeZ = z - Math.min(firstPosition[2], secondPosition[2]);

        JsonObject instruction = createInstructionJson(relativeX, relativeY, relativeZ, stack);
        JsonArray instructionsArray = currentTest.getAsJsonArray(INSTRUCTIONS);
        instructionsArray.add(instruction);

        notifyPlayer(player, EnumChatFormatting.GREEN + "Fluid instructions added for tank at (" + relativeX + ", " + relativeY + ", " + relativeZ + ").");
    }

    private JsonObject createInstructionJson(int x, int y, int z, ItemStack stack) {
        JsonObject instruction = new JsonObject();
        instruction.addProperty("type", "addFluids");
        instruction.addProperty("optionalLabel", "Adds a fluid to the tank of the block.");
        instruction.addProperty("x", x);
        instruction.addProperty("y", y);
        instruction.addProperty("z", z);

        JsonArray fluidsArray = new JsonArray();
        NBTTagCompound nbtData = stack.getTagCompound();
        if (nbtData != null && nbtData.hasKey("StoredFluids", 9)) {
            NBTTagList storedFluids = nbtData.getTagList("StoredFluids", 10);
            for (int i = 0; i < storedFluids.tagCount(); i++) {
                JsonObject fluidData = createFluidDataJson(storedFluids.getCompoundTagAt(i));
                fluidsArray.add(fluidData);
            }
        }
        instruction.add("fluids", fluidsArray);
        return instruction;
    }

    private JsonObject createFluidDataJson(NBTTagCompound fluidNBT) {
        FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(fluidNBT);
        JsonObject fluidData = new JsonObject();
        fluidData.addProperty("fluidName", fluidStack.getFluid().getName());
        fluidData.addProperty("amount", fluidStack.amount);

        if (fluidStack.tag != null) {
            String encodedNBT = NBTConverter.encodeToString(fluidStack.tag);
            fluidData.addProperty("encodedNBT", encodedNBT);
        }
        return fluidData;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        if (stack.hasTagCompound()) {
            NBTTagList storedFluids = stack.getTagCompound().getTagList("StoredFluids", 10);
            for (int i = 0; i < storedFluids.tagCount(); i++) {
                NBTTagCompound fluidNBT = storedFluids.getCompoundTagAt(i);
                FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(fluidNBT);
                if (fluidStack == null) {
                    tooltip.add(EnumChatFormatting.RED + "ERROR FLUID! Something has gone wrong.");
                } else {
                    tooltip.add(fluidStack.getFluid().getName() + " x " + fluidStack.amount + "mB");
                }
            }
            super.addInformation(stack, player, tooltip, advanced);
        }
    }
}
