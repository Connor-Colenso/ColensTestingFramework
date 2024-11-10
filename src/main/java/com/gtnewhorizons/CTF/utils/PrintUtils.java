package com.gtnewhorizons.CTF.utils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class PrintUtils {

    public static void printConsoleGreen(String string) {
        System.out.println("\u001B[32m" + string + "\u001B[0m");
    }

    public static void printConsoleRed(String string) {
        System.out.println("\u001B[31m" + string + "\u001B[0m");
    }

    public static void notifyPlayer(ICommandSender player, String message) {
        player.addChatMessage(new ChatComponentText(message));
    }
}
