package com.gtnewhorizons.CTF.rendering;

import static com.gtnewhorizons.CTF.TickHandler.uuidTestsMapping;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isCTFWandRegionNotDefined;

import com.gtnewhorizons.CTF.tests.Test;
import com.gtnewhorizons.CTF.utils.rendering.RenderFrameBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

// Client side only rendering.
public class RenderCTFFrames {

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        // Perform the ray trace once before the loop.
        MovingObjectPosition mop = player.rayTrace(5.0, 1.0F); // Reach distance and other rendering junk.
        Vec3 blockPlayerLookingAtVec = null;

        // Check if the ray trace hit a block.
        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            blockPlayerLookingAtVec = Vec3.createVectorHelper(mop.blockX + 0.5, mop.blockY + 0.5, mop.blockZ + 0.5);
        }

        // Iterate over all maps, and render ones which the player is either looking at a block within, or the player is within.
        for (Test test : uuidTestsMapping.values()) {
            // Define the bounding box for the test zone with buffer zone.
            AxisAlignedBB testRenderFrameWithBuffer = AxisAlignedBB.getBoundingBox(
                test.startX - test.bufferZone,
                test.startY - test.bufferZone,
                test.startZ - test.bufferZone,
                test.startX + test.xLength + test.bufferZone * 2 - 1,
                test.startY + test.yLength + test.bufferZone * 2 - 1,
                test.startZ + test.zLength + test.bufferZone * 2 - 1
            );

            // Check if the player intersects with the bounding box.
            boolean doesPlayerIntersectTestBounds = player.boundingBox.intersectsWith(testRenderFrameWithBuffer);

            // Check if the precomputed ray trace block is inside the current test bounding box.
            boolean playerLookingAtBlockInTestBounds = blockPlayerLookingAtVec != null && testRenderFrameWithBuffer.isVecInside(blockPlayerLookingAtVec);

            // Render the box if the player intersects or is looking at a block inside the test zone.
            if (doesPlayerIntersectTestBounds || playerLookingAtBlockInTestBounds) {
                RenderFrameBuilder renderFrameBuilder = new RenderFrameBuilder();
                renderFrameBuilder.setInterpolation(player, event);
                renderFrameBuilder.setFrame(testRenderFrameWithBuffer);
                renderFrameBuilder.setColourAccordingToCoords();
                renderFrameBuilder.render();
            }
        }

        // ----------------------------------------------------
        // CTF Frame rendering:
        // ----------------------------------------------------

        if (isCTFWandRegionNotDefined()) {
            return; // Skip rendering if positions are not set.
        }

        // Sort positions to make rendering easier and adjust for block encapsulation.
        double xMin = Math.min(firstPosition[0], secondPosition[0]);
        double yMin = Math.min(firstPosition[1], secondPosition[1]);
        double zMin = Math.min(firstPosition[2], secondPosition[2]);
        double xMax = Math.max(firstPosition[0], secondPosition[0]) + 1.0;
        double yMax = Math.max(firstPosition[1], secondPosition[1]) + 1.0;
        double zMax = Math.max(firstPosition[2], secondPosition[2]) + 1.0;

        // Render the CTF wand frame.
        RenderFrameBuilder renderFrameBuilder = new RenderFrameBuilder();
        renderFrameBuilder.setInterpolation(player, event);
        renderFrameBuilder.setFrame(xMin, yMin, zMin, xMax, yMax, zMax);
        renderFrameBuilder.render();
    }


}
