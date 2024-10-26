package com.myname.mymodid.rendering;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import static com.myname.mymodid.events.CTFWandEventHandler.firstPosition;
import static com.myname.mymodid.events.CTFWandEventHandler.secondPosition;

// Client side only rendering.
public class RenderCTFWandFrame {

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (firstPosition[0] == Integer.MAX_VALUE || secondPosition[0] == Integer.MAX_VALUE) {
            return; // Skip rendering if positions are not set
        }

        // Sort positions to make rendering easier and adjust for block encapsulation
        double xMin = Math.min(firstPosition[0], secondPosition[0]);
        double yMin = Math.min(firstPosition[1], secondPosition[1]);
        double zMin = Math.min(firstPosition[2], secondPosition[2]);
        double xMax = Math.max(firstPosition[0], secondPosition[0]) + 1.0;
        double yMax = Math.max(firstPosition[1], secondPosition[1]) + 1.0;
        double zMax = Math.max(firstPosition[2], secondPosition[2]) + 1.0;

        // Interpolate player position for smoother rendering
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        double interpX = player.prevPosX + (player.posX - player.prevPosX) * event.partialTicks;
        double interpY = player.prevPosY + (player.posY - player.prevPosY) * event.partialTicks;
        double interpZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.partialTicks;

        // Push OpenGL state to prevent rendering conflicts
        GL11.glPushMatrix();

        // Translate to the correct player-relative position
        GL11.glTranslated(-interpX, -interpY, -interpZ);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(2.0f);

        // Set color (e.g., white)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        GL11.glBegin(GL11.GL_LINES);

        // Bottom face
        GL11.glVertex3d(xMin, yMin, zMin); GL11.glVertex3d(xMax, yMin, zMin);
        GL11.glVertex3d(xMax, yMin, zMin); GL11.glVertex3d(xMax, yMin, zMax);
        GL11.glVertex3d(xMax, yMin, zMax); GL11.glVertex3d(xMin, yMin, zMax);
        GL11.glVertex3d(xMin, yMin, zMax); GL11.glVertex3d(xMin, yMin, zMin);

        // Top face
        GL11.glVertex3d(xMin, yMax, zMin); GL11.glVertex3d(xMax, yMax, zMin);
        GL11.glVertex3d(xMax, yMax, zMin); GL11.glVertex3d(xMax, yMax, zMax);
        GL11.glVertex3d(xMax, yMax, zMax); GL11.glVertex3d(xMin, yMax, zMax);
        GL11.glVertex3d(xMin, yMax, zMax); GL11.glVertex3d(xMin, yMax, zMin);

        // Vertical edges
        GL11.glVertex3d(xMin, yMin, zMin); GL11.glVertex3d(xMin, yMax, zMin);
        GL11.glVertex3d(xMax, yMin, zMin); GL11.glVertex3d(xMax, yMax, zMin);
        GL11.glVertex3d(xMax, yMin, zMax); GL11.glVertex3d(xMax, yMax, zMax);
        GL11.glVertex3d(xMin, yMin, zMax); GL11.glVertex3d(xMin, yMax, zMax);

        GL11.glEnd();

        // Restore OpenGL state
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
    }
}
