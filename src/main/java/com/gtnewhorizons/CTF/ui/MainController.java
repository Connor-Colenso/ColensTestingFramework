package com.gtnewhorizons.CTF.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.procedures.Procedure;
import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import net.minecraft.client.Minecraft;


import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;

public class MainController {

    @FXML
    private ListView<Procedure> procedureViewBox;

    @FXML
    private ListView<String> rawProcedureViewBox;

    // ObservableList to hold the 'type' values
    private static ObservableList<Procedure> listItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set the ListView's items to be the observable list
        procedureViewBox.setItems(listItems);
        updateListFromJson();

        // Set up an event handler for selection
        procedureViewBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ObservableList<String> t = FXCollections.observableArrayList();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                String prettyJson = gson.toJson(newValue.getJson());

                t.add(prettyJson);
                rawProcedureViewBox.setItems(t);
            }
        });

        // Add key event handler to the ListView
        procedureViewBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
                // Get the selected item
                int selectedIndex = procedureViewBox.getSelectionModel().getSelectedIndex();

                // Remove the selected item if any item is selected
                if (selectedIndex != -1) {
                    procedureViewBox.getItems().remove(selectedIndex);
                }
            }
        });
    }

    // Method to update the ListView based on the currentTest JsonObject
    public static void updateListFromJson() {

        String UUID = Minecraft.getMinecraft().thePlayer.getUniqueID().toString();
        JsonObject currentTest = CurrentTestUnderConstruction.getTestJson(UUID);

        if (currentTest != null && currentTest.has(INSTRUCTIONS)) {
            JsonArray instructionsArray = currentTest.getAsJsonArray(INSTRUCTIONS);

            // Clear the current list items
            listItems.clear();

            // Loop through each JsonObject in the JsonArray
            for (int i = 0; i < instructionsArray.size(); i++) {
                JsonObject instruction = instructionsArray.get(i).getAsJsonObject();

                // Extract the "type" value from each JsonObject and add it to the list
                listItems.add(Procedure.buildProcedureFromJson(instruction));
            }
        }

    }

    // Call this method when the data is updated, to refresh the ListView
    public static void refreshInstructionList() {
        updateListFromJson();

    }
}
