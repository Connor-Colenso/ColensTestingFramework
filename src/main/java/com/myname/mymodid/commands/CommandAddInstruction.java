package com.myname.mymodid.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class CommandAddInstruction extends CommandBase {

    @Override
    public String getCommandName() {
        return "addinstruction";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "addinstruction instruction_name [other data]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

    }
}
