package com.myname.mymodid.commands.instructions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.HashMap;

import static com.myname.mymodid.commands.CommandInitTest.currentTest;

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

        if (currentTest == null) {
            sender.addChatMessage(new ChatComponentText("This cannot be used if no test has been initialised. Use /inittest with a valid region selected."));
            return;
        }

        // Check if no arguments were provided
        if (args.length == 0) {
            // Print out the list of instructions and their descriptions
            sender.addChatMessage(new ChatComponentText("Valid instructions are:"));
            for (String instruction : instructionsMap.keySet()) {
                sender.addChatMessage(new ChatComponentText(instruction + ": " + instructionsMap.get(instruction)));
            }
            return; // Exit after listing instructions
        }

        JsonArray instructionArray = currentTest.getAsJsonArray("instructions");

        // Process the first argument as a command (case-insensitive)
        String command = args[0].toLowerCase();
        switch (command) {
            case "runticks":
                RunTicksInstruction.add(sender, args, instructionArray);
                return;
            case "checktile":
                // Handle the "checkTile" instruction
                CheckTileInstructions.add(sender, args); // Instruction is added when item is used.
                return;
            case "additem":
                // Handle the "checkTile" instruction
                AddItemInstructions.add(sender); // Instruction is added when item is used.
                return;

            default:
                sender.addChatMessage(new ChatComponentText("Unknown instruction. Use 'addinstruction' to list valid instructions."));
                break;
        }

    }
}
