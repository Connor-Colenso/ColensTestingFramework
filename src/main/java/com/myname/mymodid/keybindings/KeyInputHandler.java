package com.myname.mymodid.keybindings;

import com.myname.mymodid.MyMod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (KeyBindings.suspendTimeKey.isPressed()) {
            MyMod.magicStopTime = !MyMod.magicStopTime;
        }
    }
}
