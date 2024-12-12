package com.gtnewhorizons.CTF.items;

import net.minecraft.item.Item;

import cpw.mods.fml.common.registry.GameRegistry;

public class RegisterItems {

    public static final Item CTFWand = new CTFWand();
    public static final Item CTFTileEntityTag = new CTFTileEntityTag();

    public static final Item CTFAddItemsTag = new CTFAddItemsTag();
    public static final Item CTFAddFluidsTag = new CTFAddFluidsTag();

    public static void register() {
        GameRegistry.registerItem(RegisterItems.CTFWand, "CTFWand");
        GameRegistry.registerItem(RegisterItems.CTFTileEntityTag, "CTFTileEntityTag");
        GameRegistry.registerItem(RegisterItems.CTFAddItemsTag, "CTFAddItemsTag");
        GameRegistry.registerItem(RegisterItems.CTFAddFluidsTag, "CTFAddFluidsTag");
    }
}
