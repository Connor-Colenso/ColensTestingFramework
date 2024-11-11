package com.gtnewhorizons.CTF.commands.instructions;

import com.google.gson.JsonArray;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashMap;

import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;

public class RegisterInstruction {
    private static final HashMap<String, InstructionHandler> instructions = new HashMap<>();

    public static void register(String name, InstructionHandler handler) {
        InstructionHandler existingHandler = instructions.putIfAbsent(name.toLowerCase(), handler);
        if (existingHandler != null) {
            throw new IllegalArgumentException("Instruction with name '" + name + "' is already registered!");
        }
    }

    public static boolean execute(String name, EntityPlayerMP player, String[] args, JsonArray instructionArray) {
        InstructionHandler handler = instructions.get(name.toLowerCase());
        if (handler != null) {
            handler.execute(player, args, instructionArray);
            return true;
        } else {
            return false;
        }
    }

    // Needs description really...
    public static void informPlayerOfOptions(EntityPlayerMP player) {
        for (String instruction : instructions.keySet()) {
            notifyPlayer(player, instruction);
        }
    }
}
