package com.gtnewhorizons.CTF.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import static com.gtnewhorizons.CTF.commands.CommandInitTest.currentTest;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isTestNotStarted;

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
    public void processCommand(ICommandSender player, String[] args) {
        if (isTestNotStarted()) {
            notifyPlayer(player, "There is no test in construction to abort!");
        } else {
            resetTest();
            notifyPlayer(player, "Test in construction was aborted and not saved.");
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
