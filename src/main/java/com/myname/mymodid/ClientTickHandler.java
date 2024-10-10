package com.myname.mymodid;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

import static com.myname.mymodid.MyMod.autoLoadWorld;

public class ClientTickHandler {

    private boolean hasRun = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        Minecraft mc = Minecraft.getMinecraft();

        // Check if we are in the main menu and this hasn't run yet
        if (mc.currentScreen instanceof GuiMainMenu && !hasRun) {
            System.out.println("In the main menu, before world load.");
            hasRun = true;
            autoLoadWorld();
        }
    }

    boolean hasRun1 = false;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !hasRun1) {
            placeBlocks(MinecraftServer.getServer().getEntityWorld());
            hasRun1 = true;
        }
    }

    private void placeBlocks(World world) {

        for (int i = 0; i < 100; i++) {
            final int x = i;
            final int y = i+1;
            final int z = i;

            // Place chest block at coordinates
            world.setBlock(x, y, z, Blocks.chest, 3, 2);  // Set block to chest and notify clients

            TileEntityChest chestEntity = new TileEntityChest();

            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("x", x);
            nbt.setInteger("y", y);
            nbt.setInteger("z", z);

            NBTTagList tagList = new NBTTagList();
            NBTTagCompound stoneAtSlot13 = new NBTTagCompound();
            stoneAtSlot13.setByte("Count", (byte) 1);
            stoneAtSlot13.setByte("Slot", (byte) 13);
            stoneAtSlot13.setShort("Damage", (short) 0);
            stoneAtSlot13.setShort("id", (short) Block.getIdFromBlock(Blocks.stone));

            tagList.appendTag(stoneAtSlot13);
            nbt.setTag("Items", tagList);

            chestEntity.readFromNBT(nbt);

            // Set the custom TileEntity to the world
            setTileEntity(world, x, y, z, chestEntity);
        }
    }

    public static void setTileEntity(World world, int x, int y, int z, TileEntity aTileEntity) {

        if (world.getChunkFromBlockCoords(x, z) != null) {
            world.setTileEntity(x, y, z, aTileEntity);  // Update tile entity
        }
    }
}
