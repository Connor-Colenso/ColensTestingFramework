package com.myname.mymodid;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.*;

public class JsonToNBT {

    public static NBTTagCompound deserialize(JsonObject json) {
        NBTTagCompound compound = new NBTTagCompound();

        for (String key : json.keySet()) {
            JsonElement element = json.get(key);

            // Check if this is a nested compound (no "type" field, but has children).
            if (element.isJsonObject() && !element.getAsJsonObject().has("type")) {
                NBTTagCompound nestedCompound = deserialize(element.getAsJsonObject());
                compound.setTag(key, nestedCompound); // Recursively process nested compound
            } else {
                JsonObject tagJson = element.getAsJsonObject();
                String type = tagJson.get("type").getAsString();
                NBTBase nbtTag = createNBTFromJson(type, tagJson.get("value"));
                compound.setTag(key, nbtTag);
            }
        }

        return compound;
    }

    private static NBTBase createNBTFromJson(String type, JsonElement value) {
        switch (type) {
            case "net.minecraft.nbt.NBTTagDouble":
                return new NBTTagDouble(value.getAsDouble());
            case "net.minecraft.nbt.NBTTagString":
                return new NBTTagString(value.getAsString());
            case "net.minecraft.nbt.NBTTagInt":
                return new NBTTagInt(value.getAsInt());
            case "net.minecraft.nbt.NBTTagFloat":
                return new NBTTagFloat(value.getAsFloat());
            case "net.minecraft.nbt.NBTTagLong":
                return new NBTTagLong(value.getAsLong());
            case "net.minecraft.nbt.NBTTagByte":
                return new NBTTagByte(value.getAsByte());
            case "net.minecraft.nbt.NBTTagShort":
                return new NBTTagShort(value.getAsShort());
            case "net.minecraft.nbt.NBTTagByteArray":
                JsonArray byteArray = value.getAsJsonArray();
                byte[] bytes = new byte[byteArray.size()];
                for (int i = 0; i < byteArray.size(); i++) {
                    bytes[i] = byteArray.get(i).getAsByte();
                }
                return new NBTTagByteArray(bytes);
            case "net.minecraft.nbt.NBTTagIntArray":
                JsonArray intArray = value.getAsJsonArray();
                int[] ints = new int[intArray.size()];
                for (int i = 0; i < intArray.size(); i++) {
                    ints[i] = intArray.get(i).getAsInt();
                }
                return new NBTTagIntArray(ints);
            case "net.minecraft.nbt.NBTTagCompound":
                return deserialize(value.getAsJsonObject()); // Handle nested compound
            default:
                throw new IllegalArgumentException("Unknown NBT type: " + type);
        }
    }
}
