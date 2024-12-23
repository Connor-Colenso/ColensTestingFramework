package com.gtnewhorizons.CTF.ui.javafx;

import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.firstPosition;
import static com.gtnewhorizons.CTF.events.CTFWandEventHandler.secondPosition;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.BUFFER_ZONE_IN_BLOCKS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.DIMENSION;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.DUPLICATE_TEST;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.FORCE_SEPARATE_RUNNING;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.GAMERULES;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.INSTRUCTIONS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.PRESERVE_VERTICAL;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_CONFIG;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_NAME;
import static com.gtnewhorizons.CTF.utils.JsonUtils.deepCopyJson;
import static com.gtnewhorizons.CTF.utils.JsonUtils.loadJsonFromFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.gtnewhorizons.CTF.networking.TransmitJsonForBuildAndRun;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.networking.CTFNetworkHandler;
import com.gtnewhorizons.CTF.networking.TransmitJsonForBuild;
import com.gtnewhorizons.CTF.procedures.Procedure;
import com.gtnewhorizons.CTF.tests.CurrentTestUnderConstruction;
import com.gtnewhorizons.CTF.utils.JsonUtils;

import cpw.mods.fml.common.Loader;

public class MainController {

    private static final HashMap<String, String> TEST_CONFIG_TYPE = new HashMap<>();
    static {
        TEST_CONFIG_TYPE.put(DIMENSION, "textbox"); // Integer, treated as textbox input
        TEST_CONFIG_TYPE.put(BUFFER_ZONE_IN_BLOCKS, "textbox"); // Integer, treated as textbox input
        TEST_CONFIG_TYPE.put(DUPLICATE_TEST, "textbox"); // Integer, treated as textbox input
        TEST_CONFIG_TYPE.put(PRESERVE_VERTICAL, "bool"); // Boolean, string "false"
        TEST_CONFIG_TYPE.put(FORCE_SEPARATE_RUNNING, "bool"); // Boolean, string "false"
    }

    private static final int MAX_RECENT_FILES = 5; // Limit recent files
    private final ObservableList<File> recentFiles = FXCollections.observableArrayList();

    @FXML
    private Menu OpenRecentMenu;
    @FXML
    private MenuItem OpenMenuItem;
    @FXML
    private MenuItem PrintTestAndRunMenuItem;
    @FXML
    private MenuItem PrintTestMenuItem;

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
    private static final ObservableList<Procedure> listItems = FXCollections.observableArrayList();
    private static final ObservableList<String> gamerules = FXCollections.observableArrayList();
    private static final ObservableList<String> testConfig = FXCollections.observableArrayList();

    private static MainController instance;

    private void addRecentFile(File file) {
        recentFiles.remove(file); // Remove if already in the list to update position
        recentFiles.add(0, file); // Add to the top
        if (recentFiles.size() > MAX_RECENT_FILES) {
            recentFiles.remove(recentFiles.size() - 1); // Keep the list size limited
        }
        populateRecentFilesMenu(); // Refresh the menu
    }

    private void populateRecentFilesMenu() {
        OpenRecentMenu.getItems()
            .clear(); // Clear existing items
        for (File file : recentFiles) {
            MenuItem recentItem = new MenuItem(file.getName());
            recentItem.setOnAction(e -> loadRecentFile(file));
            OpenRecentMenu.getItems()
                .add(recentItem);
        }
    }

