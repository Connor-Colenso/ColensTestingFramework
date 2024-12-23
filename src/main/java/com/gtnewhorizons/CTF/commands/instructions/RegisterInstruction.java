package com.gtnewhorizons.CTF.commands.instructions;

import static com.gtnewhorizons.CTF.utils.PrintUtils.notifyPlayer;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;

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
            handler.createAndAddInstruction(player, args);
            return true;
        } else {
            return false;
        }
    }

    public static void informPlayerOfOptions(EntityPlayerMP player) {
        for (String instruction : instructions.keySet()) {
            notifyPlayer(player, instruction);
        }
    }
}
