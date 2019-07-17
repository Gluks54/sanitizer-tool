package ua.com.foxminded.sanitizer.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import ua.com.foxminded.sanitizer.data.Config;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;
import ua.com.foxminded.sanitizer.worker.ConfigWorker;
import ua.com.foxminded.sanitizer.worker.FileWorker;

public final class MainAppWindow extends SharedTextAreaLog implements ISanitizerWindow, FileVisitor<Path> {
    private FileWorker fileWorker;
    @Setter
    private Config config;
    private Button selectOriginalFolderButton = new Button();
    private Button selectConfigFileButton = new Button();
    private Button selectOutputFolderButton = new Button();
    private Label originalFolderStatusLabel = new Label();
    @Getter
    private Label configFileStatusLabel = new Label();
    private Label outputFolderStatusLabel = new Label();
    private Label originalInfoLabel = new Label();
    private Button editConfigButton = new Button();
    private Label outputInfoLabel = new Label();
    private File originalFolder;
    @Setter
    private File configFile;
    private File outputFolder;
    private File stripFolder;
    private Button exploreOriginalProjectFilesButton = new Button();
    private Button prepareOutputFolderButton = new Button();
    private Button stripOriginalProjectFilesButton = new Button();
    private boolean isOriginalFolderSelected;
    @Setter
    private boolean isProperConfigFileSelected;
    @Setter
    private boolean isOutputFolderPrepared;
    private boolean isOutputFolderSelected;
    private String title;
    private long size;
    private int files;

    public MainAppWindow() {
        super();
        originalFolderStatusLabel
                .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
        configFileStatusLabel
                .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
        outputFolderStatusLabel
                .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
    }

    protected void toggleBottomButtons() {
        exploreOriginalProjectFilesButton.setDisable(!(isOriginalFolderSelected && isProperConfigFileSelected));
        prepareOutputFolderButton
                .setDisable(!(isOriginalFolderSelected && isProperConfigFileSelected && isOutputFolderSelected));
        stripOriginalProjectFilesButton
                .setDisable(!(isOriginalFolderSelected && isProperConfigFileSelected && isOutputFolderPrepared));
    }

    @Override
    public void setMessages() {
        title = "sanitizer";
        selectOriginalFolderButton.setText("Original project folder");
        selectConfigFileButton.setText("Select config file");
        selectOutputFolderButton.setText("Output project folder");
        originalFolderStatusLabel.setText("not selected");
        configFileStatusLabel.setText("not selected");
        outputFolderStatusLabel.setText("not selected");
        exploreOriginalProjectFilesButton.setText("Explore original project");
        prepareOutputFolderButton.setText("Prepare output folder");
        stripOriginalProjectFilesButton.setText("Strip original project");
        editConfigButton.setText("Edit or new config");
    }

