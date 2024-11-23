package com.gtnewhorizons.CTF.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class CommandLoadTest extends CommandBase {

    @Override
    public String getCommandName() {
        return "loadtest";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/loadtest testname";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // Test test = MyMod.tests.get(0);
        // test.buildStructure();
    }
}
