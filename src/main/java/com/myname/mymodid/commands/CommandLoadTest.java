package com.myname.mymodid.commands;

import com.myname.mymodid.MyMod;
import com.myname.mymodid.Test;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;

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
        Test test = MyMod.tests.get(0);
        ChunkCoordinates coords = sender.getPlayerCoordinates();
        test.startX = coords.posX;
        test.startY = coords.posY;
        test.startZ = coords.posZ;


        if (test.areChunksLoadedForStructure()) {
            test.buildStructure();
        } else {
            sender.addChatMessage(new ChatComponentText("This test exceeds the loaded area, it cannot be instantiated. It is likely too big."));
        }
    }
}