    @Override
    public void setButtonsActions(Stage stage) {
        fileWorker = new FileWorker();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        FileChooser fileChooser = new FileChooser();
        selectOriginalFolderButton.setOnAction(event -> {
            getLog().info("trying select original project root folder...");
            directoryChooser.setTitle("Select original project root folder");
            originalFolder = directoryChooser.showDialog(stage);
            if (originalFolder != null) {
                getLog().info("select original project root folder");
                if (fileWorker.isMavenProject(originalFolder)) {
                    processDirectory(originalFolder);
                    originalInfoLabel.setText("Size: " + fileWorker.turnFileSizeToString(size) + " / Files: " + files);
                    getLog().info("original project root folder: " + originalFolder.getAbsolutePath());
                    originalFolderStatusLabel.setText(
                            "project at " + originalFolder.getName() + " " + ISanitizerWindow.Status.OK.getStatus());
                    originalFolderStatusLabel.setGraphic(
                            new ImageView(new Image(getClass().getResourceAsStream("/img/project/maven.png"))));
                    getLog().info("+++ maven project found at " + originalFolder);
                    stage.setTitle(stage.getTitle() + " " + originalFolder.getAbsolutePath());
                    isOriginalFolderSelected = true;
                } else {
                    originalFolderStatusLabel.setText("ordinary directory");
                    originalFolderStatusLabel.setGraphic(
                            new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
                    getLog().info("no proper projects here: " + originalFolder.toString());
                    isOriginalFolderSelected = false;
                    stage.setTitle(title);
                }
            } else {
                originalFolderStatusLabel.setText("cancel select");
                originalFolderStatusLabel
                        .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
                getLog().info("cancel select original project root folder");
                if (!isOriginalFolderSelected) {
                    isOriginalFolderSelected = false;
                }
            }
            toggleBottomButtons();
        });
        selectConfigFileButton.setOnAction(event -> {
            getLog().info("trying select config file...");
            fileChooser.setTitle("Select project config file");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml"));
            configFile = fileChooser.showOpenDialog(stage);
            if (configFile != null) {
                configFileStatusLabel.setText(configFile.getAbsolutePath());
                getLog().info("select config file " + configFile.getAbsolutePath());

                config = new ConfigWorker().readConfigData(configFile, Config.class);
                if (config != null) {
                    configFileStatusLabel
                            .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/ok.png"))));
                    isProperConfigFileSelected = true;
                    getLog().info("+++ " + configFile.getName() + " is proper sanitizer config");
                } else {
                    configFileStatusLabel.setGraphic(
                            new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Config error");
                    alert.setHeaderText(configFile.getName() + " doesn't looks like proper sanitizer config");
                    alert.setContentText("Use editor");
                    alert.showAndWait();
                }
            } else {
                configFileStatusLabel.setText("cancel select");
                configFileStatusLabel
                        .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
                getLog().info("cancel select config file");
                if (!isProperConfigFileSelected) {
                    isProperConfigFileSelected = false;
                }
            }
            toggleBottomButtons();
        });
        selectOutputFolderButton.setOnAction(event -> {
            getLog().info("trying select output project folder...");
            directoryChooser.setTitle("Select output project root folder");
            outputFolder = directoryChooser.showDialog(stage);
            if (outputFolder != null) {
                if (outputFolder.getAbsolutePath().equals(originalFolder.getAbsolutePath())) {
                    getLog().info("wrong output project folder selected!");
                    isOutputFolderSelected = false;
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Wrong folder selected!");
                    alert.setHeaderText("Original and output folder couldn't be the same");
                    alert.setContentText("Choose another output project folder");
                    alert.showAndWait();
                } else {
                    if (outputFolder.getFreeSpace() > size) {
                        outputFolderStatusLabel.setText(outputFolder.getAbsolutePath());
                        outputFolderStatusLabel.setGraphic(
                                new ImageView(new Image(getClass().getResourceAsStream("/img/sign/ok.png"))));
                        getLog().info("select output project folder " + outputFolder.getAbsolutePath());
                        outputInfoLabel
                                .setText("Free space: " + fileWorker.turnFileSizeToString(outputFolder.getFreeSpace()));
                        isOutputFolderSelected = true;
                    } else {
                        outputFolderStatusLabel.setText(outputFolder.getAbsolutePath());
                        outputFolderStatusLabel.setGraphic(
                                new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
                        getLog().info("!!! not enough space in " + outputFolder.getAbsolutePath());
                        outputInfoLabel.setText("Not enough space!");
                        isOutputFolderSelected = false;
                    }
                }
            } else {
                outputFolderStatusLabel.setText("cancel select");
                outputFolderStatusLabel
                        .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
                getLog().info("cancel select output project folder");
                if (!isOutputFolderSelected) {
                    isOutputFolderSelected = false;
                }
            }
            toggleBottomButtons();
        });
        exploreOriginalProjectFilesButton.setOnAction(event -> new ExploreProjectWindow(originalFolder, config).show());
        prepareOutputFolderButton.setOnAction(event -> {
            getLog().info("prepare output folder " + outputFolder);
            PrepareWindow prepareWindow = new PrepareWindow(originalFolder, outputFolder, config, this);
            prepareWindow.show();
        });
        stripOriginalProjectFilesButton.setOnAction(event -> {
            getLog().info("strip files according to config " + configFile.getAbsolutePath());
            new StripWindow(originalFolder, outputFolder, config).show();
        });
        editConfigButton.setOnAction(event -> {
            ConfigEditorWindow configEditor;
            if (configFile != null) {
                getLog().info("load " + configFile.getAbsolutePath() + " to config editor");
                config = new ConfigWorker().readConfigData(configFile, Config.class);

                if (config != null) {
                    configEditor = new ConfigEditorWindow(config, configFile);
                    configEditor.setMainAppWindow(this);
                    configEditor.show();
                } else {
                    Alert alert = new Alert(AlertType.WARNING, configFile.getName() + " not a config. Run new config?",
                            ButtonType.YES, ButtonType.NO);
                    Optional<ButtonType> option = alert.showAndWait();
                    if (option.get() == ButtonType.YES) {
                        configEditor = new ConfigEditorWindow();
                        configEditor.setMainAppWindow(this);
                        configEditor.show();
                    } else {
                        getLog().info("cancel new config");
                    }
                }
            } else {
                configEditor = new ConfigEditorWindow();
                configEditor.setMainAppWindow(this);
                configEditor.show();
            }
        });
    }

    @Override
    public void show() {
        BorderPane root = new BorderPane();

        GridPane topPane = new GridPane();
        ColumnConstraints buttonsLeftColumn = new ColumnConstraints();
        buttonsLeftColumn.setPercentWidth(25);
        topPane.getColumnConstraints().add(buttonsLeftColumn);
        ColumnConstraints statusLabelColumn = new ColumnConstraints();
        statusLabelColumn.setPercentWidth(50);
        topPane.getColumnConstraints().add(statusLabelColumn);
        ColumnConstraints buttonsRightColumn = new ColumnConstraints();
        buttonsRightColumn.setPercentWidth(25);
        topPane.getColumnConstraints().add(buttonsRightColumn);

        topPane.setGridLinesVisible(false);
        topPane.add(selectOriginalFolderButton, 0, 0);
        topPane.add(selectConfigFileButton, 0, 1);
        topPane.add(selectOutputFolderButton, 0, 2);
        topPane.add(originalFolderStatusLabel, 1, 0);
        topPane.add(configFileStatusLabel, 1, 1);
        topPane.add(outputFolderStatusLabel, 1, 2);
        topPane.add(originalInfoLabel, 2, 0);
        topPane.add(editConfigButton, 2, 1);
        topPane.add(outputInfoLabel, 2, 2);

        topPane.getChildren().forEach(element -> {
            GridPane.setMargin(element, new Insets(ISanitizerWindow.INSET));
            if (element instanceof Button) {
                ((Button) element).setMaxWidth(220);
            }
        });
        StackPane logPane = new StackPane();
        logPane.getChildren().add(getLogTextArea());

        exploreOriginalProjectFilesButton.setDisable(true);
        prepareOutputFolderButton.setDisable(true);
        stripOriginalProjectFilesButton.setDisable(true);
        FlowPane bottomPane = new FlowPane();
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setId("bottomPane");
        bottomPane.getChildren().add(exploreOriginalProjectFilesButton);
        bottomPane.getChildren().add(prepareOutputFolderButton);
        bottomPane.getChildren().add(stripOriginalProjectFilesButton);
        bottomPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(ISanitizerWindow.INSET)));

        root.setTop(topPane);
        root.setCenter(logPane);
        root.setBottom(bottomPane);

        getLog().addHandler(getTextAreaHandler());
        getLog().info("sanitizer started");

        setMessages();
        Stage stage = new Stage();
        stage.setOnCloseRequest(event -> getLog().info("bye!"));
        setButtonsActions(stage);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));

        stage.setScene(new Scene(root, ISanitizerWindow.MAIN_W, ISanitizerWindow.MAIN_H));
        stage.setTitle(title);
        stage.show();
    }

    private void processDirectory(File dir) {
        size = 0;
        files = 0;
        try {
            getLog().info("process " + dir.toString());
            Files.walkFileTree(Paths.get(dir.getAbsolutePath()), this);
        } catch (IOException e) {
            getLog().severe("error in " + dir.toString());
            e.printStackTrace();
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        // TODO Auto-generated method stub
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (!file.toFile().isDirectory()) {
            size += file.toFile().length();
            files++;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        // TODO Auto-generated method stub
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        // TODO Auto-generated method stub
        return FileVisitResult.CONTINUE;
    }
}
