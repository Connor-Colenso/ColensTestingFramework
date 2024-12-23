package com.gtnewhorizons.CTF.commands.instructions;

import net.minecraft.entity.player.EntityPlayerMP;

@FunctionalInterface
public interface InstructionHandler {

    void createAndAddInstruction(EntityPlayerMP player, String[] args);
}
