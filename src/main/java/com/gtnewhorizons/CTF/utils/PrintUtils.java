package com.gtnewhorizons.CTF.utils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class PrintUtils {
    public static final String RESET = "\u001B[0m";

    // Regular Colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static void printColourConsole(final String colourCode, String string) {
        System.out.println(colourCode + string + RESET);
    }

    public static void notifyPlayer(ICommandSender player, String message) {
        player.addChatMessage(new ChatComponentText(message));
    }
}
