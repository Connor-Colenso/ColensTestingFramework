package com.gtnewhorizons.CTF.networking;

import static com.gtnewhorizons.CTF.utils.JsonUtils.jsonParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.gson.JsonObject;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class JsonPacket implements IMessage {

    protected JsonObject json;

    // Constructor to create packet with a JsonObject
    public JsonPacket(JsonObject json) {
        this.json = json;
    }

    // Default constructor required for IMessage
    public JsonPacket() {
        this.json = new JsonObject();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // Convert the ByteBuf to a byte array, decompress it, and parse it into a JsonObject
        byte[] compressedData = new byte[buf.readableBytes()];
        buf.readBytes(compressedData);

        // Decompress the data
        String jsonString = decompressData(compressedData);

        // Parse the JSON string into a JsonObject
        this.json = jsonParser.parse(jsonString)
            .getAsJsonObject();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // Convert the JsonObject to a String
        String jsonString = json.toString();

        // Compress the string data
        byte[] compressedData = compressData(jsonString);

        // Write the compressed data to the buffer
        buf.writeBytes(compressedData);
    }

    private String decompressData(byte[] data) {
        // Decompress GZIP data into a string
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(data));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = gzipInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            return byteArrayOutputStream.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] compressData(String data) {
        // Compress the string data into a byte array using GZIP
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {

            gzipOutputStream.write(data.getBytes());
            gzipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    // Getter to access the JSON data in the packet
    public JsonObject getJson() {
        return json;
    }

}
