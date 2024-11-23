package com.gtnewhorizons.CTF.commands;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_NAME;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isRegionNotDefined;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class CommandInitTest extends CommandBase {

    public static JsonObject currentTest;

    @Override
    public String getCommandName() {
        return "inittest";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "inittest testname";
    }

    @Override
    public void processCommand(ICommandSender player, String[] args) {
        if (args.length == 0) {
            notifyPlayer(player, "Must provide a valid test name. You may not use spaces.");
            return;
        }

        if (isRegionNotDefined()) {
            notifyPlayer(player, "You have not selected a valid region using the CTF wand.");
            return;
        }

        currentTest = new JsonObject();
        currentTest.addProperty(TEST_NAME, args[0]);

        // Structure is saved at the end.
        currentTest.add(INSTRUCTIONS, new JsonArray());
    }

}
