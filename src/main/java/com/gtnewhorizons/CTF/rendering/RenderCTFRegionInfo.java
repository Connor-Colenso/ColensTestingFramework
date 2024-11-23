package com.gtnewhorizons.CTF.rendering;

import static com.gtnewhorizons.CTF.commands.CommandInitTest.currentTest;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.FLUID_AMOUNT;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.FLUID_NAME;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.STORED_FLUIDS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_NAME;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isRegionNotDefined;
import static com.gtnewhorizons.CTF.utils.RegionUtils.isTestNotStarted;
import static com.gtnewhorizons.CTF.utils.rendering.RegionRendering.renderFloatingText;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class RenderCTFRegionInfo {

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (isRegionNotDefined()) return;
        if (isTestNotStarted()) return;

        double minX = Math.min(firstPosition[0], secondPosition[0]);
        double minY = Math.min(firstPosition[1], secondPosition[1]);
        double minZ = Math.min(firstPosition[2], secondPosition[2]);

        JsonArray instructionsArray = currentTest.getAsJsonArray(INSTRUCTIONS);

        renderRegionLabel(instructionsArray);
        renderTileEntityTagPoints(instructionsArray, minX, minY, minZ);
        renderAddItemPoints(instructionsArray, minX, minY, minZ);
    }

    private void renderAddItemPoints(JsonArray instructionsArray, double minX, double minY, double minZ) {

        for (int i = 0; i < instructionsArray.size(); i++) {
            JsonObject instruction = instructionsArray.get(i)
                .getAsJsonObject();

            // Check if this instruction is of type "addItems"
            if (instruction.get("type")
                .getAsString()
                .equals("addItems")) {
                // Initialize a list to store text lines
                List<String> textList = new ArrayList<>();
                textList.add("Add Item(s)");

                // Add the optional label if it exists
                if (instruction.has("optionalLabel")) {
                    textList.add(
                        instruction.get("optionalLabel")
                            .getAsString());
                }

                // Process each item in the "items" array
                JsonArray itemsArray = instruction.getAsJsonArray("items");
                for (int j = 0; j < itemsArray.size(); j++) {
                    JsonObject itemObject = itemsArray.get(j)
                        .getAsJsonObject();

                    // Extract item details, kinda hacky, but I don't see a good way around making an itemstack each
                    // time...
                    String registryName = itemObject.get("registryName")
                        .getAsString();
                    int stackSize = itemObject.get("stackSize")
                        .getAsInt();
                    int meta = itemObject.get("metadata")
                        .getAsInt();

                    String[] splitReg = registryName.split(":");
                    Item item = GameRegistry.findItem(splitReg[0], splitReg[1]);
                    ItemStack itemStack = new ItemStack(item, stackSize, meta);

                    textList.add(itemStack.getDisplayName() + ":" + meta + " x " + stackSize);
                }

                // Render text in the middle of the block defined by relative coordinates x, y, z
                renderFloatingText(
                    textList,
                    minX + instruction.get("x")
                        .getAsDouble() + 0.5,
                    minY + instruction.get("y")
                        .getAsDouble() + 0.5,
                    minZ + instruction.get("z")
                        .getAsDouble() + 0.5);
            } else if (instruction.get("type")
                .getAsString()
                .equals("addFluids")) {
                    // Initialize a list to store text lines
                    List<String> textList = new ArrayList<>();
                    textList.add("Add Fluid(s)");

                    // Add the optional label if it exists
                    if (instruction.has("optionalLabel")) {
                        textList.add(
                            instruction.get("optionalLabel")
                                .getAsString());
                    }

                    // Process each fluid in the "fluids" array
                    JsonArray fluidsArray = instruction.getAsJsonArray(STORED_FLUIDS);
                    for (int j = 0; j < fluidsArray.size(); j++) {
                        JsonObject fluidObject = fluidsArray.get(j)
                            .getAsJsonObject();

                        // Extract fluid details
                        String fluidName = fluidObject.get(FLUID_NAME)
                            .getAsString();
                        int amount = fluidObject.get(FLUID_AMOUNT)
                            .getAsInt();

                        // Append fluid information to text list
                        textList.add(fluidName + " x " + amount + "mB");
                    }

                    // Render text in the middle of the block defined by relative coordinates x, y, z
                    renderFloatingText(
                        textList,
                        minX + instruction.get("x")
                            .getAsDouble() + 0.5,
                        minY + instruction.get("y")
                            .getAsDouble() + 0.5,
                        minZ + instruction.get("z")
                            .getAsDouble() + 0.5);
                }

        }
    }

    private static void renderTileEntityTagPoints(JsonArray instructionsArray, double minX, double minY, double minZ) {

        for (int i = 0; i < instructionsArray.size(); i++) {
            JsonObject instruction = instructionsArray.get(i)
                .getAsJsonObject();

            if (instruction.get("type")
                .getAsString()
                .equals("checkTile")) {
                // Render text in the middle of the block defined by relative coordinates x, y, z

                List<String> textList = new ArrayList<>();

                textList.add(
                    instruction.get("funcRegistry")
                        .getAsString());

                if (instruction.has("optionalLabel")) {
                    textList.add(
                        instruction.get("optionalLabel")
                            .getAsString());
                }

                renderFloatingText(
                    textList,
                    minX + instruction.get("x")
                        .getAsDouble() + 0.5,
                    minY + instruction.get("y")
                        .getAsDouble() + 0.5,
                    minZ + instruction.get("z")
                        .getAsDouble() + 0.5);
            }
        }
    }

    private static void renderRegionLabel(JsonArray instructionsArray) {
        // Calculate the center position for the text.
        double x = (firstPosition[0] + secondPosition[0]) / 2.0 + 0.5; // Center in x
        double y = Math.max(firstPosition[1], secondPosition[1]) + 1.5; // Slightly above
        double z = (firstPosition[2] + secondPosition[2]) / 2.0 + 0.5; // Center in z

        // Sanity check that the field exists...
        String testName = "ERROR STRING, REPORT TO AUTHOR.";
        if (currentTest.has(TEST_NAME)) {
            testName = currentTest.get(TEST_NAME)
                .getAsString();
        }

        int totalTicks = 0;

        for (JsonElement jsonObject : instructionsArray) {
            JsonObject instruction = jsonObject.getAsJsonObject();
            if (instruction.has("duration")) {
                totalTicks += instruction.get("duration")
                    .getAsInt();
            }
        }

        List<String> textList = new ArrayList<>();
        textList.add(testName);
        textList.add("Total ticks: " + totalTicks);

        // Render the floating text.
        renderFloatingText(textList, x, y, z);
    }

}
