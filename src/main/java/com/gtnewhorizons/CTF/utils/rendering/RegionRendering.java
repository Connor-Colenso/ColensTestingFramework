package com.gtnewhorizons.CTF.utils.rendering;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

import org.lwjgl.opengl.GL11;

public class RegionRendering {

    public static void renderFloatingText(List<String> lines, double x, double y, double z) {
        FontRenderer fontrenderer = Minecraft.getMinecraft().fontRenderer;
        RenderManager renderManager = RenderManager.instance;

        // Save the current GL state
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // Translate to the world position
        float dx = (float) (x - RenderManager.renderPosX);
        float dy = (float) (y - RenderManager.renderPosY);
        float dz = (float) (z - RenderManager.renderPosZ);
        GL11.glTranslatef(dx, dy, dz);

        // Make the text face the player
        GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        // Scale the text to a reasonable size
        float scale = 0.02666667F;
        GL11.glScalef(-scale, -scale, scale);

        // Set up GL states
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Calculate total height and adjust starting Y position to center the text
        int totalHeight = (10 + 2) * lines.size(); // 10 is font height, 2 is padding
        int startY = -totalHeight / 2; // Center vertically

        // Render each line of text
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int textWidth = fontrenderer.getStringWidth(line);
            int textHeight = 10;

            GL11.glPushMatrix();
            // Center the rectangle based on text width and height
            GL11.glTranslatef(-textWidth / 2 - 2, startY + i * (textHeight + 2) - 2, 0);
            drawRect(textWidth + 3, textHeight + 2);
            GL11.glPopMatrix();
        }
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Render each line of text
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int textWidth = fontrenderer.getStringWidth(line);
            int yPos = startY + i * (10 + 2); // 10 is font height, 2 is padding
            fontrenderer.drawString(line, -textWidth / 2, yPos, 0xFFFFFF);
        }

        // Restore GL state
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    private static void drawRect(int right, int bottom) {
        float alpha = (float) (0x40000000 >> 24 & 255) / 255.0F;
        float red = (float) (0x40000000 >> 16 & 255) / 255.0F;
        float green = (float) (0x40000000 >> 8 & 255) / 255.0F;
        float blue = (float) (0x40000000 & 255) / 255.0F;

        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, 0);
        GL11.glVertex2f(0, 0);
        GL11.glEnd();
    }
}
