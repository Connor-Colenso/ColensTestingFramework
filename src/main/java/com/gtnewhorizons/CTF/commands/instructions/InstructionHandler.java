package com.gtnewhorizons.CTF.commands.instructions;

import net.minecraft.entity.player.EntityPlayerMP;

import com.google.gson.JsonArray;

@FunctionalInterface
public interface InstructionHandler {

    void execute(EntityPlayerMP player, String[] args, JsonArray instructionArray);
}
