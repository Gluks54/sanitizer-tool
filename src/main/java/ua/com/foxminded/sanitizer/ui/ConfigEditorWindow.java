package ua.com.foxminded.sanitizer.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ua.com.foxminded.sanitizer.ISanitizerEnvironment;
import ua.com.foxminded.sanitizer.data.Config;
import ua.com.foxminded.sanitizer.ui.elements.ReplacementPane;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;
import ua.com.foxminded.sanitizer.worker.config.IConfigWorker;
import ua.com.foxminded.sanitizer.worker.config.XMLConfigWorker;

@RequiredArgsConstructor
public class ConfigEditorWindow extends SharedTextAreaLog implements ISanitizerWindow, ISanitizerEnvironment {
    @Getter
    @NonNull
    private Config config;
    private IConfigWorker configWorker = new XMLConfigWorker();
    @NonNull
    private File configFile;
    @NonNull
    private File originalProject;
    @NonNull
    private File outputProject;
    private Status operationStatus;
    private Label originalProjectLabel = new Label();
    private Label outputProjectLabel = new Label();
    private Button newConfigButton = new Button();
    private Button saveConfigButton = new Button();
    private Button cancelButton = new Button();
    private Button addContentReplacementButton = new Button();
    private Button addFileSystemReplacementButton = new Button();
    private ReplacementPane contentReplacementPane = new ReplacementPane();
    private ReplacementPane filesystemReplacementPane = new ReplacementPane();
    private CheckBox removeCommentsCheckBox = new CheckBox();
    private final List<CheckBox> extensions = Arrays.asList(new CheckBox(".java"), new CheckBox(".xml"),
            new CheckBox(".sql"), new CheckBox(".ts"));
    private final HBox filePatternHBox = new HBox();
    private final CheckBox filePatternCheckBox = new CheckBox();
    private final TextField filePatternTextField = new TextField();
    @Setter
    private MainAppWindow mainAppWindow;

    public ConfigEditorWindow(File originalProject, File outputProject) {
        super();
        this.originalProject = originalProject;
        this.outputProject = outputProject;
    }

