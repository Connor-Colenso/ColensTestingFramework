package com.gtnewhorizons.CTF.ui;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.GAMERULES;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_CONFIG;
import static com.gtnewhorizons.CTF.utils.JsonUtils.deepCopyJson;

import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;

import net.minecraft.client.Minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.procedures.Procedure;
import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;

public class MainController {

    @FXML
    private StackPane TestConfigStackPane;
    @FXML
    private Label TestNameLabel;
    @FXML
    private ListView<String> TestConfigListView;
    @FXML
    private ListView<String> Gamerules;
    @FXML
    private ListView<String> TestSettings;
    @FXML
    private ListView<String> Commands;
    @FXML
    private ListView<Procedure> procedureViewBox;
    @FXML
    private ListView<String> rawProcedureViewBox;

    // ObservableLists to hold data
    private static ObservableList<Procedure> listItems = FXCollections.observableArrayList();
    private static ObservableList<String> gamerules = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize procedure view with item list
        procedureViewBox.setItems(listItems);
        setupProcedureViewBoxEvents();

        // Setup Gamerules ListView with checkboxes
        setupGamerulesListView();

    }

    private void setupProcedureViewBoxEvents() {
        // Selection event for procedureViewBox
        procedureViewBox.getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    updateRawProcedureView(newValue);
                }
            });

        // Key press event for DELETE or BACK_SPACE
        procedureViewBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
                removeSelectedProcedure();
            }
        });
    }

    private void setupGamerulesListView() {
        Gamerules.setCellFactory(param -> new ListCell<>() {

            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox.setText(item);

                    JsonObject testConfig = currentTestInUI.get(TEST_CONFIG).getAsJsonObject();
                    JsonObject gameRules = testConfig.get(GAMERULES).getAsJsonObject();
                    checkBox.setSelected(gameRules.get(item).getAsBoolean());

                    setGraphic(checkBox);
                    checkBox.setOnAction(event -> handleGameruleSelection(checkBox));
                }
            }
        });

        Gamerules.setItems(gamerules);
    }

    private void updateRawProcedureView(Procedure procedure) {
        ObservableList<String> procedureJsonList = FXCollections.observableArrayList();
        Gson gson = new GsonBuilder().setPrettyPrinting()
            .create();
        String prettyJson = gson.toJson(procedure.getJson());
        procedureJsonList.add(prettyJson);
        rawProcedureViewBox.setItems(procedureJsonList);
    }

    private void handleGameruleSelection(CheckBox checkBox) {
        System.out.println("Gamerule selected: " + checkBox.getText() + " was set to " + checkBox.isSelected());
        JsonObject testConfig = currentTestInUI.get(TEST_CONFIG).getAsJsonObject();
        JsonObject gameRules = testConfig.get(GAMERULES).getAsJsonObject();

        gameRules.addProperty(checkBox.getText(), checkBox.isSelected());
        ClientSideExecutor.add(() -> CurrentTestUnderConstruction.updateFromUI(deepCopyJson(currentTestInUI)));
    }

    private void removeSelectedProcedure() {
        int selectedIndex = procedureViewBox.getSelectionModel()
            .getSelectedIndex();
        if (selectedIndex != -1) {
            procedureViewBox.getItems()
                .remove(selectedIndex);
            ClientSideExecutor.add(() -> CurrentTestUnderConstruction.removeInstruction(selectedIndex));
        }
    }

    // Update ListView based on the currentTest JsonObject
    public static void updateListFromJson(JsonObject currentTest) {
        if (currentTest != null) {
            currentTestInUI = currentTest;

            // Process instructions
            if (currentTest.has(INSTRUCTIONS)) {
                JsonArray instructionsArray = currentTest.getAsJsonArray(INSTRUCTIONS);
                listItems.clear();

                for (int i = 0; i < instructionsArray.size(); i++) {
                    JsonObject instruction = instructionsArray.get(i)
                        .getAsJsonObject();
                    listItems.add(Procedure.buildProcedureFromJson(instruction));
                }
            }

            // Process gamerules
            if (currentTest.has(TEST_CONFIG)) {
                JsonObject currentTestConfig = currentTest.getAsJsonObject(TEST_CONFIG);
                JsonObject gamerulesJson = currentTestConfig.getAsJsonObject(GAMERULES);
                gamerules.clear();

                // Iterate over all gamerules and add them to the list
                for (Map.Entry<String, JsonElement> entry : gamerulesJson.entrySet()) {
                    gamerules.add(
                        entry.getKey());
                }

            }
        }
    }

    private static JsonObject currentTestInUI;

    // Refresh instruction list from the current test JSON
    public static void refreshInstructionList() {
        String UUID = Minecraft.getMinecraft().thePlayer.getUniqueID()
            .toString();
        JsonObject currentTest = deepCopyJson(CurrentTestUnderConstruction.getTestJson(UUID));

        Platform.runLater(() -> updateListFromJson(currentTest));
    }
}
