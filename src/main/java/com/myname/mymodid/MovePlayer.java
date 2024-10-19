package com.myname.mymodid;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;

public class MovePlayer {

    private boolean hasTeleported = false;

    // Coordinates for teleportation
    private final double targetX = 0;
    private final double targetY = 4;
    private final double targetZ = 0;

    public MovePlayer() {
        // Register this class to listen for player tick events
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        EntityPlayer player = event.player;
        World world = player.worldObj;

        // Check if we are on the server side and if the player hasn't been teleported yet
        if (!world.isRemote && !hasTeleported) {
            teleportPlayer(player, targetX, targetY, targetZ);
            hasTeleported = true;  // Ensure teleport happens only once
        }
    }

    private void teleportPlayer(EntityPlayer player, double x, double y, double z) {
        player.setPositionAndUpdate(x, y, z);
        System.out.println("Player teleported to coordinates: " + x + ", " + y + ", " + z);
    }
}