    @Override
    public void show() {
        setMessages();
        GridPane topPane = new GridPane();
        topPane.setId("topPane");
        ColumnConstraints mainColumn = new ColumnConstraints();
        mainColumn.setPercentWidth(100);
        topPane.getColumnConstraints().add(mainColumn);
        FlowPane extensionsPane = new FlowPane();
        extensionsPane.getChildren().add(new Label("Files pattern:"));
        extensions.forEach(extension -> extension.setSelected(
                (extension.getText().equalsIgnoreCase(".java")) || (extension.getText().equalsIgnoreCase(".xml"))));
        extensionsPane.getChildren().addAll(extensions);

        filePatternTextField.setEditable(false);
        filePatternHBox.setAlignment(Pos.BASELINE_CENTER);
        filePatternHBox.getChildren().addAll(filePatternCheckBox, filePatternTextField);
        extensionsPane.getChildren().add(filePatternHBox);
        extensionsPane.setAlignment(Pos.CENTER);
        extensionsPane.setId("topPane");
        extensionsPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(ISanitizerWindow.INSET)));

        topPane.add(extensionsPane, 0, 1);
        GridPane.setMargin(originalProjectLabel, new Insets(ISanitizerWindow.INSET));
        topPane.add(originalProjectLabel, 0, 2);
        GridPane.setMargin(outputProjectLabel, new Insets(ISanitizerWindow.INSET));
        topPane.add(outputProjectLabel, 0, 3);

        BorderPane centerPane = new BorderPane();
        FlowPane centerTopButtonsPane = new FlowPane();
        centerTopButtonsPane.getChildren().addAll(removeCommentsCheckBox, addContentReplacementButton,
                addFileSystemReplacementButton);
        centerTopButtonsPane.getChildren()
                .forEach(node -> FlowPane.setMargin(node, new Insets(ISanitizerWindow.INSET)));
        centerPane.setTop(centerTopButtonsPane);
        SplitPane splitCenterPane = new SplitPane();
        splitCenterPane.setOrientation(Orientation.VERTICAL);
        splitCenterPane.getItems().addAll(contentReplacementPane, filesystemReplacementPane);

        centerPane.setCenter(splitCenterPane);

        FlowPane bottomButtonsPane = new FlowPane();
        bottomButtonsPane.setAlignment(Pos.CENTER);
        bottomButtonsPane.setId("bottomPane");
        bottomButtonsPane.getChildren().addAll(newConfigButton, saveConfigButton, cancelButton);
        bottomButtonsPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(INSET)));

        BorderPane root = new BorderPane();
        root.setTop(topPane);
        root.setCenter(centerPane);
        root.setBottom(bottomButtonsPane);
        Stage stage = new Stage();
        setButtonsActions(stage);
        stage.setOnCloseRequest(event -> {
            getLog().info("cancel config");
        });

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));
        stage.setScene(new Scene(root, CONFIGEDITOR_W, CONFIGEDITOR_H));

        if (config != null && configFile != null) {
            loadConfigData();
            stage.setTitle("Edit config " + configFile.getAbsolutePath());
            getLog().info("edit config " + configFile.getAbsolutePath());
        } else {
            stage.setTitle("New config file");
            getLog().info("new config file started");
        }
        stage.show();
    }

    public void loadConfigData() {
        if (config.getPatterns() != null) {
            extensions.stream().forEach(extension -> {
                extension.setSelected(
                        config.getPatterns().stream().anyMatch(config -> config.equalsIgnoreCase(extension.getText()))
                                ? true
                                : false);
            });
            operationStatus = Status.OK;
        } else {
            operationStatus = Status.FAIL;
        }
        getLog().info("...load file extensions: " + operationStatus.getStatus());

        if (config.getCustomPattern() != null) {
            filePatternCheckBox.setSelected(true);
            filePatternTextField.setEditable(true);
            filePatternTextField.setText(config.getCustomPattern());
            operationStatus = Status.OK;
        } else {
            operationStatus = Status.FAIL;
        }
        getLog().info("...load custom file pattern: " + operationStatus.getStatus());

        removeCommentsCheckBox.setSelected(config.isRemoveComments());
        getLog().info("...load remove comments feature: " + Status.OK.getStatus());

        if (config.getReplacementInFileContent() != null
                && config.getReplacementInFileContent().entrySet().size() > 0) {
            config.getReplacementInFileContent().entrySet().stream()
                    .forEach(entry -> contentReplacementPane.addReplacementItem(
                            contentReplacementPane.new ReplacementItem(entry.getKey(), entry.getValue().getSource(),
                                    entry.getValue().getTarget(), contentReplacementPane)));
            operationStatus = Status.OK;
        } else {
            operationStatus = Status.FAIL;
        }
        getLog().info("...load per-file replacements: " + operationStatus.getStatus());

        if (config.getReplacementInProjectStructure() != null
                && config.getReplacementInProjectStructure().entrySet().size() > 0) {
            config.getReplacementInProjectStructure().entrySet().stream()
                    .forEach(entry -> filesystemReplacementPane.addReplacementItem(
                            filesystemReplacementPane.new ReplacementItem(entry.getKey(), entry.getValue().getSource(),
                                    entry.getValue().getTarget(), filesystemReplacementPane)));
            operationStatus = Status.OK;
        } else {
            operationStatus = Status.FAIL;
        }
        getLog().info("...load project structure replacements: " + operationStatus.getStatus());

        operationStatus = (config.getOriginalProject() != null && config.getOutputProject() != null) ? Status.OK
                : Status.FAIL;
        getLog().info("...load original and output project folders: " + operationStatus.getStatus());
    }

    public void clearConfig() {
        removeCommentsCheckBox.setSelected(false);
        contentReplacementPane.clear();
        filesystemReplacementPane.clear();
        extensions.stream().forEach(extension -> extension.setSelected(
                (extension.getText().equalsIgnoreCase(".java")) || (extension.getText().equalsIgnoreCase(".xml"))));
        filePatternCheckBox.setSelected(false);
        filePatternTextField.setText("custom pattern");
    }

    @Override
    public void setMessages() {
        newConfigButton.setText("New config");
        saveConfigButton.setText("Save config");
        cancelButton.setText("Cancel");
        addContentReplacementButton.setText("Add per-file replacement");
        addFileSystemReplacementButton.setText("Add project structure replacement");
        removeCommentsCheckBox.setText("Remove comments");
        contentReplacementPane.setText("Per-file replacements");
        filesystemReplacementPane.setText("Project structure replacements");
        filePatternTextField.setText("custom pattern");
        originalProjectLabel.setText("Original project folder: " + originalProject);
        outputProjectLabel.setText("Output project folder: " + outputProject);
    }

    @Override
    public void setButtonsActions(Stage stage) {
        filePatternCheckBox.setOnAction(event -> {
            if (filePatternCheckBox.isSelected()) {
                filePatternTextField.setText("");
                filePatternTextField.setEditable(true);
            } else {
                filePatternTextField.setText("custom pattern");
                filePatternTextField.setEditable(false);
            }
        });
        newConfigButton.setOnAction(event -> {
            getLog().info("start new config");
            clearConfig();
        });
        saveConfigButton.setOnAction(event -> {
            Alert alert = new Alert(AlertType.ERROR);
            if (contentReplacementPane.isWrongDescriptionInReplacementItems()
                    || filesystemReplacementPane.isWrongDescriptionInReplacementItems()) {
                alert.setTitle("Description error");
                alert.setContentText("Empty descriptions are prohibited");
                alert.showAndWait();
            } else if (contentReplacementPane.isWrongSourceInReplacementItems()
                    || filesystemReplacementPane.isWrongSourceInReplacementItems()) {
                alert.setTitle("Source error");
                alert.setContentText("Empty sources are prohibited");
                alert.showAndWait();
            } else if (contentReplacementPane.isWrongTargetInReplacementItems()
                    || filesystemReplacementPane.isWrongTargetInReplacementItems()) {
                alert.setTitle("Target error");
                alert.setContentText("Empty targets are prohibited");
                alert.showAndWait();
            } else if (contentReplacementPane.isDuplicateDescriptionsInReplacementItems()
                    || filesystemReplacementPane.isDuplicateDescriptionsInReplacementItems()) {
                alert.setTitle("Description error");
                alert.setContentText("Similar descriptions are prohibited");
                alert.showAndWait();
            } else {
                FileChooser fc = new FileChooser();
                fc.getExtensionFilters().clear();
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(XML_DIALOG_NAME, XML_PATTERN));
                configFile = fc.showSaveDialog(stage);
                if (configFile != null) {
                    getLog().info("save current config to " + configFile.getAbsolutePath());
                    // считать все расширения файлов
                    List<String> patterns = new ArrayList<String>();
                    extensions.forEach(extension -> {
                        if (extension.isSelected()) {
                            patterns.add(extension.getText());
                        }
                    });
                    // читаем показатели из полей
                    // если config не проинжекчен конструктором,
                    // создаем новый
                    if (config == null) {
                        config = new Config();
                    }
                    config.setPatterns(patterns);
                    getLog().info("...save file extensions: " + Status.OK.getStatus());

                    if (contentReplacementPane.getReplacementsMap() != null
                            && contentReplacementPane.getReplacementsMap().size() > 0) {
                        config.setReplacementInFileContent(contentReplacementPane.getReplacementsMap());
                        operationStatus = Status.OK;
                    } else {
                        operationStatus = Status.FAIL;
                    }
                    getLog().info("...save per-file replacements: " + operationStatus.getStatus());

                    config.setRemoveComments(removeCommentsCheckBox.isSelected());
                    getLog().info("...save remove comments feature: " + Status.OK.getStatus());

                    // добавляем regexp из поля
                    if (filePatternCheckBox.isSelected() && (!filePatternTextField.getText().equals(""))
                            && (!filePatternTextField.getText().equals(null))) {
                        config.setCustomPattern(filePatternTextField.getText());
                        operationStatus = Status.OK;
                    } else {
                        config.setCustomPattern(null);
                        operationStatus = Status.FAIL;
                    }
                    System.out.println("=" + filePatternTextField.getText() + "=");
                    getLog().info("...save custom file regexp: " + operationStatus.getStatus());

                    if (filesystemReplacementPane.getReplacementsMap() != null
                            && filesystemReplacementPane.getReplacementsMap().size() > 0) {
                        config.setReplacementInProjectStructure(filesystemReplacementPane.getReplacementsMap());
                        operationStatus = Status.OK;
                    } else {
                        operationStatus = Status.FAIL;
                    }
                    getLog().info("...save project structure replacements: " + operationStatus.getStatus());

                    if (originalProject != null && outputProject != null) {
                        config.setOriginalProject(originalProject);
                        config.setOutputProject(outputProject);
                        operationStatus = Status.OK;
                    } else {
                        operationStatus = Status.FAIL;
                    }
                    getLog().info("...save original and output folder: " + operationStatus.getStatus());

                    if (configWorker.writeConfigData(configFile, config)) {
                        // записали, обновили статус, проверили кнопки снизу
                        mainAppWindow.setConfigFile(configFile);
                        mainAppWindow.setProperConfigFileSelected(true);
                        mainAppWindow.getConfigFileStatusLabel().setText(configFile.getAbsolutePath());
                        mainAppWindow.getConfigFileStatusLabel().setGraphic(
                                new ImageView(new Image(getClass().getResourceAsStream("/img/sign/ok.png"))));
                        mainAppWindow.checkAndToggleButtons();
                        mainAppWindow.setConfig(config);
                    } else {
                        mainAppWindow.getConfigFileStatusLabel().setText("cancel select");
                        mainAppWindow.getConfigFileStatusLabel().setGraphic(
                                new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
                    }
                } else {
                    getLog().info("cancel config save");
                }
                stage.close();
            }
        });
        cancelButton.setOnAction(event -> {
            getLog().info("cancel config save");
            stage.close();
        });
        addContentReplacementButton.setOnAction(event -> {
            contentReplacementPane.addReplacementItem(null);
        });
        addFileSystemReplacementButton.setOnAction(event -> {
            filesystemReplacementPane.addReplacementItem(null);
        });
    }

}
