package com.myname.mymodid.conditionals;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;


public class TestConditional {

    public static boolean isChestContainingStone(TileEntity tile, World world) {
        return ((IInventory) tile).getStackInSlot(0).stackSize == 1;
    }
}
