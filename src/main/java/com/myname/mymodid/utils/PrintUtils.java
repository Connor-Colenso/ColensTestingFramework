package com.myname.mymodid.utils;

public class PrintUtils {

    public static void printGreen(String string) {
        System.out.println("\u001B[32m" + string + "\u001B[0m");
    }

    public static void printRed(String string) {
        System.out.println("\u001B[31m" + string + "\u001B[0m");
    }
}
