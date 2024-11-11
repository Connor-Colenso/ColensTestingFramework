package com.gtnewhorizons.CTF.utils;

import static com.gtnewhorizons.CTF.commands.CommandInitTest.currentTest;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;

public class RegionUtils {
    public static boolean isWithinRegion(int x, int y, int z) {

        if (isRegionNotDefined()) return false;

        // Determine the minimum and maximum bounds for the region
        int minX = Math.min(firstPosition[0], secondPosition[0]);
        int maxX = Math.max(firstPosition[0], secondPosition[0]);
        int minY = Math.min(firstPosition[1], secondPosition[1]);
        int maxY = Math.max(firstPosition[1], secondPosition[1]);
        int minZ = Math.min(firstPosition[2], secondPosition[2]);
        int maxZ = Math.max(firstPosition[2], secondPosition[2]);

        // Check if the block coordinates are within these bounds
        return (x >= minX && x <= maxX) &&
            (y >= minY && y <= maxY) &&
            (z >= minZ && z <= maxZ);
    }

    public static boolean isRegionNotDefined() {
        return firstPosition[0] == Integer.MAX_VALUE || secondPosition[0] == Integer.MAX_VALUE;
    }

    public static boolean isTestNotStarted() {
        return currentTest == null;
    }
}
