package com.gtnewhorizons.CTF.commands.instructions;

import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayerMP;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;

public class RegisterInstruction {

    private static final HashMap<String, InstructionHandler> instructions = new HashMap<>();

    public static void register(String name, InstructionHandler handler) {
        InstructionHandler existingHandler = instructions.putIfAbsent(name.toLowerCase(), handler);
        if (existingHandler != null) {
            throw new IllegalArgumentException("Instruction with name '" + name + "' is already registered!");
        }
    }

    public static boolean addToInstructions(String name, EntityPlayerMP player, String[] args) {
        InstructionHandler handler = instructions.get(name.toLowerCase());
        if (handler != null) {
            JsonObject procedure = handler.createInstruction(player, args);
            if (procedure != null) {
                CurrentTestUnderConstruction.addInstruction(player, procedure);
            }
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
