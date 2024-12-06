package com.gtnewhorizons.CTF.networking;

import static com.gtnewhorizons.CTF.MyMod.CTF_LOG;

import net.minecraft.entity.player.EntityPlayerMP;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;
import com.gtnewhorizons.CTF.tests.Test;
import com.gtnewhorizons.CTF.utils.ServerUtils;
import com.gtnewhorizons.CTF.utils.Structure;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class TransmitJsonForBuild extends JsonPacket {

    public TransmitJsonForBuild(JsonObject json) {
        this.json = json;
    }

    // Do not delete, needed for server side reflection nonsense.
    public TransmitJsonForBuild() {
        super();
    }

    // Handler to process the packet when received
    public static class Handler implements IMessageHandler<TransmitJsonForBuild, IMessage> {

        @Override
        public IMessage onMessage(TransmitJsonForBuild message, MessageContext ctx) {
            // Get the player who sent the packet
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;

            if (!ServerUtils.isPlayerOpped(player)) {
                CTF_LOG
                    .warn("Player {} tried to send json packet to CTF, without permissions.", player.getDisplayName());
                return null; // No response needed.
            }

            Test test = new Test(message.getJson());

            test.setManualPlacement(player.posX + 2, player.posY, player.posZ + 2);

            Structure.buildStructure(test);

            CurrentTestUnderConstruction.updateTest(player, message.getJson());

            return null; // No response needed.
        }
    }
}
