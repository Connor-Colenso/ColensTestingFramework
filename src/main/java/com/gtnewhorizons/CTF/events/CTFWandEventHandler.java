package com.gtnewhorizons.CTF.events;

import static com.gtnewhorizons.CTF.commands.CommandInitTest.currentTest;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import com.gtnewhorizons.CTF.items.CTFWand;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CTFWandEventHandler {

    // Static fields to store positions.
    public static int[] firstPosition = { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };
    public static int[] secondPosition = { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player.worldObj.isRemote) return;
        ItemStack itemStack = player.getHeldItem();

        // Check if the player is holding the CTFWand
        if (itemStack != null && itemStack.getItem() instanceof CTFWand) {
            // Record the right-click action

            if (currentTest != null) {
                notifyPlayer(
                    player,
                    "Test is already in progress. Complete it or reset the region to change the bounds.");
                event.setCanceled(true);
                return;
            }

            if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
                int blockX = event.x;
                int blockY = event.y;
                int blockZ = event.z;

                firstPosition[0] = blockX;
                firstPosition[1] = blockY;
                firstPosition[2] = blockZ;

                // Send message to player
                notifyPlayer(player, "First position set: [" + blockX + ", " + blockY + ", " + blockZ + "]");
                // Stop user accidentally breaking stuff.
                event.setCanceled(true);
            }

            // Record the left-click action
            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                int blockX = event.x;
                int blockY = event.y;
                int blockZ = event.z;

                // Store the second position
                secondPosition[0] = blockX;
                secondPosition[1] = blockY;
                secondPosition[2] = blockZ;

                // Send message to player
                notifyPlayer(player, "Second position set: [" + blockX + ", " + blockY + ", " + blockZ + "]");
            }
        }
    }
}
