package com.gtnewhorizons.CTF.commands;

import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import com.gtnewhorizons.CTF.MyMod;
import com.gtnewhorizons.CTF.utils.nbt.NBTConverter;

public class CommandGetTileEntity extends CommandBase {

    @Override
    public String getCommandName() {
        return "getTileEntity";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/getTileEntity";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            World world = player.getEntityWorld();

            // Get player's eye position (height offset included)
            Vec3 playerPosition = player.getPosition(1.0F)
                .addVector(0, player.getEyeHeight(), 0);

            // Get the direction the player is looking at
            Vec3 lookVector = player.getLookVec();

            // Extend the reach vector to a reasonable distance (e.g., 200 blocks)
            double reachDistance = 200.0;
            Vec3 reachVector = playerPosition.addVector(
                lookVector.xCoord * reachDistance,
                lookVector.yCoord * reachDistance,
                lookVector.zCoord * reachDistance);

            // Perform the ray trace
            MovingObjectPosition mop = world.rayTraceBlocks(playerPosition, reachVector);
            if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                int blockX = mop.blockX;
                int blockY = mop.blockY;
                int blockZ = mop.blockZ;

                TileEntity tileEntity = world.getTileEntity(blockX, blockY, blockZ);
                if (tileEntity != null) {
                    notifyPlayer(player, "TileEntity found at: " + blockX + ", " + blockY + ", " + blockZ);
                    NBTTagCompound tag = new NBTTagCompound();
                    tileEntity.writeToNBT(tag);

                    String encodedNBT = NBTConverter.encodeToString(tag);

                    MyMod.LOG.info("Encoded: {}", encodedNBT);
                    notifyPlayer(player, "Encoded: " + encodedNBT);

                    MyMod.LOG.info("Metadata: {}", tileEntity.blockMetadata);
                    notifyPlayer(player, "Metadata: " + tileEntity.blockMetadata);
                } else {
                    notifyPlayer(player, "No TileEntity at the specified location.");
                }
            } else {
                notifyPlayer(player, "No block targeted.");
            }
        }
    }

}
