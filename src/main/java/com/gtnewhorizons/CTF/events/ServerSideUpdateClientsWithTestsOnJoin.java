package com.gtnewhorizons.CTF.events;

import com.gtnewhorizons.CTF.networking.CTFNetworkHandler;
import com.gtnewhorizons.CTF.networking.UpdateClientSideTestInfo;
import com.gtnewhorizons.CTF.tests.Test;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayerMP;

import static com.gtnewhorizons.CTF.tests.TestManager.uuidTestsMapping;

public class ServerSideUpdateClientsWithTestsOnJoin {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // Send a packet for each test to the player.
        if (event.player instanceof EntityPlayerMP player) {
            for (Test test : uuidTestsMapping.values()) {
                CTFNetworkHandler.INSTANCE.sendTo(new UpdateClientSideTestInfo(test), player);
            }
        }
    }
}
