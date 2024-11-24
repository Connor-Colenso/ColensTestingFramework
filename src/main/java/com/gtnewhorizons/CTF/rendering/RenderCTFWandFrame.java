package com.gtnewhorizons.CTF.rendering;

import static com.gtnewhorizons.CTF.TickHandler.tmpTestsStorage;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isCTFWandRegionNotDefined;

import com.gtnewhorizons.CTF.tests.Test;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

// Client side only rendering.
public class RenderCTFWandFrame {

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onRenderWorldLast(RenderWorldLastEvent event) {

        // Interpolate player position for smoother rendering
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        double interpX = player.prevPosX + (player.posX - player.prevPosX) * event.partialTicks;
        double interpY = player.prevPosY + (player.posY - player.prevPosY) * event.partialTicks;
        double interpZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.partialTicks;


        for(Test test : tmpTestsStorage.values()) {
            renderBox(test.startX - test.bufferZone, test.startY - test.bufferZone, test.startZ - test.bufferZone, test.startX + test.xLength + test.bufferZone * 2-1, test.startY + test.yLength + test.bufferZone * 2-1, test.startZ + test.zLength + test.bufferZone * 2-1, interpX, interpY, interpZ);
        }


        if (isCTFWandRegionNotDefined()) {
            return; // Skip rendering if positions are not set
        }

        // Sort positions to make rendering easier and adjust for block encapsulation
        double xMin = Math.min(firstPosition[0], secondPosition[0]);
        double yMin = Math.min(firstPosition[1], secondPosition[1]);
        double zMin = Math.min(firstPosition[2], secondPosition[2]);
        double xMax = Math.max(firstPosition[0], secondPosition[0]) + 1.0;
        double yMax = Math.max(firstPosition[1], secondPosition[1]) + 1.0;
        double zMax = Math.max(firstPosition[2], secondPosition[2]) + 1.0;

        // Render the CTF frame.
        renderBox(xMin, yMin, zMin, xMax, yMax, zMax, interpX, interpY, interpZ);
    }

    /**
     * Renders a box with the given coordinates, applying player position interpolation.
     *
     * @param xMin     Minimum x-coordinate
     * @param yMin     Minimum y-coordinate
     * @param zMin     Minimum z-coordinate
     * @param xMax     Maximum x-coordinate
     * @param yMax     Maximum y-coordinate
     * @param zMax     Maximum z-coordinate
     * @param interpX  Interpolated player x-position
     * @param interpY  Interpolated player y-position
     * @param interpZ  Interpolated player z-position
     */
    private void renderBox(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax,
                           double interpX, double interpY, double interpZ) {
        GL11.glPushMatrix();
        GL11.glTranslated(-interpX, -interpY, -interpZ);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(2.0f);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f); // Set color (e.g., white)

        GL11.glBegin(GL11.GL_LINES);

        // Bottom face
        GL11.glVertex3d(xMin, yMin, zMin);
        GL11.glVertex3d(xMax, yMin, zMin);
        GL11.glVertex3d(xMax, yMin, zMin);
        GL11.glVertex3d(xMax, yMin, zMax);
        GL11.glVertex3d(xMax, yMin, zMax);
        GL11.glVertex3d(xMin, yMin, zMax);
        GL11.glVertex3d(xMin, yMin, zMax);
        GL11.glVertex3d(xMin, yMin, zMin);

        // Top face
        GL11.glVertex3d(xMin, yMax, zMin);
        GL11.glVertex3d(xMax, yMax, zMin);
        GL11.glVertex3d(xMax, yMax, zMin);
        GL11.glVertex3d(xMax, yMax, zMax);
        GL11.glVertex3d(xMax, yMax, zMax);
        GL11.glVertex3d(xMin, yMax, zMax);
        GL11.glVertex3d(xMin, yMax, zMax);
        GL11.glVertex3d(xMin, yMax, zMin);

        // Vertical edges
        GL11.glVertex3d(xMin, yMin, zMin);
        GL11.glVertex3d(xMin, yMax, zMin);
        GL11.glVertex3d(xMax, yMin, zMin);
        GL11.glVertex3d(xMax, yMax, zMin);
        GL11.glVertex3d(xMax, yMin, zMax);
        GL11.glVertex3d(xMax, yMax, zMax);
        GL11.glVertex3d(xMin, yMin, zMax);
        GL11.glVertex3d(xMin, yMax, zMax);

        GL11.glEnd();

        // Restore OpenGL state
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
    }
}
