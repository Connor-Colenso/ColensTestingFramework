package com.myname.mymodid.keybindings;

import com.llamalad7.mixinextras.lib.apache.commons.ArrayUtils;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import cpw.mods.fml.client.FMLClientHandler;

public class KeyBindings {

    public static KeyBinding suspendTimeKey;

    public static void init() {
        suspendTimeKey = new KeyBinding("key.suspendtime", Keyboard.KEY_P, "key.categories.misc");
        FMLClientHandler.instance().getClient().gameSettings.keyBindings =
            ArrayUtils.add(FMLClientHandler.instance().getClient().gameSettings.keyBindings, suspendTimeKey);
    }
}
