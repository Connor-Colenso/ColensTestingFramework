package com.gtnewhorizons.CTF.commands;


import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isTestNotStarted;

import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CommandResetTest extends CommandBase {

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
        if (player instanceof EntityPlayer entityPlayer) {
            if (isTestNotStarted(entityPlayer)) {
                notifyPlayer(player, "There is no test in construction to abort!");
            } else {
                resetTest(entityPlayer);
                notifyPlayer(player, "Test in construction was aborted and not saved.");
            }
        }

    }

    public static void resetTest(EntityPlayer player) {
        firstPosition[0] = Integer.MAX_VALUE;
        firstPosition[1] = Integer.MAX_VALUE;
        firstPosition[2] = Integer.MAX_VALUE;

        secondPosition[0] = Integer.MAX_VALUE;
        secondPosition[1] = Integer.MAX_VALUE;
        secondPosition[2] = Integer.MAX_VALUE;

        CurrentTestUnderConstruction.removeTest(player);
    }
}
