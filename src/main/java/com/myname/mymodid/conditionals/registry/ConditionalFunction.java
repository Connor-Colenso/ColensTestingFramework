package com.myname.mymodid.conditionals.registry;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@FunctionalInterface
public interface ConditionalFunction {
    boolean checkCondition(TileEntity tile, World world);

}
