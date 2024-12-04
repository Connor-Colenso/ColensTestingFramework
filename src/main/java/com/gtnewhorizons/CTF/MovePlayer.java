package com.gtnewhorizons.CTF;

import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.world.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class MovePlayer {

    private boolean hasTeleported = false;

    // Coordinates for teleportation
    private final double targetX = 0;
    private final double targetY = 4;
    private final double targetZ = 0;

    public MovePlayer() {
        // Register this class to listen for player tick events
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        EntityPlayer player = event.player;
        World world = player.worldObj;

        // Check if we are on the server side and if the player hasn't been teleported yet
        if (!world.isRemote && !hasTeleported) {
            teleportPlayer(player, targetX, targetY, targetZ);
            hasTeleported = true; // Ensure teleport happens only once
        }
    }

    private void teleportPlayer(EntityPlayer player, double x, double y, double z) {
        player.setPositionAndUpdate(x, y, z);
        notifyPlayer(player, "You were teleported to " + x + ", " + y + ", " + z + " by CTF.");
        MyMod.CTF_LOG.info("{} was teleported to coordinates: {}, {}, {} by CTF.", player.getDisplayName(), x, y, z);

        setPlayerFlying(player);
    }

    private void setPlayerFlying(EntityPlayer player) {
        PlayerCapabilities capabilities = player.capabilities;

        // Set the player to the flying state immediately.
        capabilities.isFlying = true;

        // Synchronise the updated capabilities with the client
        player.sendPlayerAbilities();
    }
}
