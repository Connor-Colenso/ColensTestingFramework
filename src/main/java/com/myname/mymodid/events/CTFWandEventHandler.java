package com.myname.mymodid.events;

import com.myname.mymodid.items.CTFWand;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class CTFWandEventHandler {


    // Static fields to store positions.
    public static int[] firstPosition = { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };
    public static int[] secondPosition = { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };


    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        World world = player.worldObj;
        ItemStack itemStack = player.getHeldItem();

        // Check if the player is holding the CTFWand
        if (itemStack != null && itemStack.getItem() instanceof CTFWand) {
            // Record the right-click action
            if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
                int blockX = event.x;
                int blockY = event.y;
                int blockZ = event.z;

                firstPosition[0] = blockX;
                firstPosition[1] = blockY;
                firstPosition[2] = blockZ;

                // Send message to player
                if (!world.isRemote) {
                    player.addChatMessage(new ChatComponentText("First position set: [" + blockX + ", " + blockY + ", " + blockZ + "]"));
                }
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
                if (!world.isRemote) {
                    player.addChatMessage(new ChatComponentText("Second position set: [" + blockX + ", " + blockY + ", " + blockZ + "]"));
                }
            }
        }
    }
}
