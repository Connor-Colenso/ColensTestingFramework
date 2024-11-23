package com.gtnewhorizons.CTF.utils;

import java.util.Random;

public class RandomNums {

    private static final Random RAND = new Random();

    public static int getRandomIntInRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min cannot be greater than max");
        }
        return RAND.nextInt((max - min) + 1) + min;
    }

}
