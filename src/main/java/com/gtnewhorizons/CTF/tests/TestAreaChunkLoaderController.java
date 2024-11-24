package com.gtnewhorizons.CTF.tests;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class TestAreaChunkLoaderController {

    /**
     * Loads all chunks that intersect with the given AxisAlignedBB.
     *
     * @param world The current World object.
     * @param totalTestAreaBounds The AxisAlignedBB representing the area.
     */
    public static void loadAllRelevantChunks(World world, AxisAlignedBB totalTestAreaBounds) {
        int chunkMinX = (int) Math.floor(totalTestAreaBounds.minX) >> 4;
        int chunkMaxX = (int) Math.floor(totalTestAreaBounds.maxX) >> 4;
        int chunkMinZ = (int) Math.floor(totalTestAreaBounds.minZ) >> 4;
        int chunkMaxZ = (int) Math.floor(totalTestAreaBounds.maxZ) >> 4;

        for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
            for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                if (!world.getChunkProvider().chunkExists(chunkX, chunkZ)) {
                    // Force load the chunk if it's not already loaded
                    world.getChunkProvider().loadChunk(chunkX, chunkZ);
                }
            }
        }
    }
}
