package com.gtnewhorizons.CTF.utils.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gtnewhorizons.CTF.utils.rendering.RegionRendering.renderFloatingText;

public class RenderFrameBuilder {

    // Default white colour for frame.
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

    // ----------------------------------------
    // Text stuff.
    // ----------------------------------------

    // Map to store text and its coordinates
    private final Map<String, List<String>> textMap = new HashMap<>();

    // Core method handling text addition
    private RenderFrameBuilder addTextInternal(List<String> texts, double x, double y, double z) {
        String key = x + "," + y + "," + z; // Unique key for each coordinate
        textMap.computeIfAbsent(key, k -> new ArrayList<>()).addAll(texts);
        return this;
    }

    // Public methods cascading to the core method
    public RenderFrameBuilder addText(String text, double x, double y, double z) {
        return addTextInternal(Collections.singletonList(text), x, y, z);
    }

    public RenderFrameBuilder addCentralText(String text) {
        return addText(text, (xMin + xMax) / 2.0, (yMin + yMax) / 2.0, (zMin + zMax) / 2.0);
    }

    public RenderFrameBuilder addText(List<String> texts, double x, double y, double z) {
        return addTextInternal(texts, x, y, z);
    }

    public RenderFrameBuilder addCentralText(List<String> texts) {
        return addText(texts, (xMin + xMax) / 2.0, (yMin + yMax) / 2.0, (zMin + zMax) / 2.0);
    }

    // ----------------------------------------

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

        // Iterate over map of text to render, and unpack the coordinates, then render to screen.
        if (!textMap.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : textMap.entrySet()) {
                // Extract x, y, and z from the key
                String[] coords = entry.getKey().split(",");
                double x = Double.parseDouble(coords[0]);
                double y = Double.parseDouble(coords[1]);
                double z = Double.parseDouble(coords[2]);

                // Render the floating text at the specified coordinates
                renderFloatingText(entry.getValue(), x, y, z);
            }
        }
    }
}
