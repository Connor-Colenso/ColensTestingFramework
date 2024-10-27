package com.myname.mymodid.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import static com.myname.mymodid.CommonTestFields.TEST_NAME;
import static com.myname.mymodid.commands.CommandInitTest.currentTest;
import static com.myname.mymodid.events.CTFWandEventHandler.firstPosition;
import static com.myname.mymodid.events.CTFWandEventHandler.secondPosition;

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
        firstPosition[0] = Integer.MAX_VALUE;
        firstPosition[1] = Integer.MAX_VALUE;
        firstPosition[2] = Integer.MAX_VALUE;

        secondPosition[0] = Integer.MAX_VALUE;
        secondPosition[1] = Integer.MAX_VALUE;
        secondPosition[2] = Integer.MAX_VALUE;

        CommandCaptureStructure.saveJsonToFile(currentTest);
        sender.addChatMessage(new ChatComponentText("Test completed and saved to config/CTF/testing/" + currentTest.get(TEST_NAME) + "."));

        currentTest = null;

    }
}
