package com.myname.mymodid.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class CTFWand extends Item {

    public CTFWand() {
        // Set the unlocalized name and texture for the wand (using stick texture)
        this.setUnlocalizedName("ctfWand");
        this.setTextureName("minecraft:stick");
        this.setMaxStackSize(1);  // Only one wand can be held in a stack
    }


    @Override
    public boolean isFull3D() {
        // This will make the item render like a tool (held in 3D)
        return true;
    }
}
