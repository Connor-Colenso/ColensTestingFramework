package com.gtnewhorizons.CTF.ui;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.GAMERULES;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_CONFIG;
import static com.gtnewhorizons.CTF.utils.JsonUtils.deepCopyJson;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;

import javafx.util.Callback;
import net.minecraft.client.Minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.procedures.Procedure;
import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;

public class MainController {

    private static final HashMap<String, String> TEST_CONFIG_TYPE = new HashMap<>();
    static {
        TEST_CONFIG_TYPE.put("dimension", "textbox");           // Integer, treated as textbox input
        TEST_CONFIG_TYPE.put("bufferZoneInBlocks", "textbox");  // Integer, treated as textbox input
        TEST_CONFIG_TYPE.put("preserveVertical", "bool");       // Boolean, string "false"
        TEST_CONFIG_TYPE.put("forceSeparateRunning", "bool");   // Boolean, string "false"
        TEST_CONFIG_TYPE.put("duplicateTest", "textbox");       // Integer, treated as textbox input
    }

    @FXML
    private StackPane TestConfigStackPane;
    @FXML
    private Label TestNameLabel;
    @FXML
    private ListView<String> TestConfigListView;
    @FXML
    private ListView<String> GamerulesListView;
    @FXML
    private ListView<String> CommandsListView;
    @FXML
    private ListView<Procedure> procedureViewBox;
    @FXML
    private ListView<String> rawProcedureViewBox;

    @FXML
    private Button GamerulesButton;
    @FXML
    private Button TestSettingsButton;
    @FXML
    private Button CommandsButton;

    // ObservableLists to hold data
    private static ObservableList<Procedure> listItems = FXCollections.observableArrayList();
    private static ObservableList<String> gamerules = FXCollections.observableArrayList();
    private static ObservableList<String> testConfig = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize procedure view with item list
        procedureViewBox.setItems(listItems);
        setupProcedureViewBoxEvents();

        // Setup Gamerules ListView with checkboxes
        setupGamerulesListView();

        // Setup test config ListView with various options.
        setupTestConfigListView();
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

        GamerulesButton.setOnAction(event -> {
            GamerulesListView.setVisible(true);
            CommandsListView.setVisible(false);
            TestConfigListView.setVisible(false);
        });

        TestSettingsButton.setOnAction(event -> {
            GamerulesListView.setVisible(false);
            CommandsListView.setVisible(false);
            TestConfigListView.setVisible(true);
        });

        CommandsButton.setOnAction(event -> {
            GamerulesListView.setVisible(false);
            CommandsListView.setVisible(true);
            TestConfigListView.setVisible(false);
        });


    }


    private void setupTestConfigListView() {
        TestConfigListView.setItems(testConfig);

        TestConfigListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<>() {
                    private final CheckBox checkBox = new CheckBox();
                    private final TextField textField = new TextField();

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            // Get the type of the current test config item from the TEST_CONFIG_TYPE map
                            String configType = TEST_CONFIG_TYPE.get(item);
                            JsonObject testConfig = currentTestInUI.get(TEST_CONFIG).getAsJsonObject();

                            // Set the content based on the config type
                            if ("bool".equals(configType)) {
                                checkBox.setText(item);
                                checkBox.setSelected(testConfig.get(item).getAsBoolean());
                                setGraphic(checkBox);
                                checkBox.setOnAction(event -> handleTestConfigSelection(checkBox, item));
                            } else if ("textbox".equals(configType)) {
                                textField.setText(testConfig.get(item).getAsString());
                                textField.setPromptText("Enter a number");
                                setGraphic(textField);
                                textField.setOnAction(event -> handleTestConfigTextInput(textField, item));
                                textField.setMaxWidth(40);

                                // Ensure only numbers are inputted in the text field
                                textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                    if (!newValue.matches("\\d*")) {
                                        textField.setText(oldValue); // Reject non-numeric input
                                    }
                                });
                            }
                        }
                    }
                };
            }
        });
    }

    private void handleTestConfigTextInput(TextField textField, String item) {
        JsonObject testConfig = currentTestInUI.get(TEST_CONFIG).getAsJsonObject();
        JsonObject gameRules = testConfig.get(GAMERULES).getAsJsonObject();

        gameRules.addProperty(item, textField.getText());
        ClientSideExecutor.add(() -> CurrentTestUnderConstruction.updateFromUI(deepCopyJson(currentTestInUI)));
    }

    private void handleTestConfigSelection(CheckBox checkBox, String item) {
        JsonObject testConfig = currentTestInUI.get(TEST_CONFIG).getAsJsonObject();
        JsonObject gameRules = testConfig.get(GAMERULES).getAsJsonObject();

        gameRules.addProperty(checkBox.getText(), checkBox.isSelected());
        ClientSideExecutor.add(() -> CurrentTestUnderConstruction.updateFromUI(deepCopyJson(currentTestInUI)));
    }

    private void setupGamerulesListView() {
        GamerulesListView.setCellFactory(param -> new ListCell<>() {

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

        GamerulesListView.setItems(gamerules);
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

                testConfig.clear();
                testConfig.addAll(TEST_CONFIG_TYPE.keySet());

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
