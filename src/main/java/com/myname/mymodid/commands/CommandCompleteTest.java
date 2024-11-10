package com.myname.mymodid.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import static com.myname.mymodid.CommonTestFields.STRUCTURE;
import static com.myname.mymodid.CommonTestFields.TEST_NAME;
import static com.myname.mymodid.commands.CommandInitTest.currentTest;
import static com.myname.mymodid.commands.CommandResetTest.resetTest;
import static com.myname.mymodid.events.CTFWandEventHandler.firstPosition;
import static com.myname.mymodid.events.CTFWandEventHandler.secondPosition;
import static com.myname.mymodid.utils.Structure.captureStructureJson;

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

        if (currentTest == null) {
            sender.addChatMessage(new ChatComponentText("No test is in construction!"));
            return;
        }

        // captureStructureJson will obtain it from the static coordinates set by the CTF Wand (though the event CTFWandEventHandler actually sets the coords).
        // The region itself is drawn by RenderCTFWandFrame and the info in it by RenderCTFRegionInfo.
        currentTest.add(STRUCTURE, captureStructureJson());

        // Save the test.
        CommandCaptureStructure.saveJsonToFile(currentTest);
        sender.addChatMessage(new ChatComponentText("Test completed and saved to config/CTF/testing/" + currentTest.get(TEST_NAME).getAsString() + ".json"));

        // Reset all info for next test.
        resetTest();
    }
}
