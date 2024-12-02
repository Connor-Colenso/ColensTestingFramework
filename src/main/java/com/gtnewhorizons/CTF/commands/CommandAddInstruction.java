package com.gtnewhorizons.CTF.commands;

import static com.gtnewhorizons.CTF.commands.instructions.RegisterInstruction.addToInstructions;
import static com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction.isTestNotStarted;
import static com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction.removeInstruction;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isCTFWandRegionNotDefined;

import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.google.gson.JsonArray;
import com.gtnewhorizons.CTF.commands.instructions.RegisterInstruction;

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

        if (sender instanceof EntityPlayerMP player) {

            if (isCTFWandRegionNotDefined()) {
                notifyPlayer(player, "No region has been selected.");
                return;
            }

            if (isTestNotStarted(player)) {
                notifyPlayer(
                    player,
                    "This cannot be used if no test has been initialised. Use /inittest with a valid region selected.");
                return;
            }

            // Check if no arguments were provided
            if (args.length == 0) {
                // Print out the list of instructions and their descriptions
                notifyPlayer(player, "Valid instructions are:");
                RegisterInstruction.informPlayerOfOptions(player);
                return; // Exit after listing instructions
            }

            // Process the first argument as a command (case-insensitive)
            String command = args[0].toLowerCase();

            if (command.equals("undo")) {
                if (CurrentTestUnderConstruction.removeLastInstruction(player)) {
                    notifyPlayer(player, "Undo successful.");
                } else {
                    notifyPlayer(player, "Could not undo, instruction list is empty.");
                }
                return;
            }

            if (!addToInstructions(command, player, args)) {
                notifyPlayer(
                    player,
                    "Unknown instruction " + command + ". Use 'addinstruction' to list valid instructions.");
            }
        }
    }
}
