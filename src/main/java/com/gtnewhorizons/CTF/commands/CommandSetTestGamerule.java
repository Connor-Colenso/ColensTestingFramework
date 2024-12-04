package com.gtnewhorizons.CTF.commands;

import static com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction.isTestNotStarted;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.ALL_GAMERULES_DEFAULT;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.ALL_GAMERULES_LOWER_CONVERTER;
import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isCTFWandRegionNotDefined;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;

public class CommandSetTestGamerule extends CommandBase {

    @Override
    public String getCommandName() {
        return "addtestgamerule";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "addtestgamerule <gamerule_name> <value>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayerMP player) {

            // Check if a region is defined with the CTF wand
            if (isCTFWandRegionNotDefined()) {
                notifyPlayer(player, "No region has been selected.");
                return;
            }

            // Check if the test is started
            if (isTestNotStarted(player)) {
                notifyPlayer(
                    player,
                    "This cannot be used if no test has been initialised. Use /inittest with a valid region selected.");
                return;
            }

            // Validate the gamerule argument
            if (args.length != 2) {
                notifyPlayer(player, "Invalid arguments. Usage: /addtestgamerule <gamerule_name> <value>");
                return;
            }

            String gamerule = args[0].toLowerCase(); // Making gamerule case-insensitive
            String valueString = args[1].toLowerCase();

            // Check if the gamerule exists.
            if (!ALL_GAMERULES_LOWER_CONVERTER.containsKey(gamerule)) {
                notifyPlayer(player, "Invalid gamerule: " + gamerule);
                return;
            }

            // Convert the value argument to a boolean
            boolean value;
            if (valueString.equals("true")) {
                value = true;
            } else if (valueString.equals("false")) {
                value = false;
            } else {
                notifyPlayer(player, "Invalid value. Please use 'true' or 'false'.");
                return;
            }

            // Set the gamerule for the current test
            CurrentTestUnderConstruction.setGamerule(player, ALL_GAMERULES_LOWER_CONVERTER.get(gamerule), value);
            notifyPlayer(player, "Gamerule '" + ALL_GAMERULES_LOWER_CONVERTER.get(gamerule) + "' set to " + value + ".");
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Tab completion for gamerules
            String prefix = args[0].toLowerCase();
            for (String gamerule : ALL_GAMERULES_DEFAULT.keySet()) {
                if (gamerule.toLowerCase()
                    .startsWith(prefix)) {
                    completions.add(gamerule);
                }
            }
        } else if (args.length == 2) {
            // Tab completion for values (true/false)
            String prefix = args[1].toLowerCase();
            if ("true".startsWith(prefix)) {
                completions.add("true");
            }
            if ("false".startsWith(prefix)) {
                completions.add("false");
            }
        }

        return completions;
    }
}
