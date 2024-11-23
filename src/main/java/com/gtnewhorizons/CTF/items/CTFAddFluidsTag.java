package com.gtnewhorizons.CTF.items;

import static com.gtnewhorizons.CTF.commands.CommandInitTest.currentTest;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.FLUID_AMOUNT;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.FLUID_NAME;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.STORED_FLUIDS;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isRegionNotDefined;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isTestNotStarted;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.utils.RegionUtils;

public class CTFAddFluidsTag extends Item {

    CTFAddFluidsTag() {
        this.setUnlocalizedName("CTFAddFluidsTag");
        this.setTextureName("minecraft:emerald");
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
            handleFluidAbsorption(stack, player, world, x, y, z, ForgeDirection.getOrientation(side));
            return true;
        }

        addFluidInstruction(stack, player, x, y, z);
        return true;
    }

    private void handleFluidAbsorption(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
        ForgeDirection forgeDirection) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (!(tileEntity instanceof IFluidHandler)) {
            notifyPlayer(
                player,
                EnumChatFormatting.RED + "You must use this on a fluid tank outside the selected region.");
            return;
        }

        IFluidHandler fluidHandler = (IFluidHandler) tileEntity;
        NBTTagCompound nbtData = stack.getTagCompound();
        if (nbtData == null) {
            nbtData = new NBTTagCompound();
            stack.setTagCompound(nbtData);
        }

        NBTTagList storedFluids = nbtData.hasKey(STORED_FLUIDS) ? nbtData.getTagList(STORED_FLUIDS, 10)
            : new NBTTagList();
        absorbFluids(fluidHandler, storedFluids, forgeDirection);
        if (storedFluids.tagCount() > 0) {
            nbtData.setTag(STORED_FLUIDS, storedFluids);
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

        notifyPlayer(
            player,
            EnumChatFormatting.GREEN + "Fluid instructions added for tank at ("
                + relativeX
                + ", "
                + relativeY
                + ", "
                + relativeZ
                + ").");
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
        if (nbtData != null && nbtData.hasKey(STORED_FLUIDS, 9)) {
            NBTTagList storedFluids = nbtData.getTagList(STORED_FLUIDS, 10);
            for (int i = 0; i < storedFluids.tagCount(); i++) {
                JsonObject fluidData = createFluidDataJson(storedFluids.getCompoundTagAt(i));
                fluidsArray.add(fluidData);
            }
        }
        instruction.add(STORED_FLUIDS, fluidsArray);
        return instruction;
    }

    private JsonObject createFluidDataJson(NBTTagCompound fluidNBT) {
        FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(fluidNBT);
        JsonObject fluidData = new JsonObject();
        fluidData.addProperty(
            FLUID_NAME,
            fluidStack.getFluid()
                .getName());
        fluidData.addProperty(FLUID_AMOUNT, fluidStack.amount);

        return fluidData;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        if (stack.hasTagCompound()) {
            NBTTagList storedFluids = stack.getTagCompound()
                .getTagList(STORED_FLUIDS, 10);
            for (int i = 0; i < storedFluids.tagCount(); i++) {
                NBTTagCompound fluidNBT = storedFluids.getCompoundTagAt(i);
                FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(fluidNBT);
                if (fluidStack == null) {
                    tooltip.add(EnumChatFormatting.RED + "ERROR FLUID! Something has gone wrong.");
                } else {
                    tooltip.add(
                        fluidStack.getFluid()
                            .getName() + " x "
                            + fluidStack.amount
                            + "mB");
                }
            }
            super.addInformation(stack, player, tooltip, advanced);
        }
    }
}
