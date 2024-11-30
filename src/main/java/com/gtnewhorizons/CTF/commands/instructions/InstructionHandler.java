package com.gtnewhorizons.CTF.commands.instructions;

import net.minecraft.entity.player.EntityPlayerMP;

import com.google.gson.JsonArray;

@FunctionalInterface
public interface InstructionHandler {

    void addToInstructionArray(EntityPlayerMP player, String[] args, JsonArray instructionArray);
}
