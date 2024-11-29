package com.gtnewhorizons.CTF.conditionals;

import net.minecraft.inventory.IInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.gtnewhorizons.CTF.MyMod;

public class TestConditional {

    public static boolean isChestContainingStone(TileEntity tile, World world) {
        return ((IInventory) tile).getStackInSlot(0).stackSize == 1;
    }

    public static boolean checkStructure(TileEntity tile, World world) {
        MyMod.LOG.info(
            "Ticks complete: {}",
            MinecraftServer.getServer()
                .getTickCounter());
        if (tile == null) {
            MyMod.LOG.info("Tile is null");
            return false;
        } else {
            MyMod.LOG.info("Tile is not null.");

            if (tile.isInvalid()) {
                MyMod.LOG.info("Still failing on an actual test!");
                return false;
            } else {
                MyMod.LOG.info("Not invalid!");
                return true;
            }
        }
    }
}
