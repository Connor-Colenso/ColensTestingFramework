package com.gtnewhorizons.CTF.procedures;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.Test;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class AddFluids extends Procedure {
    public List<FluidStack> fluidsToAdd = new ArrayList<>();
    public int x;
    public int y;
    public int z;

    public AddFluids(JsonObject instruction) {
        // Extract coordinates
        x = instruction.get("x").getAsInt();
        y = instruction.get("y").getAsInt();
        z = instruction.get("z").getAsInt();

        // Parse each fluid from the "fluids" array
        JsonArray fluidsArray = instruction.getAsJsonArray("fluids");
        for (int fluidIndex = 0; fluidIndex < fluidsArray.size(); fluidIndex++) {
            JsonObject fluidObj = fluidsArray.get(fluidIndex).getAsJsonObject();

            // Get registry name and amount
            String registryName = fluidObj.get("registryName").getAsString();
            int amount = fluidObj.get("amount").getAsInt();

            // Retrieve the fluid from the registry
            Fluid fluid = FluidRegistry.getFluid(registryName);
            if (fluid != null) {
                FluidStack fluidStack = new FluidStack(fluid, amount);
                fluidsToAdd.add(fluidStack);
            }
        }
    }

    public void handleEvent(Test test) {
        // Retrieve the WorldServer instance and TileEntity at specified coordinates
        WorldServer worldServer = MinecraftServer.getServer().worldServers[0];
        TileEntity te = worldServer.getTileEntity(test.startX + x, test.startY + y, test.startZ + z);

        if (te == null) {
            System.out.println("Could not add fluid(s) at (" + (test.startX + x) + ", " + (test.startY + y) + ", " + (test.startZ + z) + ") as tile entity was null.");
            return;
        }

        // Check if the TileEntity is a fluid handler
        if (te instanceof IFluidHandler fluidHandler) {
            // Iterate over fluids and add them to the fluid handler
            for (FluidStack fluidStack : fluidsToAdd) {
                // Try to fill the fluid handler with the current fluid
                int amountFilled = fluidHandler.fill( ForgeDirection.UNKNOWN, fluidStack, true);

                // If the fluid handler couldn’t fill the entire amount, print a message
                if (amountFilled < fluidStack.amount) {
                    System.out.println("Warning: Only " + amountFilled + "mB of " + fluidStack.getFluid().getName() + " could be added.");
                }
            }
        }
    }
}
