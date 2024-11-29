package com.gtnewhorizons.CTF.procedures;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.FLUID_AMOUNT;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.FLUID_NAME;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.STORED_FLUIDS;
import static com.gtnewhorizons.CTF.utils.PrintUtils.RED;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.MyMod;
import com.gtnewhorizons.CTF.tests.Test;
import com.gtnewhorizons.CTF.tests.TestManager;

public class AddFluids extends Procedure {

    public List<FluidStack> fluidsToAdd = new ArrayList<>();
    public int x;
    public int y;
    public int z;

    public AddFluids(JsonObject instruction) {
        // Extract coordinates
        x = instruction.get("x")
            .getAsInt();
        y = instruction.get("y")
            .getAsInt();
        z = instruction.get("z")
            .getAsInt();

        // Parse each fluid from the "fluids" array
        JsonArray fluidsArray = instruction.getAsJsonArray(STORED_FLUIDS);
        for (int fluidIndex = 0; fluidIndex < fluidsArray.size(); fluidIndex++) {
            JsonObject fluidObj = fluidsArray.get(fluidIndex)
                .getAsJsonObject();

            // Get registry name and amount
            String registryName = fluidObj.get(FLUID_NAME)
                .getAsString();
            int amount = fluidObj.get(FLUID_AMOUNT)
                .getAsInt();

            // Retrieve the fluid from the registry
            Fluid fluid = FluidRegistry.getFluid(registryName);
            if (fluid != null) {
                FluidStack fluidStack = new FluidStack(fluid, amount);
                fluidsToAdd.add(fluidStack);
            }
        }
    }

    public void handleEventCustom(Test test) {
        // Retrieve the WorldServer instance and TileEntity at specified coordinates
        World dimension = TestManager.getWorldByDimensionId(test.getDimension());
        TileEntity tileEntity = dimension
            .getTileEntity(test.getStartStructureX() + x, test.getStartStructureY() + y, test.getStartStructureZ() + z);

        if (tileEntity == null) {
            MyMod.LOG.info(
                "Could not add fluid(s) at ({}, {}, {}) as tile entity was null.",
                test.getStartStructureX() + x,
                test.getStartStructureY() + y,
                test.getStartStructureZ() + z);
            return;
        }

        // Check if the TileEntity is a fluid handler
        if (tileEntity instanceof IFluidHandler fluidHandler) {
            // Iterate over fluids and add them to the fluid handler
            for (FluidStack fluidStack : fluidsToAdd) {
                // Try to fill the fluid handler with the current fluid
                int amountFilled = fluidHandler.fill(ForgeDirection.UNKNOWN, fluidStack, true);

                // If the fluid handler couldn’t fill the entire amount, print a message
                if (amountFilled < fluidStack.amount) {
                    if (amountFilled == 0) {
                        test.addMessage(
                            RED,
                            "Error: Could not add fluid " + fluidStack.getFluid()
                                .getName() + ".");
                    } else {
                        test.addMessage(
                            RED,
                            "Warning: Only " + amountFilled
                                + "mB of "
                                + fluidStack.getFluid()
                                    .getName()
                                + " could be added.");
                    }
                }
            }
        }
    }
}
