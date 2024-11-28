package com.gtnewhorizons.CTF.tests;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.BUFFER_ZONE_IN_BLOCKS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.DIMENSION;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.FORCE_SEPARATE_RUNNING;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.GAMERULES;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.PRESERVE_VERTICAL;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.RUN_CONSOLE_COMMANDS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.minecraft.world.WorldServer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TestSettings {

    private final HashMap<String, String> gameruleMap = new HashMap<>();
    private final List<String> runConsoleCommands = new ArrayList<>();
    private boolean forceSeparateRunning = false;
    private boolean preserveVertical = false;
    private int dimension = 0;
    private int bufferZoneInBlocks = 1;

    public TestSettings() {}

    public TestSettings(JsonObject testConfig) {

        if (testConfig.has(GAMERULES)) {
            addGameruleInfo(
                testConfig.get(GAMERULES)
                    .getAsJsonObject());
        }

        if (testConfig.has(RUN_CONSOLE_COMMANDS)) {
            addCommandsToRunInConsole(
                testConfig.get(RUN_CONSOLE_COMMANDS)
                    .getAsJsonArray());
        }

        if (testConfig.has(FORCE_SEPARATE_RUNNING)) {
            forceSeparateRunning = testConfig.get(FORCE_SEPARATE_RUNNING)
                .getAsBoolean();
        }

        if (testConfig.has(PRESERVE_VERTICAL)) {
            preserveVertical = testConfig.get(PRESERVE_VERTICAL)
                .getAsBoolean();
        }

        if (testConfig.has(DIMENSION)) {
            dimension = testConfig.get(DIMENSION)
                .getAsInt();
        }

        if (testConfig.has(BUFFER_ZONE_IN_BLOCKS)) {
            bufferZoneInBlocks = testConfig.get(BUFFER_ZONE_IN_BLOCKS)
                .getAsInt();
        }

    }

    private void addCommandsToRunInConsole(JsonArray jsonArray) {
        for (JsonElement consoleCommands : jsonArray) {
            runConsoleCommands.add(consoleCommands.getAsString());
        }
    }

    public void registerGameRule(String rule, String state) {
        String doesExist = gameruleMap.putIfAbsent(rule, state);
        if (doesExist != null) throw new RuntimeException("Duplicate gamerule: " + rule);
    }

    public void initGameRules(WorldServer world) {
        for (Map.Entry<String, String> entry : gameruleMap.entrySet()) {
            world.getGameRules()
                .setOrCreateGameRule(entry.getKey(), entry.getValue());
        }
    }

    public void addGameruleInfo(JsonObject gamerules) {
        // Iterate over each entry in the "gamerules" entries of our json.
        for (Map.Entry<String, JsonElement> entry : gamerules.entrySet()) {
            String ruleName = entry.getKey();
            String ruleValue = entry.getValue()
                .getAsString();

            registerGameRule(ruleName, ruleValue);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Same instance check.
        if (obj == null || getClass() != obj.getClass()) return false; // Null or class type check.

        TestSettings that = (TestSettings) obj;

        // If either instance requires separate running, they are not equal.
        if (this.forceSeparateRunning || that.forceSeparateRunning) return false;

        // Compare gameruleMap and runConsoleCommands content.
        return Objects.equals(this.gameruleMap, that.gameruleMap)
            && Objects.equals(this.runConsoleCommands, that.runConsoleCommands);
    }

    @Override
    public int hashCode() {
        // Hash computation includes all relevant fields.
        return Objects.hash(gameruleMap, runConsoleCommands, forceSeparateRunning);
    }

    public void runConsoleCommands() {
        // Idk figure this out.
    }

    // Generic stuff.

    public boolean isForceSeparateRunning() {
        return forceSeparateRunning;
    }

    public int getDimension() {
        return dimension;
    }

    public int getBufferZoneInBlocks() {
        return bufferZoneInBlocks;
    }

    public boolean isPreserveVertical() {
        return preserveVertical;
    }

}