    private void loadRecentFile(File file) {
        try {
            updateFromJson(loadJsonFromFile(file.toPath()));
        } catch (Exception exception) {
            showAlert("Error loading JSON", "Failed to load the selected recent file.");
            exception.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        instance = this;

        // Initialize procedure view with item list
        procedureViewBox.setItems(listItems);
        setupProcedureViewBoxEvents();

        // Setup Gamerules ListView with checkboxes
        setupGamerulesListView();

        // Setup test config ListView with various options.
        setupTestConfigListView();

        // Setup menu items
        setupMenuItems();

        PrintTestAndRunMenuItem.setOnAction(e -> ClientSideExecutor.add(() -> {
            if (currentTestInUI == null) return;
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;

            int[] dimensions = JsonUtils.getStructureDimensions(currentTestInUI);

            firstPosition[0] = (int) (player.posX + 2);
            firstPosition[1] = (int) (player.posY);
            firstPosition[2] = (int) (player.posZ + 2);

            // Bit of a hack, since the player could move inbetween, but it's close enough for now.
            secondPosition[0] = firstPosition[0] + dimensions[0] - 1;
            secondPosition[1] = firstPosition[1] + dimensions[1] - 1;
            secondPosition[2] = firstPosition[2] + dimensions[2] - 1;

            TransmitJsonForBuildAndRun packet = new TransmitJsonForBuildAndRun(currentTestInUI, firstPosition[0], firstPosition[1], firstPosition[2]);

            // Send the packet to the server
            CTFNetworkHandler.INSTANCE.sendToServer(packet);
        }));

        PrintTestMenuItem.setOnAction(e -> ClientSideExecutor.add(() -> {
            if (currentTestInUI == null) return;
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;

            int[] dimensions = JsonUtils.getStructureDimensions(currentTestInUI);

            firstPosition[0] = (int) (player.posX + 2);
            firstPosition[1] = (int) (player.posY);
            firstPosition[2] = (int) (player.posZ + 2);

            // Bit of a hack, since the player could move inbetween, but it's close enough for now.
            secondPosition[0] = firstPosition[0] + dimensions[0] - 1;
            secondPosition[1] = firstPosition[1] + dimensions[1] - 1;
            secondPosition[2] = firstPosition[2] + dimensions[2] - 1;

            TransmitJsonForBuild packet = new TransmitJsonForBuild(currentTestInUI, firstPosition[0], firstPosition[1], firstPosition[2]);
            CurrentTestUnderConstruction.updateTest(player, currentTestInUI);

            // Send the packet to the server
            CTFNetworkHandler.INSTANCE.sendToServer(packet);
        }));
    }

    // Todo: maybe add a way for this to persist between restarts? Not sure, don't want to confuse people.
    private static File initialDirForOpenJson;

    private void setupMenuItems() {
        if (initialDirForOpenJson == null) {
            initialDirForOpenJson = new File(
                Loader.instance()
                    .getConfigDir(),
                "/CTF/testing");
        }

        // Existing OpenMenuItem setup
        OpenMenuItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Json Files", "*.json"));
            fileChooser.setInitialDirectory(initialDirForOpenJson);

            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                initialDirForOpenJson = selectedFile.getParentFile();
                try {
                    updateFromJson(loadJsonFromFile(selectedFile.toPath()));
                    addRecentFile(selectedFile); // Add to recent files
                } catch (Exception exception) {
                    showAlert("Error loading JSON", "Test could not be loaded. Check the log for details.");
                    exception.printStackTrace();
                }
            } else {
                System.out.println("No file selected");
            }
        });

        // Populate recent files menu
        populateRecentFilesMenu();
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
                    private final Label label = new Label(); // Create the label for the text box

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            // Get the type of the current test config item from the TEST_CONFIG_TYPE map
                            String configType = TEST_CONFIG_TYPE.get(item);
                            JsonObject testConfig = currentTestInUI.get(TEST_CONFIG)
                                .getAsJsonObject();

                            if ("bool".equals(configType)) {
                                checkBox.setText(item);
                                checkBox.setSelected(
                                    testConfig.get(item)
                                        .getAsBoolean());
                                setGraphic(checkBox);
                                checkBox.setOnAction(event -> handleTestConfigCheckbox(checkBox));
                            } else if ("textbox".equals(configType)) {
                                label.setText(item); // Set the label text
                                textField.setText(
                                    testConfig.get(item)
                                        .getAsString());
                                textField.setMaxWidth(40);

                                // Create an HBox to contain the label and text field
                                HBox hbox = new HBox(10, textField, label);
                                hbox.setAlignment(Pos.CENTER_LEFT); // Center vertically, align left horizontally
                                setGraphic(hbox);

                                textField.setOnAction(event -> handleTestConfigTextInput(textField, item));

                                // Ensure only numbers are inputted in the text field
                                textField.textProperty()
                                    .addListener((observable, oldValue, newValue) -> {
                                        if (!newValue.matches("\\d*")) {
                                            textField.setText(oldValue); // Reject non-numeric input
                                        } else {
                                            textField.setText(newValue);
                                            currentTestInUI.get(TEST_CONFIG)
                                                .getAsJsonObject()
                                                .addProperty(item, newValue);
                                        }
                                    });
                            }
                        }
                    }
                };
            }
        });
    }

    private void handleTestConfigCheckbox(CheckBox checkBox) {
        JsonObject testConfig = currentTestInUI.get(TEST_CONFIG)
            .getAsJsonObject();

        testConfig.addProperty(checkBox.getText(), checkBox.isSelected());
        ClientSideExecutor.add(() -> CurrentTestUnderConstruction.updateFromUI(deepCopyJson(currentTestInUI)));
    }

    private void handleTestConfigTextInput(TextField textField, String item) {
        JsonObject testConfig = currentTestInUI.get(TEST_CONFIG)
            .getAsJsonObject();
        JsonObject gameRules = testConfig.get(GAMERULES)
            .getAsJsonObject();

        gameRules.addProperty(item, textField.getText());
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

                    JsonObject testConfig = currentTestInUI.get(TEST_CONFIG)
                        .getAsJsonObject();
                    JsonObject gameRules = testConfig.get(GAMERULES)
                        .getAsJsonObject();
                    checkBox.setSelected(
                        gameRules.get(item)
                            .getAsBoolean());

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
        JsonObject testConfig = currentTestInUI.get(TEST_CONFIG)
            .getAsJsonObject();
        JsonObject gameRules = testConfig.get(GAMERULES)
            .getAsJsonObject();

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
    public static void updateFromJson(JsonObject currentTest) {
        if (currentTest != null) {

            instance.TestNameLabel.setText(
                currentTest.get(TEST_NAME)
                    .getAsString());

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
                    gamerules.add(entry.getKey());
                }

                testConfig.clear();
                testConfig.addAll(TEST_CONFIG_TYPE.keySet());

            }
        }
    }

    private static JsonObject currentTestInUI;

    // Refresh instruction list from the current test JSON
    public static void refreshUIWithJson() {
        JsonObject currentTest = deepCopyJson(CurrentTestUnderConstruction.getTestJson());

        Platform.runLater(() -> updateFromJson(currentTest));
    }

    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
}
