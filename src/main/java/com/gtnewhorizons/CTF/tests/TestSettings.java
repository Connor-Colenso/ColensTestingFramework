package com.gtnewhorizons.CTF.tests;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.GAMERULES;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.world.WorldServer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TestSettings {

    private final HashMap<String, String> gameruleMap = new HashMap<>();

    public void registerGameRule(String rule, String state) {
        String doesExist = gameruleMap.putIfAbsent(rule, state);
        if (doesExist != null) throw new RuntimeException("Duplicate gamerule: " + rule);
    }

    public void initGameRulesWorldLoad(WorldServer world) {
        for (Map.Entry<String, String> entry : gameruleMap.entrySet()) {
            world.getGameRules()
                .setOrCreateGameRule(entry.getKey(), entry.getValue());
        }
    }

    public void addGameruleInfo(JsonObject json) {
        if (json.has(GAMERULES)) {
            JsonObject gamerules = json.get(GAMERULES)
                .getAsJsonObject();

            // Iterate over each entry in the "gamerules" entries of our json.
            for (Map.Entry<String, JsonElement> entry : gamerules.entrySet()) {
                String ruleName = entry.getKey();
                String ruleValue = entry.getValue()
                    .getAsString();

                registerGameRule(ruleName, ruleValue);
            }
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Check if the objects are the same instance
        if (obj == null || getClass() != obj.getClass()) return false; // Check for null or different class
        TestSettings that = (TestSettings) obj;
        return Objects.equals(gameruleMap, that.gameruleMap); // Compare the gameruleMap content
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameruleMap); // Generate hash based on the gameruleMap
    }
}
