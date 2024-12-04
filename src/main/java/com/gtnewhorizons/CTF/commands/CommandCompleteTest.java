package com.gtnewhorizons.CTF.commands;

import static com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction.isTestNotStarted;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;

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

            CurrentTestUnderConstruction.completeTest(entityPlayer);
        }
    }
}
