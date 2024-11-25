package com.gtnewhorizons.CTF.utils.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static com.gtnewhorizons.CTF.utils.rendering.RegionRendering.renderFloatingText;

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

    List<String> frameText = new ArrayList<>();

    public RenderFrameBuilder addText(List<String> text) {
        frameText.addAll(text);
        return this;
    }

    public RenderFrameBuilder addText(String text) {
        frameText.add(text);
        return this;
    }

    public RenderFrameBuilder setColour(double red, double green, double blue, double alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        return this;
    }

    public RenderFrameBuilder setInterpolation(EntityPlayer entityPlayer, RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        interpX = player.prevPosX + (player.posX - player.prevPosX) * event.partialTicks;
        interpY = player.prevPosY + (player.posY - player.prevPosY) * event.partialTicks;
        interpZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.partialTicks;
        return this;
    }

    public RenderFrameBuilder setFrame(AxisAlignedBB aabb) {
        xMin = aabb.minX;
        yMin = aabb.minY;
        zMin = aabb.minZ;
        xMax = aabb.maxX;
        yMax = aabb.maxY;
        zMax = aabb.maxZ;
        return this;
    }

    public RenderFrameBuilder setFrame(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.zMin = zMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.zMax = zMax;
        return this;
    }


    public RenderFrameBuilder setColourAccordingToCoords() {
        // Ensure that each color component is within the 0-1 range. Some magic idk, found it online. Works though. Primes?
        red = (xMin * 31 + yMin * 17 + zMin * 13) % 256 / 255.0;
        green = (xMax * 7 + yMax * 3 + zMax * 5) % 256 / 255.0;
        blue = ((xMin + yMin + zMin + xMax + yMax + zMax) * 13) % 256 / 255.0;

        return this;
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

        if (!frameText.isEmpty()) {
            renderFloatingText(frameText, (xMin + xMax)/2.0, (yMin + yMax)/2.0, (zMin + zMax)/2.0);
        }
    }

}
