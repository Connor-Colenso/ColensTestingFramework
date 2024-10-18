package com.myname.mymodid;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.nbt.*;

public class NBTToJson {

    public static JsonObject serialize(NBTTagCompound compound) {
        JsonObject json = new JsonObject();
        for (String key : compound.func_150296_c()) {  // List of keys in the compound
            NBTBase tag = compound.getTag(key);
            if (tag instanceof NBTTagCompound) {
                json.add(key, serialize((NBTTagCompound) tag)); // Recursively handle nested compounds
            } else {
                addToJson(key, json, tag); // Handle primitive types
            }
        }
        return json;
    }

    private static void addToJson(String key, JsonObject json, NBTBase base) {
        JsonObject newJson = new JsonObject();
        newJson.addProperty("type", base.getClass().getName());

        if (base instanceof NBTTagDouble) {
            newJson.addProperty("value", ((NBTTagDouble) base).func_150286_g());
        } else if (base instanceof NBTTagString) {
            newJson.addProperty("value", ((NBTTagString) base).func_150285_a_());
        } else if (base instanceof NBTTagInt) {
            newJson.addProperty("value", ((NBTTagInt) base).func_150287_d());
        } else if (base instanceof NBTTagFloat) {
            newJson.addProperty("value", ((NBTTagFloat) base).func_150288_h());
        } else if (base instanceof NBTTagLong) {
            newJson.addProperty("value", ((NBTTagLong) base).func_150291_c());
        } else if (base instanceof NBTTagByte) {
            newJson.addProperty("value", ((NBTTagByte) base).func_150290_f());
        } else if (base instanceof NBTTagShort) {
            newJson.addProperty("value", ((NBTTagShort) base).func_150289_e());
        } else if (base instanceof NBTTagByteArray) {
            JsonArray array = new JsonArray();
            for (byte b : ((NBTTagByteArray) base).func_150292_c()) {
                array.add(b);
            }
            newJson.add("value", array);
        } else if (base instanceof NBTTagIntArray) {
            JsonArray array = new JsonArray();
            for (int i : ((NBTTagIntArray) base).func_150302_c()) {
                array.add(i);
            }
            newJson.add("value", array);
        }

        json.add(key, newJson);
    }

}
