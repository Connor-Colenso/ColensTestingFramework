package com.gtnewhorizons.CTF.networking;

import com.google.common.base.Charsets;
import com.gtnewhorizons.CTF.rendering.RenderTest;
import com.gtnewhorizons.CTF.tests.Test;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UpdateClientSideTestInfo implements IMessage {

    // Data fields to serialize/deserialize
    private List<String> textToDraw;
    private int bufferStartX;
    private int bufferStartY;
    private int bufferStartZ;
    private int bufferEndX;
    private int bufferEndY;
    private int bufferEndZ;
    private int dimension;
    private boolean forceRender;
    private String uuid;

    // Constructor to create packet from a Test
    public UpdateClientSideTestInfo(Test test) {
        this.bufferEndX = test.getBufferEndX();
        this.bufferEndY = test.getBufferEndY();
        this.bufferEndZ = test.getBufferEndZ();
        this.bufferStartX = test.getBufferStartX();
        this.bufferStartY = test.getBufferStartY();
        this.bufferStartZ = test.getBufferStartZ();
        this.dimension = test.getDimension();
        this.forceRender = false; // Default value
        this.textToDraw = test.relevantDebugInfo();
        this.uuid = test.getUUID();
    }

    // Default constructor required for IMessage
    public UpdateClientSideTestInfo() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // Deserialize data from the buffer
        try {
            this.bufferStartX = buf.readInt();
            this.bufferStartY = buf.readInt();
            this.bufferStartZ = buf.readInt();
            this.bufferEndX = buf.readInt();
            this.bufferEndY = buf.readInt();
            this.bufferEndZ = buf.readInt();
            this.dimension = buf.readInt(); // Read the dimension
            this.forceRender = buf.readBoolean();

            int textSize = buf.readInt();
            this.textToDraw = new ArrayList<>(textSize);
            for (int i = 0; i < textSize; i++) {
                int strLen = buf.readInt();
                this.textToDraw.add(buf.readBytes(strLen).toString(Charsets.UTF_8));
            }

            int uuidLen = buf.readInt();
            this.uuid = buf.readBytes(uuidLen).toString(Charsets.UTF_8);
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException("Failed to read packet data", e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // Serialize data into the buffer
        buf.writeInt(bufferStartX);
        buf.writeInt(bufferStartY);
        buf.writeInt(bufferStartZ);
        buf.writeInt(bufferEndX);
        buf.writeInt(bufferEndY);
        buf.writeInt(bufferEndZ);
        buf.writeInt(dimension); // Write the dimension
        buf.writeBoolean(forceRender);

        buf.writeInt(textToDraw.size());
        for (String str : textToDraw) {
            byte[] strBytes = str.getBytes(Charsets.UTF_8);
            buf.writeInt(strBytes.length);
            buf.writeBytes(strBytes);
        }

        byte[] uuidBytes = uuid.getBytes(Charsets.UTF_8);
        buf.writeInt(uuidBytes.length);
        buf.writeBytes(uuidBytes);
    }

    public static final HashMap<String, RenderTest> renderTestMap = new HashMap<>();

    public static class Handler implements IMessageHandler<UpdateClientSideTestInfo, IMessage> {
        @Override
        public IMessage onMessage(UpdateClientSideTestInfo message, MessageContext ctx) {
            // Handle message on the client side only.

            RenderTest renderTest = new RenderTest(
                message.textToDraw,
                message.bufferStartX, message.bufferStartY, message.bufferStartZ,
                message.bufferEndX, message.bufferEndY, message.bufferEndZ, message.dimension,
                message.forceRender,
                message.uuid
            );

            renderTestMap.put(message.uuid, renderTest);

            return null; // No response needed
        }
    }
}

