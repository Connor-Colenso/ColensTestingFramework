package com.gtnewhorizons.CTF.networking;

import static com.gtnewhorizons.CTF.MyMod.CTF_LOG;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;
import com.gtnewhorizons.CTF.tests.Test;
import com.gtnewhorizons.CTF.utils.ServerUtils;
import com.gtnewhorizons.CTF.utils.Structure;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class TransmitJsonForBuild extends JsonPacket {

    private int playerX;
    private int playerY;
    private int playerZ;

    public TransmitJsonForBuild(JsonObject json, int x, int y, int z) {
        this.json = json;
        this.playerX = x;
        this.playerY = y;
        this.playerZ = z;
    }

    // Do not delete, needed for server side reflection nonsense.
    public TransmitJsonForBuild() {
        super();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        playerX = buf.readInt();
        playerY = buf.readInt();
        playerZ = buf.readInt();
        super.fromBytes(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(playerX);
        buf.writeInt(playerY);
        buf.writeInt(playerZ);
        super.toBytes(buf);
    }

    // Handler to process the packet when received
    public static class Handler implements IMessageHandler<TransmitJsonForBuild, IMessage> {

        @Override
        public IMessage onMessage(TransmitJsonForBuild message, MessageContext ctx) {
            // Get the player who sent the packet
            EntityPlayer player = ctx.getServerHandler().playerEntity;

            if (!ServerUtils.isPlayerOpped(player)) {
                CTF_LOG
                    .warn("Player {} tried to send json packet to CTF, without permissions.", player.getDisplayName());
                return null; // No response needed.
            }

            Test test = new Test(message.getJson());

            // We do this, because otherwise the players position will sometimes get out of sync by the time the packet is read by the server.
            // This way we fix the printed structures position safely.
            test.setManualPlacement(message.playerX, message.playerY, message.playerZ);

            Structure.buildStructure(test);

            return null; // No response needed.
        }
    }
}
