package com.gtnewhorizons.CTF.commands.instructions;

import com.google.gson.JsonArray;
import net.minecraft.entity.player.EntityPlayerMP;

@FunctionalInterface
public interface InstructionHandler {
    void execute(EntityPlayerMP player, String[] args, JsonArray instructionArray);
}
