package com.gtnewhorizons.CTF.commands;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_NAME;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isCTFWandRegionNotDefined;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

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

            CurrentTestUnderConstruction.updateTest(player.getUniqueID().toString(), currentTest);
        }
    }
}
