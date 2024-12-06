package com.gtnewhorizons.CTF.utils;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.BUILD;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.STRUCTURE;
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

import com.google.gson.JsonArray;
import net.minecraft.client.Minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gtnewhorizons.CTF.MyMod;

import cpw.mods.fml.common.Loader;

public class JsonUtils {

    // Method to get all sizes: X, Y, Z
    public static int[] getStructureDimensions(JsonObject test) {

        JsonArray build = test.getAsJsonObject(STRUCTURE).getAsJsonArray(BUILD);

        int xLength = getXStructureLength(build);
        int yLength = getYStructureLength(build);
        int zLength = getZStructureLength(build);

        if (xLength == 0 || yLength == 0 || zLength == 0) {
            throw new RuntimeException("Structure for test had no volume.");
        }

        return new int[] {xLength, yLength, zLength};
    }

    // Method to get the Y-size (height)
    public static int getYStructureLength(JsonArray build) {
        return build.size();  // Number of layers in the structure
    }

    // Method to get the Z-size (rows per layer)
    public static int getZStructureLength(JsonArray build) {
        if (build.size() == 0) return 0;  // Empty structure case
        JsonArray firstLayer = build.get(0).getAsJsonArray();
        return firstLayer.size();  // Number of rows in the first layer
    }

    // Method to get the X-size (length of the first row)
    public static int getXStructureLength(JsonArray build) {
        if (build.size() == 0) return 0;  // Empty structure case
        JsonArray firstLayer = build.get(0).getAsJsonArray();
        if (firstLayer.size() == 0) return 0;  // Empty first row case
        String firstRowData = firstLayer.get(0).getAsString();
        return firstRowData.length();  // Length of the first row
    }

    public static void saveJsonToFile(JsonObject overallJson) {
        // Create a pretty-printing Gson instance
        Gson gson = new GsonBuilder().setPrettyPrinting()
            .create();

        try {
            // Define the file path in the CTF folder inside the config directory
            File ctfConfigDir = new File(Minecraft.getMinecraft().mcDataDir, "config/CTF/testing");
            if (!ctfConfigDir.exists()) {
                if (ctfConfigDir.mkdirs()) {
                    MyMod.CTF_LOG.info("Successfully saved CTF test.");
                } else {
                    MyMod.CTF_LOG.info("Failed to save CTF test, please report to author.");
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
                MyMod.CTF_LOG.info("Json saved successfully to {}", outputFile.getAbsolutePath());
            }

        } catch (IOException e) {
            MyMod.CTF_LOG.info("Error writing Json to file: {}", e.getMessage());
        }
    }

    public static final JsonParser jsonParser = new JsonParser();

    // Method to load all JSONs from a directory
    private static List<JsonObject> loadJsonsFromDir(File directory) {
        List<JsonObject> jsonList = new ArrayList<>();
        if (!directory.isDirectory()) return jsonList;

        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            paths.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    JsonObject jsonObject = loadJsonFromFile(path);
                    if (jsonObject != null) {
                        jsonList.add(jsonObject);
                    }
                });
        } catch (IOException e) {
            throw new RuntimeException("Error accessing JSON files in directory: " + directory, e);
        }

        return jsonList;
    }

    // Method to load an individual JSON file and return the JsonObject
    public static JsonObject loadJsonFromFile(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return jsonParser.parse(reader).getAsJsonObject();
        } catch (Exception e) {
            System.err.println("Error reading JSON file: " + path.getFileName());
            e.printStackTrace();
            return null; // Return null in case of error
        }
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

        System.out.println("Loading JSONs from: " + ctfIntegrationTestsDirectory.getAbsolutePath());

        jsonList.addAll(loadJsonsFromDir(ctfIntegrationTestsDirectory));
        jsonList.addAll(loadJsonsFromDir(ctfConfigDirectory));

        System.out.println("CTF Total jsons obtained " + jsonList.size());
        return jsonList; // Return the list of JSON objects
    }

    public static JsonObject deepCopyJson(JsonObject original) {
        // Use Gson to convert the JsonObject to a JsonElement and then back to a JsonObject
        Gson gson = new Gson();
        String jsonString = gson.toJson(original); // Serialize to JSON string
        return jsonParser.parse(jsonString)
            .getAsJsonObject(); // Parse back to JsonObject
    }

}
