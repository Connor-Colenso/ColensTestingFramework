package com.gtnewhorizons.CTF.conditionals;

import net.minecraft.inventory.IInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;


public class TestConditional {

    public static boolean isChestContainingStone(TileEntity tile, World world) {
        return ((IInventory) tile).getStackInSlot(0).stackSize == 1;
    }

    public static boolean checkStructure(TileEntity tile, World world) {
        System.out.println("Ticks complete: " + MinecraftServer.getServer().getTickCounter());
        if (tile == null) {
            System.out.println("Tile is null");
            return false;
        } else {
            System.out.println("Tile is not null.");

            if (tile.isInvalid()) {
                System.out.println("Still failing on an actual test!");
                return false;
            } else {
                System.out.println("Not invalid!");
                return true;
            }
        }
    }
}
