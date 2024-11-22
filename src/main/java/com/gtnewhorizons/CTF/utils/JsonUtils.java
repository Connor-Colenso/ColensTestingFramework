package com.gtnewhorizons.CTF.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_NAME;

public class JsonUtils {

    public static void saveJsonToFile(JsonObject overallJson) {
        // Create a pretty-printing Gson instance
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            // Define the file path in the CTF folder inside the config directory
            File ctfConfigDir = new File(Minecraft.getMinecraft().mcDataDir, "config/CTF/testing");
            if (!ctfConfigDir.exists()) {
                if (ctfConfigDir.mkdirs()) {
                    System.out.println("Successfully saved CTF test.");
                } else {
                    System.out.println("Failed to save CTF test, please report to author.");
                }
            }

            // Specify the output file path within the CTF folder
            File outputFile = new File(ctfConfigDir, overallJson.get(TEST_NAME).getAsString() + ".json");

            // Write the JSON content to the file with pretty printing (provided by Gson).
            try (FileWriter fileWriter = new FileWriter(outputFile)) {
                fileWriter.write(gson.toJson(overallJson));
                System.out.println("Json saved successfully to " + outputFile.getAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("Error writing Json to file: " + e.getMessage());
        }
    }

}
