package com.myname.mymodid.rendering;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import static com.myname.mymodid.events.CTFWandEventHandler.firstPosition;
import static com.myname.mymodid.events.CTFWandEventHandler.secondPosition;

public class RenderCTFRegionInfo {

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (firstPosition[0] == Integer.MAX_VALUE || secondPosition[0] == Integer.MAX_VALUE) return;

        // Calculate the center position for the text
        double x = (firstPosition[0] + secondPosition[0]) / 2.0 + 0.5; // Center in x
        double y = Math.max(firstPosition[1], secondPosition[1]) + 1.5; // Slightly above
        double z = (firstPosition[2] + secondPosition[2]) / 2.0 + 0.5; // Center in z

        // Render the floating text
        renderFloatingText(
            "test",
            x, y, z,
            0xFFFFFF,             // Color (white)
            true,                       // Show background
            event.partialTicks,         // Use the event's partialTicks
            false                       // Disable depth test to render on top
        );
    }

    private static final Minecraft mc = Minecraft.getMinecraft();

    /**
     * Renders text at specified coordinates in the world
     * @param text The text to render
     * @param x The x coordinate in the world
     * @param y The y coordinate in the world
     * @param z The z coordinate in the world
     * @param color The color of the text (0xRRGGBB format)
     * @param renderBlackBackground Whether to render a black background behind the text
     * @param partialTicks The partial tick time for smooth rendering
     * @param depthTest Whether to enable depth testing (true = text can be hidden behind blocks)
     */
    public static void renderFloatingText(String text, double x, double y, double z, int color,
                                          boolean renderBlackBackground, float partialTicks, boolean depthTest) {
        FontRenderer fontrenderer = mc.fontRenderer;
        RenderManager renderManager = RenderManager.instance;

        // Save the current GL state
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // Translate to the world position
        float dx = (float)(x - RenderManager.renderPosX);
        float dy = (float)(y - RenderManager.renderPosY);
        float dz = (float)(z - RenderManager.renderPosZ);
        GL11.glTranslatef(dx, dy, dz);

        // Make the text face the player
        GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        // Scale the text to a reasonable size
        float scale = 0.02666667F;
        GL11.glScalef(-scale, -scale, scale);

        // Set up GL states
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false); // Disable depth writing for the text
        GL11.glDisable(GL11.GL_DEPTH_TEST); // Disable depth test to ensure text appears above blocks
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Calculate text dimensions
        int textWidth = fontrenderer.getStringWidth(text);
        int textHeight = 10;

        // Render background if enabled
        if (renderBlackBackground) {
            int backgroundColor = 0x40000000;
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            GL11.glPushMatrix();
            // Center the rectangle based on text width and height, setting it slightly larger than the text
            GL11.glTranslatef(-textWidth / 2 - 2, -2, 0);
            drawRect(0, 0, textWidth + 4, textHeight + 2, backgroundColor);

            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        // Render the text
        fontrenderer.drawString(text, -textWidth / 2, 0, color);

        // Restore GL state
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Re-enable depth test after rendering
        GL11.glDepthMask(true); // Re-enable depth writing
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    /**
     * Helper method to draw a rectangle
     */
    private static void drawRect(int left, int top, int right, int bottom, int color) {
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;

        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(left, bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, top);
        GL11.glVertex2f(left, top);
        GL11.glEnd();
    }
}
