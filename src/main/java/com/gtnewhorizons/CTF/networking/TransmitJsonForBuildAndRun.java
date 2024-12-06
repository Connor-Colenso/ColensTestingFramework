package com.gtnewhorizons.CTF.networking;

import static com.gtnewhorizons.CTF.MyMod.CTF_LOG;
import static micdoodle8.mods.galacticraft.core.util.VersionUtil.isPlayerOpped;

import net.minecraft.entity.player.EntityPlayerMP;

import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class TransmitJsonForBuildAndRun extends JsonPacket {

    public static class Handler implements IMessageHandler<TransmitJsonForBuild, IMessage> {

        @Override
        public IMessage onMessage(TransmitJsonForBuild message, MessageContext ctx) {
            // Get the player who sent the packet
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;

            if (!isPlayerOpped(player)) {
                CTF_LOG
                    .warn("Player {} tried to send json packet to CTF, without permissions.", player.getDisplayName());
                return null; // No response needed.
            }

            CurrentTestUnderConstruction.updateTest(player, message.getJson());

            return null; // No response needed.
        }

    }
}
