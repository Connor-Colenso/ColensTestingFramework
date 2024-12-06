package com.gtnewhorizons.CTF.commands;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.ALL_GAMERULES_DEFAULT;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.GAMERULES;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_CONFIG;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_NAME;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isCTFWandRegionNotDefined;

import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;

public class CommandInitTest extends CommandBase {

    @Override
    public String getCommandName() {
        return "init_test";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "init_test <test_name> [template_name]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            notifyPlayer(sender, "Must provide a valid test name.");
            return;
        }

        if (isCTFWandRegionNotDefined()) {
            notifyPlayer(sender, "You have not selected a valid region using the CTF wand.");
            return;
        }

        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;

            JsonObject currentTest = new JsonObject();
            currentTest.addProperty(TEST_NAME, args[0]);
            currentTest.add(INSTRUCTIONS, new JsonArray());

            JsonObject testConfig = new JsonObject();

            testConfig.addProperty("dimension", 0);
            testConfig.addProperty("bufferZoneInBlocks", 1);
            testConfig.addProperty("preserveVertical", false);
            testConfig.addProperty("forceSeparateRunning", false);
            testConfig.addProperty("duplicateTest", 1);

            JsonObject gamerules = new JsonObject();
            for (Map.Entry<String, Boolean> pair : ALL_GAMERULES_DEFAULT.entrySet()) {
                gamerules.addProperty(pair.getKey(), pair.getValue());
            }

            testConfig.add(GAMERULES, gamerules);
            currentTest.add(TEST_CONFIG, testConfig);

            CurrentTestUnderConstruction.updateTest(
                player.getUniqueID()
                    .toString(),
                currentTest);
        }
    }
}
