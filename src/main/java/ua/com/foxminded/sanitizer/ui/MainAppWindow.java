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
import ua.com.foxminded.sanitizer.data.Template;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;
import ua.com.foxminded.sanitizer.worker.FileWorker;
import ua.com.foxminded.sanitizer.worker.TemplateWorker;

public final class MainAppWindow extends SharedTextAreaLog implements SanitizerWindow, FileVisitor<Path> {
    private FileWorker fw;
    private Button selectOriginalFolderButton = new Button();
    private Button selectTemplateFileButton = new Button();
    private Button selectOutputFolderButton = new Button();
    private Label originalFolderStatusLabel = new Label();
    @Getter
    private Label templateFileStatusLabel = new Label();
    private Label outputFolderStatusLabel = new Label();
    private Label originalInfoLabel = new Label();
    private Button editTemplateButton = new Button();
    private Label outputInfoLabel = new Label();
    private File originalFolder;
    @Setter
    private File templateFile;
    private File outputFolder;
    private Button exploreOriginalProjectFilesButton = new Button();
    private Button processOriginalProjectFilesButton = new Button();
    private boolean originalFolderSelected;
    @Setter
    private boolean templateFileSelected;
    private boolean outputFolderSelected;
    private String title;
    private long size;
    private int files;

    public MainAppWindow() {
        super();
        originalFolderStatusLabel
                .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
        templateFileStatusLabel
                .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
        outputFolderStatusLabel
                .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
    }

    protected void toggleBottomButtons() {
        exploreOriginalProjectFilesButton.setDisable(!(originalFolderSelected && templateFileSelected));
        processOriginalProjectFilesButton
                .setDisable(!(originalFolderSelected && templateFileSelected && outputFolderSelected));
    }

    @Override
    public void setMessages() {
        title = "sanitizer";
        selectOriginalFolderButton.setText("Original project folder");
        selectTemplateFileButton.setText("Select template file");
        selectOutputFolderButton.setText("Output project folder");
        originalFolderStatusLabel.setText("not selected");
        templateFileStatusLabel.setText("not selected");
        outputFolderStatusLabel.setText("not selected");
        exploreOriginalProjectFilesButton.setText("Explore original files");
        processOriginalProjectFilesButton.setText("Process original files");
        editTemplateButton.setText("Edit or new template");
    }

