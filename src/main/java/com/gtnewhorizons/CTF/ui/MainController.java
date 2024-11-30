package com.gtnewhorizons.CTF.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MainController {

    public static final String INSTRUCTIONS = "instructions";

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

        // Get the path to the directory where JSON files are located

        String t = System.getProperty("user.dir");

        File ctfIntegrationTestsDirectory = new File("/Users/connorcolenso/Desktop/GTNH-Projects/ColensTestingFramework/src/main/ctf_tests");

        System.out.println("Loading JSONs from: " + ctfIntegrationTestsDirectory.getAbsolutePath());

        jsonList.addAll(loadJsonsFromDir(ctfIntegrationTestsDirectory));

        System.out.println("CTF Total jsons obtained " + jsonList.size());
        return jsonList; // Return the list of JSON objects
    }


    @FXML
    private ListView<JsonObject> toplistbox;

    @FXML
    private ListView<String> bottomlistbox;

    private static JsonObject currentTest = loadAll().get(0);

    // ObservableList to hold the 'type' values
    private static ObservableList<JsonObject> listItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set the ListView's items to be the observable list
        toplistbox.setItems(listItems);
        updateListFromJson();

        // Set up an event handler for selection
        toplistbox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ObservableList<String> t = FXCollections.observableArrayList();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                String prettyJson = gson.toJson(oldValue);

                t.add(prettyJson);
                bottomlistbox.setItems(t);
            }
        });
    }

    // Method to update the ListView based on the currentTest JsonObject
    public static void updateListFromJson() {
        if (currentTest != null && currentTest.has(INSTRUCTIONS)) {
            JsonArray instructionsArray = currentTest.getAsJsonArray(INSTRUCTIONS);

            // Clear the current list items
            listItems.clear();

            // Loop through each JsonObject in the JsonArray
            for (int i = 0; i < instructionsArray.size(); i++) {
                JsonObject instruction = instructionsArray.get(i).getAsJsonObject();

                // Extract the "type" value from each JsonObject and add it to the list
                listItems.add(instruction);
            }
        }

    }

    // Call this method when the data is updated, to refresh the ListView
    public static void refreshList() {
        updateListFromJson();


    }
}
