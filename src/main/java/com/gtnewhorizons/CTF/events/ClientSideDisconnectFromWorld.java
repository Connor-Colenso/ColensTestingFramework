package com.gtnewhorizons.CTF.events;

import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;

public class ClientSideDisconnectFromWorld {

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        // Ensure this is only triggered client-side
        if (event.world.isRemote) {
            CurrentTestUnderConstruction.resetTest(Minecraft.getMinecraft().thePlayer);
        }
    }
}
