package ua.com.foxminded.sanitizer.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import ua.com.foxminded.sanitizer.project.AbstractProject;
import ua.com.foxminded.sanitizer.project.AngularProject;
import ua.com.foxminded.sanitizer.project.MavenProject;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;
import ua.com.foxminded.sanitizer.worker.FileWorker;
import ua.com.foxminded.sanitizer.worker.IConfigWorker;
import ua.com.foxminded.sanitizer.worker.XMLConfigWorker;

public final class MainAppWindow extends SharedTextAreaLog implements ISanitizerWindow {
    private FileWorker fileWorker;
    @Setter
    private Config config;
    private IConfigWorker configWorker = new XMLConfigWorker();
    private Button selectOriginalFolderButton = new Button();
    private Button selectConfigFileButton = new Button();
    private Button selectOutputFolderButton = new Button();
    private Label originalFolderStatusLabel = new Label();
    @Getter
    private Label configFileStatusLabel = new Label();
    private Label outputFolderStatusLabel = new Label();
    private Label originalInfoLabel = new Label();
    private Button editOrNewConfigButton = new Button();
    private Label outputInfoLabel = new Label();
    private File originalFolder;
    @Setter
    private File configFile;
    private File outputPreparedFolder;
    private File baseFolder;
    private Button exploreOriginalProjectFilesButton = new Button();
    private Button prepareOutputFolderButton = new Button();
    private Button stripOriginalProjectFilesButton = new Button();
    private Button undoStrippedProjectFilesButton = new Button();
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
        exploreOriginalProjectFilesButton.setDisable(!(isOriginalFolderSelected));
        prepareOutputFolderButton.setDisable(!(isOriginalFolderSelected && isOutputFolderSelected));
        stripOriginalProjectFilesButton.setDisable(!isProperConfigFileSelected);
        undoStrippedProjectFilesButton.setDisable(!isProperConfigFileSelected);
        editOrNewConfigButton.setDisable(!(isOriginalFolderSelected && isOutputFolderSelected));
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
        undoStrippedProjectFilesButton.setText("Undo strip steps");
        editOrNewConfigButton.setText("Edit or new config");
    }

    private void fillOriginalFolderLine(Stage stage) {
        // выясняем, что за проект
        AbstractProject project = new MavenProject(originalFolder).isProperProject() ? new MavenProject(originalFolder)
                : (new AngularProject(originalFolder).isProperProject() ? new AngularProject(originalFolder) : null);

        if (project != null) {
            processDirectory(originalFolder);
            originalInfoLabel.setText("Size: " + fileWorker.turnFileSizeToString(size) + " / Files: " + files);
            getLog().info("original project root folder: " + originalFolder.getAbsolutePath());
            originalFolderStatusLabel
                    .setText("project at " + originalFolder.getName() + " " + ISanitizerWindow.Status.OK.getStatus());
            originalFolderStatusLabel.setGraphic(project.getProjectLabelIcon());
            stage.setTitle(stage.getTitle() + " " + originalFolder.getAbsolutePath());
            isOriginalFolderSelected = true;
        } else {
            originalFolderStatusLabel.setText("ordinary directory");
            originalFolderStatusLabel
                    .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
            getLog().info("no proper projects here: " + originalFolder.toString());
            isOriginalFolderSelected = false;
            stage.setTitle(title);
        }
    }

    private void fillOutputFolderLine() {
        // обрабатываем выходную папку
        if (originalFolder != null && outputPreparedFolder.getAbsolutePath().equals(originalFolder.getAbsolutePath())) {
            getLog().info("wrong output project folder selected!");
            isOutputFolderSelected = false;
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Wrong folder selected!");
            alert.setHeaderText("Original and output folder couldn't be the same");
            alert.setContentText("Choose another output or original project folder");
            alert.showAndWait();
        } else {
            if (outputPreparedFolder.getFreeSpace() > size) {
                outputFolderStatusLabel.setText(outputPreparedFolder.getAbsolutePath());
                outputFolderStatusLabel
                        .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/ok.png"))));
                getLog().info("select output project folder " + outputPreparedFolder.getAbsolutePath());
                outputInfoLabel
                        .setText("Free space: " + fileWorker.turnFileSizeToString(outputPreparedFolder.getFreeSpace()));
                isOutputFolderSelected = true;
            } else {
                outputFolderStatusLabel.setText(outputPreparedFolder.getAbsolutePath());
                outputFolderStatusLabel
                        .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
                getLog().info("!!! not enough space in " + outputPreparedFolder.getAbsolutePath());
                outputInfoLabel.setText("Not enough space!");
                isOutputFolderSelected = false;
            }
        }
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
                if (outputPreparedFolder != null
                        && outputPreparedFolder.getAbsolutePath().equals(originalFolder.getAbsolutePath())) {
                    getLog().info("wrong original project folder selected!");
                    isOriginalFolderSelected = false;
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Wrong folder selected!");
                    alert.setHeaderText("Original and output folder couldn't be the same");
                    alert.setContentText("Choose another output or original project folder");
                    alert.showAndWait();
                } else {
                    getLog().info("select original project root folder");
                    fillOriginalFolderLine(stage);
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

                config = configWorker.readConfigData(configFile, Config.class);
                if (config != null) {
                    configFileStatusLabel
                            .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/ok.png"))));
                    isProperConfigFileSelected = true;
                    getLog().info("+++ " + configFile.getName() + " is proper sanitizer config");

                    // проверяем пути исходной и выходной папок
                    if (originalFolder == null || outputPreparedFolder == null) {
                        originalFolder = config.getOriginalProject();
                        outputPreparedFolder = config.getOutputProject();
                        fillOriginalFolderLine(stage);
                        fillOutputFolderLine();
                    } else if (!config.getOriginalProject().getAbsolutePath()
                            .equalsIgnoreCase(originalFolder.getAbsolutePath())
                            || !config.getOutputProject().getAbsolutePath()
                                    .equalsIgnoreCase(outputPreparedFolder.getAbsolutePath())) {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Folders error");
                        alert.setHeaderText("Original or output folders doesn't match saved properties");
                        alert.setContentText("Folders you've chose will be replaced ones from config file!");
                        alert.showAndWait();
                        originalFolder = config.getOriginalProject();
                        outputPreparedFolder = config.getOutputProject();
                        fillOriginalFolderLine(stage);
                        fillOutputFolderLine();
                    }
                } else {
                    configFileStatusLabel.setGraphic(
                            new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
                    isProperConfigFileSelected = false;
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
            outputPreparedFolder = directoryChooser.showDialog(stage);
            if (outputPreparedFolder != null) {
                fillOutputFolderLine();
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
            if (!outputPreparedFolder.getAbsolutePath().equalsIgnoreCase(config.getOutputProject().getAbsolutePath())) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Folders mismatch!");
                alert.setHeaderText("Chosen output folder doesn't match saved to config");
                alert.setContentText("Fix before continue...");
                alert.showAndWait();
            } else if (!originalFolder.getAbsolutePath()
                    .equalsIgnoreCase(config.getOriginalProject().getAbsolutePath())) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Folders mismatch!");
                alert.setHeaderText("Chosen original folder doesn't match saved to config");
                alert.setContentText("Fix before continue...");
                alert.showAndWait();
            } else if (!fileWorker.isContainProperOriginalFolder(outputPreparedFolder)) {
                getLog().info("prepare output folder " + outputPreparedFolder);
                new PrepareWindow(originalFolder, outputPreparedFolder, config, this).show();
            } else {
                getLog().info("!!! " + outputPreparedFolder
                        + " already contains proper original project folder, choose another one!");
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Wrong folder!");
                alert.setHeaderText(outputPreparedFolder.toString());
                alert.setContentText("already contains proper original project folder, " + System.lineSeparator()
                        + "choose another one");
                alert.showAndWait();
            }
        });
        stripOriginalProjectFilesButton.setOnAction(event -> {
            directoryChooser.setTitle("Select base work folder");
            baseFolder = directoryChooser.showDialog(stage);
            if (baseFolder != null) {
                if (fileWorker.isContainProperOriginalFolder(baseFolder)) {
                    getLog().info("+++ strip files according to config " + configFile.getAbsolutePath());
                    new StripWindow(
                            new File(fileWorker.getProperOriginalFolderName(baseFolder)), new File(fileWorker
                                    .getProperOriginalFolderName(baseFolder).replaceAll(ORIG_SUFFIX, STRIP_SUFFIX)),
                            config).show();
                } else {
                    getLog().info("--- no proper original project folder found at " + baseFolder
                            + ". prepare project in advance");
                }
            } else {
                getLog().info("--- strip files cancelled");
            }
        });
        undoStrippedProjectFilesButton.setOnAction(event -> {
            directoryChooser.setTitle("Select base work folder");
            baseFolder = directoryChooser.showDialog(stage);
            if (baseFolder != null) {
                if (fileWorker.isContainProperOriginalFolder(baseFolder)
                        && fileWorker.isContainProperStripFolder(baseFolder)) {
                    getLog().info("*** start undo operations in " + baseFolder + " using config " + configFile);
                    new UndoSelectWindow(baseFolder, config).show();
                } else {
                    getLog().info("--- no proper original and strip project folder found at " + baseFolder);
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Wrong folder!");
                    alert.setHeaderText(baseFolder.toString());
                    alert.setContentText("no proper original and strip project here " + System.lineSeparator()
                            + "choose another one");
                    alert.showAndWait();
                }
            } else {
                getLog().info("--- undo cancelled");
            }
        });
        editOrNewConfigButton.setOnAction(event -> {
            ConfigEditorWindow configEditor;
            if (configFile != null) {
                getLog().info("load " + configFile.getAbsolutePath() + " to config editor");
                config = configWorker.readConfigData(configFile, Config.class);
                if (config != null) {
                    configEditor = new ConfigEditorWindow(config, configFile, originalFolder, outputPreparedFolder);
                    configEditor.setMainAppWindow(this);
                    configEditor.show();
                } else {
                    Alert alert = new Alert(AlertType.WARNING, configFile.getName() + " not a config. Run new config?",
                            ButtonType.YES, ButtonType.NO);
                    Optional<ButtonType> option = alert.showAndWait();
                    if (option.get() == ButtonType.YES) {
                        configEditor = new ConfigEditorWindow(originalFolder, outputPreparedFolder);
                        configEditor.setMainAppWindow(this);
                        configEditor.show();
                    } else {
                        getLog().info("cancel new config");
                    }
                }
            } else {
                configEditor = new ConfigEditorWindow(originalFolder, outputPreparedFolder);
                configEditor.setMainAppWindow(this);
                configEditor.show();
            }
        });
    }

    @Override
    public void show() {
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
        topPane.add(editOrNewConfigButton, 2, 1);
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
        undoStrippedProjectFilesButton.setDisable(true);
        editOrNewConfigButton.setDisable(true);
        FlowPane bottomPane = new FlowPane();
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setId("bottomPane");
        bottomPane.getChildren().addAll(exploreOriginalProjectFilesButton, prepareOutputFolderButton,
                stripOriginalProjectFilesButton, undoStrippedProjectFilesButton);
        bottomPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(ISanitizerWindow.INSET)));

        getRoot().setTop(topPane);
        getRoot().setCenter(logPane);
        getRoot().setBottom(bottomPane);

        getLog().addHandler(getTextAreaHandler());
        getLog().info("sanitizer started");

        setMessages();
        Stage stage = new Stage();
        stage.setOnCloseRequest(event -> getLog().info("bye!"));
        setButtonsActions(stage);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));

        stage.setScene(new Scene(getRoot(), ISanitizerWindow.MAIN_W, ISanitizerWindow.MAIN_H));
        stage.setTitle(title);
        stage.show();
    }

    private void processDirectory(File dir) {
        size = 0;
        files = 0;

        try (Stream<Path> walk = Files.walk(Paths.get(dir.toURI()))) {
            List<Path> result = walk.collect(Collectors.toList());
            for (Path path : result) {
                if (path.toFile().isFile()) {
                    files++;
                    size += path.toFile().length();
                }
            }
        } catch (IOException e) {
            getLog().severe("IO error during process file tree in " + dir);
        }
    }
}
