package com.gtnewhorizons.CTF.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import static com.gtnewhorizons.CTF.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.CommonTestFields.TEST_NAME;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;

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
