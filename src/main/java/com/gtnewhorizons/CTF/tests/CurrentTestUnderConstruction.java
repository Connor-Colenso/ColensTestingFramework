package com.gtnewhorizons.CTF.tests;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;

public class CurrentTestUnderConstruction {

//    private static HashMap<String, Test> tests = new HashMap<>();
    private static HashMap<String, JsonObject> testJsons = new HashMap<>();

    public static void updateTest(String uuid, JsonObject testJson) {
        testJsons.put(uuid, testJson);
//        tests.put(uuid, new Test(testJson));
    }

//    public static Test getTest(String uuid) {
//        return tests.get(uuid);
//    }

    public static JsonObject getTestJson(String playerUUID) {
        return testJsons.getOrDefault(playerUUID, null);
    }

    public static JsonObject getTestJson(EntityPlayer player) {
        return getTestJson(player.getUniqueID().toString());
    }

    public static void removeTest(EntityPlayer player) {
//        tests.remove(player.getUniqueID().toString());
        testJsons.remove(player.getUniqueID().toString());
    }
}
