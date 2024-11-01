package com.myname.mymodid.commands.instructions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class RunTicksInstruction {

    public static void add(ICommandSender sender, String[] args, JsonArray instructions) {
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText("Usage: addinstruction runTicks X"));
        } else {
            // Example logic to handle runTicks
            String ticks = args[1];

            try {
                int duration = Integer.parseInt(ticks); // Try to parse ticks as an integer
                sender.addChatMessage(new ChatComponentText("Added command to run for " + duration + " ticks."));

                JsonObject runTicks = new JsonObject();
                runTicks.addProperty("type", "runTicks");
                runTicks.addProperty("duration", duration); // Use the parsed integer

                instructions.add(runTicks);

            } catch (NumberFormatException e) {
                // Handle the case where ticks is not a valid integer
                sender.addChatMessage(new ChatComponentText("Invalid ticks: " + ticks));
            }
        }
    }
}
