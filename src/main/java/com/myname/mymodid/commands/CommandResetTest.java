package com.myname.mymodid.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import static com.myname.mymodid.commands.CommandInitTest.currentTest;
import static com.myname.mymodid.events.CTFWandEventHandler.firstPosition;
import static com.myname.mymodid.events.CTFWandEventHandler.secondPosition;

public class CommandResetTest  extends CommandBase {
    @Override
    public String getCommandName() {
        return "resettest";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/resettest";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (currentTest != null) {
            resetTest();
            sender.addChatMessage(new ChatComponentText("Test in construction was aborted and not saved."));
        } else {
            sender.addChatMessage(new ChatComponentText("There is no test in construction to abort!"));
        }

    }

    public static void resetTest() {
        firstPosition[0] = Integer.MAX_VALUE;
        firstPosition[1] = Integer.MAX_VALUE;
        firstPosition[2] = Integer.MAX_VALUE;

        secondPosition[0] = Integer.MAX_VALUE;
        secondPosition[1] = Integer.MAX_VALUE;
        secondPosition[2] = Integer.MAX_VALUE;

        currentTest = null;
    }
}
