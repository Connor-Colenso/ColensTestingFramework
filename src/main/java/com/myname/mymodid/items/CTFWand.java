package com.myname.mymodid.items;

import net.minecraft.item.Item;

public class CTFWand extends Item {

    public CTFWand() {
        // Set the unlocalised name and texture for the wand (using stick texture)
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
