package com.gtnewhorizons.CTF.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.fml.common.Loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Json {

    public static List<JsonObject> loadAll() {
        final JsonParser jsonParser = new JsonParser();

        List<JsonObject> jsonList = new ArrayList<>();
        String directoryPath = "CTF/";

        // Get the path to the directory where JSON files are located
        File directory = new File(
            Loader.instance()
                .getConfigDir(),
            directoryPath);
        File[] jsonFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));

        if (jsonFiles == null) throw new RuntimeException("No json files found for CTF.");

        for (File json : jsonFiles) {
            // Load each JSON file
            try (InputStream inputStream = Files.newInputStream(json.toPath());
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                JsonObject jsonObject = jsonParser.parse(reader)
                    .getAsJsonObject();
                jsonList.add(jsonObject); // Add the parsed JSON object to the list
            } catch (Exception e) {
                System.err.println("Error reading JSON file: " + json.getName());
                e.printStackTrace();
            }
        }

        return jsonList; // Return the list of JSON objects
    }

}
