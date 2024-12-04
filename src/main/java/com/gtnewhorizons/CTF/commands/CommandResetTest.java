package com.gtnewhorizons.CTF.commands;

import static com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction.isTestNotStarted;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;

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
                CurrentTestUnderConstruction.resetTest(entityPlayer);
                notifyPlayer(player, "Test in construction was aborted and not saved.");
            }
        }
    }
}
