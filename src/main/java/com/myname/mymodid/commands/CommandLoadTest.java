package com.myname.mymodid.commands;

import com.myname.mymodid.MyMod;
import com.myname.mymodid.Test;
import com.myname.mymodid.procedures.AddItems;
import com.myname.mymodid.procedures.Procedure;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;

import java.util.ArrayList;
import java.util.Random;

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
        Random random = new Random();
        test.startX = random.nextInt(65) - 32;
        test.startY = random.nextInt(30);
        test.startZ = random.nextInt(65) - 32;

        test.buildStructure();
        AddItems addItems = new AddItems();
        addItems.x = 0;
        addItems.y = 0;
        addItems.z = 2;
        addItems.itemsToAdd = new ArrayList<>();
        addItems.itemsToAdd.add(new ItemStack(Items.diamond, 15));
        addItems.handleEvent(test);

    }
}
