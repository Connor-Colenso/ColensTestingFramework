package com.gtnewhorizons.CTF.utils;

import java.util.HashMap;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.gtnewhorizons.CTF.MyMod;

@SuppressWarnings("unused")
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

    public static final HashMap<String, EnumChatFormatting> CONSOLE_COLOUR_TO_MINECRAFT_COLOUR = new HashMap<>();

    static {
        CONSOLE_COLOUR_TO_MINECRAFT_COLOUR.put(BLACK, EnumChatFormatting.BLACK);
        CONSOLE_COLOUR_TO_MINECRAFT_COLOUR.put(RED, EnumChatFormatting.RED);
        CONSOLE_COLOUR_TO_MINECRAFT_COLOUR.put(GREEN, EnumChatFormatting.GREEN);
        CONSOLE_COLOUR_TO_MINECRAFT_COLOUR.put(YELLOW, EnumChatFormatting.YELLOW);
        CONSOLE_COLOUR_TO_MINECRAFT_COLOUR.put(BLUE, EnumChatFormatting.BLUE);
        CONSOLE_COLOUR_TO_MINECRAFT_COLOUR.put(PURPLE, EnumChatFormatting.DARK_RED);
        CONSOLE_COLOUR_TO_MINECRAFT_COLOUR.put(CYAN, EnumChatFormatting.DARK_GREEN);
        CONSOLE_COLOUR_TO_MINECRAFT_COLOUR.put(WHITE, EnumChatFormatting.WHITE);
        CONSOLE_COLOUR_TO_MINECRAFT_COLOUR.put(RESET, EnumChatFormatting.RESET);
    }

    public static String colourConsole(final String colourCode, String string) {
        return colourCode + string + RESET;
    }

    public static void notifyPlayer(ICommandSender player, String message) {
        player.addChatMessage(new ChatComponentText(message));
    }

    public static void printSeparator() {
        MyMod.CTF_LOG.info("------------------------------------------------------------------");
    }
}
