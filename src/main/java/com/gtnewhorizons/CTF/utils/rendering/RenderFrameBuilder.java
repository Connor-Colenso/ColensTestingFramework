package com.gtnewhorizons.CTF.utils.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

public class RenderFrameBuilder {

    // Default white
    double red = 1.0f;
    double green = 1.0f;
    double blue = 1.0f;
    double alpha = 1.0f;

    double interpX;
    double interpY;
    double interpZ;

    double xMin;
    double yMin;
    double zMin;
    double xMax;
    double yMax;
    double zMax;

    public void setColour(double red, double green, double blue, double alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public void setColour(int red, int green, int blue) {
        setColour(red, green, blue, 1);
    }

    public void setInterpolation(EntityPlayer entityPlayer, RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        interpX = player.prevPosX + (player.posX - player.prevPosX) * event.partialTicks;
        interpY = player.prevPosY + (player.posY - player.prevPosY) * event.partialTicks;
        interpZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.partialTicks;
    }

    public void setFrame(AxisAlignedBB aabb) {
        xMin = aabb.minX;
        yMin = aabb.minY;
        zMin = aabb.minZ;
        xMax = aabb.maxX;
        yMax = aabb.maxY;
        zMax = aabb.maxZ;
    }

    public void setFrame(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.zMin = zMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.zMax = zMax;
    }

    public void render() {
        GL11.glPushMatrix();
        GL11.glTranslated(-interpX, -interpY, -interpZ);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(2.0f);

        // Colour of the frame itself.
        GL11.glColor4d(red, green, blue, alpha);

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

    public void setColourAccordingToCoords() {
        // Ensure that each color component is within the 0-1 range.
        red = (xMin * 31 + yMin * 17 + zMin * 13) % 256 / 255.0;
        green = (xMax * 7 + yMax * 3 + zMax * 5) % 256 / 255.0;
        blue = ((xMin + yMin + zMin + xMax + yMax + zMax) * 13) % 256 / 255.0;
    }
}
