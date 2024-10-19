package com.myname.mymodid.commands;

import com.myname.mymodid.MyMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class CommandSuspendTime extends CommandBase {

    @Override
    public String getCommandName() {
        return "suspendtime";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/suspendtime"; // Usage message
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        MyMod.magicStopTime = !MyMod.magicStopTime;
    }
}
