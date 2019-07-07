package ua.com.foxminded.sanitizer.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import ua.com.foxminded.sanitizer.worker.FileWorker;

public final class StartWindow extends SharedTextAreaLog implements SanitizerWindow, FileVisitor<Path> {
    private FileWorker fw;
    private Button selectOriginalFolderButton = new Button();
    private Button selectTemplateFileButton = new Button();
    private Button selectOutputFolderButton = new Button();
    private Label originalFolderStatusLabel = new Label();
    private Label templateFileStatusLabel = new Label();
    private Label outputFolderStatusLabel = new Label();
    private Label originalInfoLabel = new Label();
    private Button editTemplateButton = new Button();
    private Label outputInfoLabel = new Label();
    private File originalFolder;
    private File templateFile;
    private File outputFolder;
    private Button checkOriginalProjectFilesButton = new Button();
    private Button proceedOriginalProjectFilesButton = new Button();
    private boolean originalFolderSelected;
    private boolean templateFileSelected;
    private boolean outputFolderSelected;
    private String title;
    private int inset = 10;
    private long size;
    private int files;

    public StartWindow() {
        super();
        originalFolderStatusLabel
                .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
        templateFileStatusLabel
                .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
        outputFolderStatusLabel
                .setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
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
        checkOriginalProjectFilesButton.setText("Explore original files");
        proceedOriginalProjectFilesButton.setText("Process original files");
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
                    originalInfoLabel.setText("Size: " + size + " / Files: " + files);
                    getLog().info("original project root folder: " + originalFolder.getAbsolutePath());
                    originalFolderStatusLabel.setText("project ok at " + originalFolder.getName());
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
        });
        selectTemplateFileButton.setOnAction(event -> {
            getLog().info("trying select template file...");
            fc.setTitle("Select project template file");
            fc.setSelectedExtensionFilter(new ExtensionFilter("XML", "*.xml"));
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
        });
        checkOriginalProjectFilesButton.setOnAction(event -> {

        });
        proceedOriginalProjectFilesButton.setOnAction(event -> {

        });
        editTemplateButton.setOnAction(event -> {
            new TemplateEditor().show();
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
            GridPane.setMargin(element, new Insets(inset));
            if (element instanceof Button) {
                ((Button) element).setMaxWidth(220);
            }
        });

        StackPane logPane = new StackPane();
        logPane.getChildren().add(getLogTextArea());

        checkOriginalProjectFilesButton.setDisable(true);
        proceedOriginalProjectFilesButton.setDisable(true);
        FlowPane bottomPane = new FlowPane();
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.getChildren().add(checkOriginalProjectFilesButton);
        bottomPane.getChildren().add(proceedOriginalProjectFilesButton);
        bottomPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(inset)));

        root.setTop(topPane);
        root.setCenter(logPane);
        root.setBottom(bottomPane);

        getLog().addHandler(getTextAreaHandler());
        getLog().info("sanitizer started");

        int mainW = 800;
        int mainH = 600;

        setMessages();
        Stage stage = new Stage();
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
