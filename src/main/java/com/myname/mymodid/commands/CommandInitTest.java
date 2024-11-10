package com.myname.mymodid.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.myname.mymodid.NBTConverter;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

import static com.myname.mymodid.CommonTestFields.ENCODED_NBT;
import static com.myname.mymodid.CommonTestFields.INSTRUCTIONS;
import static com.myname.mymodid.CommonTestFields.STRUCTURE;
import static com.myname.mymodid.CommonTestFields.TEST_NAME;
import static com.myname.mymodid.events.CTFWandEventHandler.firstPosition;
import static com.myname.mymodid.events.CTFWandEventHandler.secondPosition;
import static com.myname.mymodid.utils.Structure.captureStructureJson;

public class CommandInitTest extends CommandBase {

    public static JsonObject currentTest;

    @Override
    public String getCommandName() {
        return "inittest";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "inittest testname";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText("Must provide a valid test name. You may not use spaces."));
            return;
        }

        if (firstPosition[0] == Integer.MAX_VALUE || secondPosition[0] == Integer.MAX_VALUE) {
            sender.addChatMessage(new ChatComponentText("You have not selected a valid region using the CTF wand."));
            return;
        }

        currentTest = new JsonObject();
        currentTest.addProperty(TEST_NAME, args[0]);

        // Structure is saved at the end.
        currentTest.add(INSTRUCTIONS, new JsonArray());
    }



}
