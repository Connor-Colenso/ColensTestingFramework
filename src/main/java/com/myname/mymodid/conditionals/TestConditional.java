package com.myname.mymodid.conditionals;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;


public class TestConditional {

    public static boolean isChestContainingStone(TileEntity tile, World world) {
        return ((IInventory) tile).getStackInSlot(0).stackSize == 1;
    }

    public static boolean checkStructure(TileEntity tile, World world) {
        if (tile instanceof BaseMetaTileEntity baseMetaTileEntity) {
            MTEMultiBlockBase t = (MTEMultiBlockBase) baseMetaTileEntity.getMetaTileEntity();
            t.checkStructure(true, (IGregTechTileEntity) tile);
        }
        return true;
    }
}
