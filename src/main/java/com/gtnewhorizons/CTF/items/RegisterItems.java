package com.gtnewhorizons.CTF.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class RegisterItems {
    public static final Item CTFWand = new CTFWand();
    public static final Item CTFTileEntityTag = new CTFTileEntityTag();

    public static final Item CTFAddItemTag = new CTFAddItemTag();
    public static final Item CTFAddFluidsTag = new CTFAddFluidsTag();

    public static void register() {
        GameRegistry.registerItem(RegisterItems.CTFWand, "CTFWand");
        GameRegistry.registerItem(RegisterItems.CTFTileEntityTag, "CTFTileEntityTag");
        GameRegistry.registerItem(RegisterItems.CTFAddItemTag, "CTFAddItemTag");
        GameRegistry.registerItem(RegisterItems.CTFAddFluidsTag, "CTFAddFluidsTag");
    }
}
