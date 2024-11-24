package com.gtnewhorizons.CTF.utils;

import net.minecraft.util.AxisAlignedBB;

import static com.gtnewhorizons.CTF.commands.CommandInitTest.currentTest;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;

public class RegionUtils {

    public static boolean isOutsideRegion(int x, int y, int z) {

        if (isCTFWandRegionNotDefined()) return true;

        // Determine the minimum and maximum bounds for the region
        int minX = Math.min(firstPosition[0], secondPosition[0]);
        int maxX = Math.max(firstPosition[0], secondPosition[0]);
        int minY = Math.min(firstPosition[1], secondPosition[1]);
        int maxY = Math.max(firstPosition[1], secondPosition[1]);
        int minZ = Math.min(firstPosition[2], secondPosition[2]);
        int maxZ = Math.max(firstPosition[2], secondPosition[2]);

        // Check if the block coordinates are within these bounds
        return (x < minX || x > maxX) || (y < minY || y > maxY) || (z < minZ || z > maxZ);
    }

    public static boolean isCTFWandRegionNotDefined() {
        return firstPosition[0] == Integer.MAX_VALUE || secondPosition[0] == Integer.MAX_VALUE;
    }

    public static boolean isTestNotStarted() {
        return currentTest == null;
    }

    /**
     * Checks if the second AABB (inner) is fully contained within the first AABB (outer).
     *
     * @param outer The outer AxisAlignedBB.
     * @param inner The inner AxisAlignedBB to check.
     * @return true if the inner AABB is fully inside the outer AABB, including touching the edges.
     */
    public static boolean isFullyContained(AxisAlignedBB outer, AxisAlignedBB inner) {
        return inner.minX >= outer.minX &&
            inner.maxX <= outer.maxX &&
            inner.minY >= outer.minY &&
            inner.maxY <= outer.maxY &&
            inner.minZ >= outer.minZ &&
            inner.maxZ <= outer.maxZ;
    }
}
