package com.gtnewhorizons.CTF.commands.instructions;

import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;

import net.minecraft.entity.player.EntityPlayerMP;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class RunTicksInstruction {

    @SuppressWarnings("unused")
    public static void add(EntityPlayerMP player, String[] args, JsonArray instructions) {
        if (args.length < 2) {
            notifyPlayer(player, "Usage: addinstruction runTicks X");
        } else {
            // Example logic to handle runTicks
            String ticks = args[1];

            try {
                int duration = Integer.parseInt(ticks); // Try to parse ticks as an integer
                notifyPlayer(player, "Added command to run for " + duration + " ticks.");

                JsonObject runTicks = new JsonObject();
                runTicks.addProperty("type", "runTicks");
                runTicks.addProperty("duration", duration); // Use the parsed integer

                // This adds directly, since we have no item associated.
                instructions.add(runTicks);

            } catch (NumberFormatException e) {
                // Handle the case where ticks is not a valid integer.
                notifyPlayer(player, "Invalid ticks: " + ticks);
            }
        }
    }
}
