package com.gtnewhorizons.CTF.utils;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_NAME;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.gtnewhorizons.CTF.MyMod;
import net.minecraft.client.Minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cpw.mods.fml.common.Loader;

public class JsonUtils {

    public static void saveJsonToFile(JsonObject overallJson) {
        // Create a pretty-printing Gson instance
        Gson gson = new GsonBuilder().setPrettyPrinting()
            .create();

        try {
            // Define the file path in the CTF folder inside the config directory
            File ctfConfigDir = new File(Minecraft.getMinecraft().mcDataDir, "config/CTF/testing");
            if (!ctfConfigDir.exists()) {
                if (ctfConfigDir.mkdirs()) {
                    MyMod.LOG.info("Successfully saved CTF test.");
                } else {
                    MyMod.LOG.info("Failed to save CTF test, please report to author.");
                }
            }

            // Specify the output file path within the CTF folder
            File outputFile = new File(
                ctfConfigDir,
                overallJson.get(TEST_NAME)
                    .getAsString() + ".json");

            // Write the JSON content to the file with pretty printing (provided by Gson).
            try (FileWriter fileWriter = new FileWriter(outputFile)) {
                fileWriter.write(gson.toJson(overallJson));
                MyMod.LOG.info("Json saved successfully to {}", outputFile.getAbsolutePath());
            }

        } catch (IOException e) {
            MyMod.LOG.info("Error writing Json to file: {}", e.getMessage());
        }
    }

    private static final JsonParser jsonParser = new JsonParser();

    private static List<JsonObject> loadJsonsFromDir(File directory) {
        List<JsonObject> jsonList = new ArrayList<>();

        if (!directory.isDirectory()) return jsonList;

        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            paths.filter(Files::isRegularFile)
                .filter(
                    path -> path.toString()
                        .endsWith(".json"))
                .forEach(path -> {
                    try (BufferedReader reader = Files.newBufferedReader(path)) {
                        JsonObject jsonObject = jsonParser.parse(reader)
                            .getAsJsonObject();
                        jsonList.add(jsonObject); // Add the parsed JSON object to the list
                    } catch (Exception e) {
                        System.err.println("Error reading JSON file: " + path.getFileName());
                        e.printStackTrace();
                    }
                });
        } catch (IOException e) {
            throw new RuntimeException("Error accessing JSON files in directory: " + directory, e);
        }

        return jsonList; // Return the list of JSON objects
    }

    public static List<JsonObject> loadAll() {
        List<JsonObject> jsonList = new ArrayList<>();

        String directoryPath = "CTF/";

        // Get the path to the directory where JSON files are located
        File ctfConfigDirectory = new File(
            Loader.instance()
                .getConfigDir(),
            directoryPath);

        File ctfIntegrationTestsDirectory = new File("../src/main/ctf_tests");

        jsonList.addAll(loadJsonsFromDir(ctfIntegrationTestsDirectory));
        jsonList.addAll(loadJsonsFromDir(ctfConfigDirectory));

        return jsonList; // Return the list of JSON objects
    }

}