    @Override
    public void setButtonsActions(Stage stage) {
        fw = new FileWorker();
        DirectoryChooser dc = new DirectoryChooser();
        FileChooser fc = new FileChooser();
        selectOriginalFolderButton.setOnAction(event -> {
            getLog().info("trying select original project root folder...");
            dc.setTitle("Select original project root folder");
            originalFolder = dc.showDialog(stage);
            if (originalFolder != null) {
                getLog().info("select original project root folder");
                if (fw.isMavenProject(originalFolder)) {
                    processDirectory(originalFolder);
                    originalInfoLabel.setText("Size: " + fw.turnFileSizeToString(size) + " / Files: " + files);
                    getLog().info("original project root folder: " + originalFolder.getAbsolutePath());
                    originalFolderStatusLabel.setText(
                            "project at " + originalFolder.getName() + " " + SanitizerWindow.Status.OK.getStatus());
                    originalFolderStatusLabel.setGraphic(
                            new ImageView(new Image(getClass().getResourceAsStream("/img/project/maven.png"))));
                    getLog().info("+++ maven project found at " + originalFolder);
                    stage.setTitle(stage.getTitle() + " " + originalFolder.getAbsolutePath());
                    originalFolderSelected = true;
                } else {
                    originalFolderStatusLabel.setText("ordinary directory");
                    originalFolderStatusLabel.setGraphic(
                            new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
                    getLog().info("ordinary directory selected: " + originalFolder.toString());
                    getLog().info("no proper projects here");
                    originalFolderSelected = false;
                    stage.setTitle(title);
                }
            } else {
                originalFolderStatusLabel.setText("cancel select");
                originalFolderStatusLabel
                        .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
                getLog().info("cancel select original project root folder");
                if (!originalFolderSelected) {
                    originalFolderSelected = false;
                }
            }
            toggleBottomButtons();
        });
        selectTemplateFileButton.setOnAction(event -> {
            getLog().info("trying select template file...");
            fc.setTitle("Select project template file");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
            fc.getExtensionFilters().add(extFilter);
            templateFile = fc.showOpenDialog(stage);
            if (templateFile != null) {
                templateFileStatusLabel.setText(templateFile.getAbsolutePath());
                templateFileStatusLabel
                        .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/ok.png"))));
                getLog().info("select template file " + templateFile.getAbsolutePath());
                templateFileSelected = true;
            } else {
                templateFileStatusLabel.setText("cancel select");
                templateFileStatusLabel
                        .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
                getLog().info("cancel select template file");
                if (!templateFileSelected) {
                    templateFileSelected = false;
                }
            }
            toggleBottomButtons();
        });
        selectOutputFolderButton.setOnAction(event -> {
            getLog().info("trying select output project folder...");
            dc.setTitle("Select output project root folder");
            outputFolder = dc.showDialog(stage);
            if (outputFolder != null) {
                outputFolderStatusLabel.setText(outputFolder.getAbsolutePath());
                outputFolderStatusLabel
                        .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/ok.png"))));
                getLog().info("select output project folder " + outputFolder.getAbsolutePath());
                outputInfoLabel.setText("Free space: " + fw.turnFileSizeToString(outputFolder.getFreeSpace()));
                outputFolderSelected = true;
            } else {
                outputFolderStatusLabel.setText("cancel select");
                outputFolderStatusLabel
                        .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
                getLog().info("cancel select output project folder");
                if (!outputFolderSelected) {
                    outputFolderSelected = false;
                }
            }
            toggleBottomButtons();
        });
        exploreOriginalProjectFilesButton.setOnAction(event -> {
            new ExploreProjectWindow(originalFolder).show();
        });
        processOriginalProjectFilesButton.setOnAction(event -> {
            getLog().info("process files with template " + templateFile.getAbsolutePath());
        });
        editTemplateButton.setOnAction(event -> {
            TemplateEditor templateEditor;
            if (templateFile != null) {
                getLog().info("load " + templateFile.getAbsolutePath() + " to template editor");
                Template template = new TemplateWorker().readTemplateData(templateFile, Template.class);

                if (template != null) {
                    templateEditor = new TemplateEditor(template, templateFile);
                    templateEditor.setStartWindow(this);
                    templateEditor.show();
                } else {
                    Alert alert = new Alert(AlertType.WARNING,
                            templateFile.getName() + " not a template. Run new template?", ButtonType.YES,
                            ButtonType.NO);
                    Optional<ButtonType> option = alert.showAndWait();
                    if (option.get() == ButtonType.YES) {
                        templateEditor = new TemplateEditor();
                        templateEditor.setStartWindow(this);
                        templateEditor.show();
                    } else {
                        getLog().info("cancel new template");
                    }
                }
            } else {
                templateEditor = new TemplateEditor();
                templateEditor.setStartWindow(this);
                templateEditor.show();
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
        topPane.add(selectTemplateFileButton, 0, 1);
        topPane.add(selectOutputFolderButton, 0, 2);
        topPane.add(originalFolderStatusLabel, 1, 0);
        topPane.add(templateFileStatusLabel, 1, 1);
        topPane.add(outputFolderStatusLabel, 1, 2);
        topPane.add(originalInfoLabel, 2, 0);
        topPane.add(editTemplateButton, 2, 1);
        topPane.add(outputInfoLabel, 2, 2);

        topPane.getChildren().forEach(element -> {
            GridPane.setMargin(element, new Insets(SanitizerWindow.INSET));
            if (element instanceof Button) {
                ((Button) element).setMaxWidth(220);
            }
        });
        StackPane logPane = new StackPane();
        logPane.getChildren().add(getLogTextArea());

        exploreOriginalProjectFilesButton.setDisable(true);
        processOriginalProjectFilesButton.setDisable(true);
        FlowPane bottomPane = new FlowPane();
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setId("bottomPane");
        bottomPane.getChildren().add(exploreOriginalProjectFilesButton);
        bottomPane.getChildren().add(processOriginalProjectFilesButton);
        bottomPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(SanitizerWindow.INSET)));

        root.setTop(topPane);
        root.setCenter(logPane);
        root.setBottom(bottomPane);

        getLog().addHandler(getTextAreaHandler());
        getLog().info("sanitizer started");

        int mainW = 800;
        int mainH = 600;
        setMessages();
        Stage stage = new Stage();
        stage.setOnCloseRequest(event -> getLog().info("bye!"));
        setButtonsActions(stage);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));
        stage.setScene(new Scene(root, mainW, mainH));
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
