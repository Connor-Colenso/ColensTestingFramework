package com.myname.mymodid.conditionals;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;


public class TestConditional {

    public static boolean isChestContainingStone(TileEntity tile, World world, int tickCounter) {
        return ((TileEntityChest) tile).getStackInSlot(13).stackSize == 1;
    }
}
