package com.gtnewhorizons.CTF.tests;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class TestAreaChunkLoaderController {

    private static final int chunkMinX = 0;
    private static final int chunkMinZ = 0;
    public static int chunkMaxX = 1;
    public static int chunkMaxZ = 1;

    public static void loadAllRelevantChunks(World world) {

        for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
            for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                if (!world.getChunkProvider().chunkExists(chunkX, chunkZ)) {
                    // Force load the chunk if it's not already loaded
                    world.getChunkProvider().loadChunk(chunkX, chunkZ);
                }
            }
        }
    }

    public static void clearOutTestZone(World world, int buffer) {
        for (int x = -buffer; x < chunkMaxX * 16 + buffer; x++) {
            for (int z = -buffer; z < chunkMaxX * 16 + buffer; z++) {
                for (int y = 0; y <= 256; y++) {
                    world.setBlock(x,y,z, Blocks.air);
                }
            }
        }
    }
}
