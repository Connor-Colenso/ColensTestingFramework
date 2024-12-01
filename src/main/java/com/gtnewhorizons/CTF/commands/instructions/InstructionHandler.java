package com.gtnewhorizons.CTF.commands.instructions;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.procedures.Procedure;
import net.minecraft.entity.player.EntityPlayerMP;

@FunctionalInterface
public interface InstructionHandler {

    JsonObject createInstruction(EntityPlayerMP player, String[] args);
}
