package com.gtnewhorizons.CTF.utils;

import java.util.HashMap;
import java.util.Map;

public class CommonTestFields {

    public static final String INSTRUCTIONS = "instructions";
    public static final String TEST_NAME = "testName";
    public static final String STRUCTURE = "structure";
    public static final String ENCODED_NBT = "encodedNBT";
    public static final String GAMERULES = "gamerules";
    public static final String TEST_CONFIG = "testConfig";
    public static final String RUN_CONSOLE_COMMANDS = "runConsoleCommands";
    public static final String FORCE_SEPARATE_RUNNING = "forceSeparateRunning";
    public static final String PRESERVE_VERTICAL = "preserveVertical";
    public static final String DIMENSION = "dimension";
    public static final String BUFFER_ZONE_IN_BLOCKS = "bufferZoneInBlocks";
    public static final String DUPLICATE_TEST = "duplicateTest";

    // AddFluids - A lot of this is just using whatever FluidStack defines, considering we use that for saving/loading.
    public static final String FLUID_AMOUNT = "Amount";
    public static final String FLUID_NAME = "FluidName";
    public static final String STORED_FLUIDS = "StoredFluids";

    // All gamerules with default values.
    public static final Map<String, Boolean> ALL_GAMERULES_DEFAULT = new HashMap<>();

    static {
        ALL_GAMERULES_DEFAULT.put("doFireTick", true);
        ALL_GAMERULES_DEFAULT.put("mobGriefing", true);
        ALL_GAMERULES_DEFAULT.put("keepInventory", false);
        ALL_GAMERULES_DEFAULT.put("doMobSpawning", true);
        ALL_GAMERULES_DEFAULT.put("doMobLoot", true);
        ALL_GAMERULES_DEFAULT.put("doTileDrops", true);
        ALL_GAMERULES_DEFAULT.put("commandBlockOutput", true);
        ALL_GAMERULES_DEFAULT.put("naturalRegeneration", true);
        ALL_GAMERULES_DEFAULT.put("doDaylightCycle", true);
    }

}
