package com.gtnewhorizons.CTF.commands;

import com.google.gson.JsonArray;
import com.gtnewhorizons.CTF.commands.instructions.RegisterInstruction;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashMap;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.commands.CommandInitTest.currentTest;
import static com.gtnewhorizons.CTF.commands.instructions.RegisterInstruction.execute;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isRegionNotDefined;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isTestNotStarted;

public class CommandAddInstruction extends CommandBase {

    private static final HashMap<String, String> instructionsMap = new HashMap<>();

    static {
        instructionsMap.put("runTicks X", "This instruction will run the test for X ticks.");
        instructionsMap.put("checkTile name", "To use this, you must register a function with the name label. It will then return a true/false confirming if this check passed or failed.");
    }

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

        if (sender instanceof EntityPlayerMP player) {

            if (isRegionNotDefined()) {
                notifyPlayer(player, "No region has been selected.");
                return;
            }

            if (isTestNotStarted()) {
                notifyPlayer(player, "This cannot be used if no test has been initialised. Use /inittest with a valid region selected.");
                return;
            }

            // Check if no arguments were provided
            if (args.length == 0) {
                // Print out the list of instructions and their descriptions
                notifyPlayer(player,"Valid instructions are:");
                RegisterInstruction.informPlayerOfOptions(player);
                return; // Exit after listing instructions
            }

            JsonArray instructionArray = currentTest.getAsJsonArray(INSTRUCTIONS);

            // Process the first argument as a command (case-insensitive)
            String command = args[0].toLowerCase();

            if (execute(command, player, args, instructionArray)) {
                notifyPlayer(player, "Success.");
            } else {
                notifyPlayer(player, "Unknown instruction " + command + ". Use 'addinstruction' to list valid instructions.");
            }
        }
    }
}
