package com.gtnewhorizons.CTF.commands;

import static com.gtnewhorizons.CTF.commands.CommandResetTest.resetTest;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.STRUCTURE;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_NAME;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isTestNotStarted;
import static com.gtnewhorizons.CTF.utils.Structure.captureStructureJson;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import com.gtnewhorizons.CTF.utils.JsonUtils;
import net.minecraft.entity.player.EntityPlayer;

public class CommandCompleteTest extends CommandBase {

    @Override
    public String getCommandName() {
        return "completetest";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/completetest";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayer entityPlayer) {

            if (isTestNotStarted(entityPlayer)) {
                notifyPlayer(entityPlayer, "No test is in construction!");
                return;
            }

            // captureStructureJson will obtain it from the static coordinates set by the CTF Wand (though the event
            // CTFWandEventHandler actually sets the coords).
            // The region itself is drawn by RenderCTFWandFrame and the info in it by RenderCTFRegionInfo.
            JsonObject currentTest = CurrentTestUnderConstruction.getTestJson(entityPlayer);
            currentTest.add(STRUCTURE, captureStructureJson());

            // Save the test.
            JsonUtils.saveJsonToFile(currentTest);
            notifyPlayer(
                entityPlayer,
                "Test completed and saved to config/CTF/testing/" + currentTest.get(TEST_NAME)
                    .getAsString() + ".json");

            // Reset all info for next test.
            resetTest(entityPlayer);
        }
    }
}
