package com.gtnewhorizons.CTF.instructions;

import net.minecraft.inventory.IInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.gtnewhorizons.CTF.MyMod;

public class TestInstructions {

    public static boolean isChestContainingStone(TileEntity tile, World world) {
        return ((IInventory) tile).getStackInSlot(0).stackSize == 1;
    }

    public static boolean checkStructure(TileEntity tile, World world) {
        MyMod.CTF_LOG.info(
            "Ticks complete: {}",
            MinecraftServer.getServer()
                .getTickCounter());
        if (tile == null) {
            MyMod.CTF_LOG.info("Tile is null");
            return false;
        } else {
            MyMod.CTF_LOG.info("Tile is not null.");

            if (tile.isInvalid()) {
                MyMod.CTF_LOG.info("Still failing on an actual test!");
                return false;
            } else {
                MyMod.CTF_LOG.info("Not invalid!");
                return true;
            }
        }
    }
}
