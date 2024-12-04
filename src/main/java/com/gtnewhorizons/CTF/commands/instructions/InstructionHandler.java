package com.gtnewhorizons.CTF.commands.instructions;

import net.minecraft.entity.player.EntityPlayerMP;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface InstructionHandler {

    JsonObject createInstruction(EntityPlayerMP player, String[] args);
}
