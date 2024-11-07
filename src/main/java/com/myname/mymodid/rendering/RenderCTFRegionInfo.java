package com.myname.mymodid.rendering;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static com.myname.mymodid.CommonTestFields.INSTRUCTIONS;
import static com.myname.mymodid.CommonTestFields.TEST_NAME;
import static com.myname.mymodid.commands.CommandInitTest.currentTest;
import static com.myname.mymodid.events.CTFWandEventHandler.firstPosition;
import static com.myname.mymodid.events.CTFWandEventHandler.secondPosition;

public class RenderCTFRegionInfo {

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (firstPosition[0] == Integer.MAX_VALUE || secondPosition[0] == Integer.MAX_VALUE) return;

        renderRegionLabel(event);
        renderTileEntityTagPoints(event);
        renderAddItemPoints(event);
    }

    private void renderAddItemPoints(RenderWorldLastEvent event) {
        if (firstPosition[0] == Integer.MAX_VALUE || secondPosition[0] == Integer.MAX_VALUE) return;
        if (currentTest == null) return;

        if (!currentTest.has(INSTRUCTIONS)) {
            return;
        }

        // Calculate the minimum coordinates for the bounding box
        double minX = Math.min(firstPosition[0], secondPosition[0]);
        double minY = Math.min(firstPosition[1], secondPosition[1]);
        double minZ = Math.min(firstPosition[2], secondPosition[2]);

        JsonArray instructionsArray = currentTest.getAsJsonArray(INSTRUCTIONS);

        for (int i = 0; i < instructionsArray.size(); i++) {
            JsonObject instruction = instructionsArray.get(i).getAsJsonObject();

            // Check if this instruction is of type "addItems"
            if (instruction.get("type").getAsString().equals("addItems")) {
                // Initialize a list to store text lines
                List<String> textList = new ArrayList<>();
                textList.add("Add Item(s)");

                // Add the optional label if it exists
                if (instruction.has("optionalLabel")) {
                    textList.add(instruction.get("optionalLabel").getAsString());
                }

                // Process each item in the "items" array
                JsonArray itemsArray = instruction.getAsJsonArray("items");
                for (int j = 0; j < itemsArray.size(); j++) {
                    JsonObject itemObject = itemsArray.get(j).getAsJsonObject();

                    // Extract item details, kinda hacky, but I don't see a good way around making an itemstack each time...
                    String registryName = itemObject.get("registryName").getAsString();
                    int stackSize = itemObject.get("stackSize").getAsInt();
                    int meta = itemObject.get("metadata").getAsInt();

                    String[] splitReg = registryName.split(":");
                    Item item = GameRegistry.findItem(splitReg[0], splitReg[1]);
                    ItemStack itemStack = new ItemStack(item, stackSize, meta);

                    textList.add(itemStack.getDisplayName() + ":" + meta + " x " + stackSize);
                }

                // Render text in the middle of the block defined by relative coordinates x, y, z
                renderFloatingText(
                    textList,
                    minX + instruction.get("x").getAsDouble() + 0.5,
                    minY + instruction.get("y").getAsDouble() + 0.5,
                    minZ + instruction.get("z").getAsDouble() + 0.5
                );
            }
        }
    }


    private static void renderTileEntityTagPoints(RenderWorldLastEvent event) {
        if (firstPosition[0] == Integer.MAX_VALUE || secondPosition[0] == Integer.MAX_VALUE) return;
        if (currentTest == null) return;

        if (!currentTest.has(INSTRUCTIONS)) {
            return;
        }

        // Calculate the minimum and maximum coordinates for the bounding box
        double minX = Math.min(firstPosition[0], secondPosition[0]);
        double minY = Math.min(firstPosition[1], secondPosition[1]);
        double minZ = Math.min(firstPosition[2], secondPosition[2]);

        JsonArray instructionsArray = currentTest.getAsJsonArray(INSTRUCTIONS);

        for (int i = 0; i < instructionsArray.size(); i++) {
            JsonObject instruction = instructionsArray.get(i).getAsJsonObject();

            if (instruction.get("type").getAsString().equals("checkTile")) {
                // Render text in the middle of the block defined by relative coordinates x, y, z

                List<String> textList = new ArrayList<>();

                textList.add(instruction.get("funcRegistry").getAsString());

                if (instruction.has("optionalLabel")) {
                    textList.add(instruction.get("optionalLabel").getAsString());
                }

                renderFloatingText(
                    textList,
                    minX + instruction.get("x").getAsDouble() + 0.5,
                    minY + instruction.get("y").getAsDouble() + 0.5,
                    minZ + instruction.get("z").getAsDouble() + 0.5
                );
            }
        }
    }


    private static void renderRegionLabel(RenderWorldLastEvent event) {
        // Not yet set.
        if (firstPosition[0] == Integer.MAX_VALUE || secondPosition[0] == Integer.MAX_VALUE) return;

        // Calculate the center position for the text.
        double x = (firstPosition[0] + secondPosition[0]) / 2.0 + 0.5; // Center in x
        double y = Math.max(firstPosition[1], secondPosition[1]) + 1.5; // Slightly above
        double z = (firstPosition[2] + secondPosition[2]) / 2.0 + 0.5; // Center in z

        // User has not initialised test yet.
        if (currentTest == null) return;

        // Sanity check that the field exists...
        String testName = "ERROR STRING, REPORT TO AUTHOR.";
        if (currentTest.has(TEST_NAME)) {
            testName = currentTest.get(TEST_NAME).getAsString();
        }

        int totalTicks = 0;
        if (currentTest.has(INSTRUCTIONS)) {
            JsonArray instructionsArray = currentTest.getAsJsonArray(INSTRUCTIONS);
            if (instructionsArray != null) {
                for (JsonElement jsonObject : instructionsArray) {
                    JsonObject instruction = jsonObject.getAsJsonObject();
                    if (instruction.has("duration")) {
                        totalTicks += instruction.get("duration").getAsInt();
                    }
                }
            } else {
                // Handle the case where the JsonArray is null
                System.err.println("Instructions array is null");
            }
        } else {
            // Handle the case where the key doesn't exist
            System.err.println("Key 'INSTRUCTIONS' does not exist in the JsonObject");
        }


        List<String> textList = new ArrayList<>();
        textList.add(testName);
        textList.add("Total ticks: " + totalTicks);

        // Render the floating text.
        renderFloatingText(
            textList,
            x, y, z
        );
    }

    /**
     * Renders multiple lines of floating text at specified coordinates in the world.
     * @param lines The lines of text to render
     * @param x The x coordinate in the world
     * @param y The y coordinate in the world
     * @param z The z coordinate in the world
     */
    public static void renderFloatingText(List<String> lines, double x, double y, double z) {
        FontRenderer fontrenderer = Minecraft.getMinecraft().fontRenderer;
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
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Calculate total height and adjust starting Y position to center the text
        int totalHeight = (10 + 2) * lines.size();  // 10 is font height, 2 is padding
        int startY = -totalHeight / 2;  // Center vertically

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
            int yPos = startY + i * (10 + 2);  // 10 is font height, 2 is padding
            fontrenderer.drawString(line, -textWidth / 2, yPos, 0xFFFFFF);
        }

        // Restore GL state
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    /**
     * Helper method to draw a rectangle.
     */
    private static void drawRect(int right, int bottom) {
        float alpha = (float)(0x40000000 >> 24 & 255) / 255.0F;
        float red = (float)(0x40000000 >> 16 & 255) / 255.0F;
        float green = (float)(0x40000000 >> 8 & 255) / 255.0F;
        float blue = (float)(0x40000000 & 255) / 255.0F;

        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, 0);
        GL11.glVertex2f(0, 0);
        GL11.glEnd();
    }

}
