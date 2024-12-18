package com.gtnewhorizons.CTF.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class ServerUtils {

    public static boolean isPlayerOpped(EntityPlayer player) {
        MinecraftServer server = MinecraftServer.getServer();

        // Check if the player is in the list of opped players using obfuscation magic.
        return server.getConfigurationManager()
            .func_152596_g(player.getGameProfile());
    }
}
