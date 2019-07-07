package ua.com.foxminded.sanitizer.ui;

import java.io.File;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import ua.com.foxminded.sanitizer.worker.FileWorker;

@Log
public final class StartWindow implements FXWindow {
    private FileWorker fw;
    private Button selectOriginalFolderButton = new Button();
    private Button selectTemplateFileButton = new Button();
    private Button selectOutputFolderButton = new Button();
    private Label originalFolderStatusLabel = new Label();
    private Label templateFileStatusLabel = new Label();
    private Label outputFolderStatusLabel = new Label();
    private Button editTemplateButton = new Button();
    private TextArea logTextArea = new TextArea();
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
    private TextAreaHandler textAreaHandler = new TextAreaHandler();

    @Override
    public void setMessages() {
        title = "sanitizer";
        selectOriginalFolderButton.setText("Original project folder");
        selectTemplateFileButton.setText("Select template file");
        selectOutputFolderButton.setText("Output project folder");
        originalFolderStatusLabel.setText("not selected");
        templateFileStatusLabel.setText("not selected");
        outputFolderStatusLabel.setText("not selected");
        checkOriginalProjectFilesButton.setText("Check original files");
        proceedOriginalProjectFilesButton.setText("Proceed original files");
        editTemplateButton.setText("Edit or new template");
    }

    @Override
    public void setButtonsActions(Stage stage) {
        fw = new FileWorker();
        DirectoryChooser dc = new DirectoryChooser();
        selectOriginalFolderButton.setOnAction(event -> {
            dc.setTitle("Select original project root folder");
            originalFolder = dc.showDialog(stage);
            if (originalFolder != null) {
                if (fw.isMavenProject(originalFolder)) {
                    originalFolderStatusLabel.setText("project ok at " + originalFolder.getName());
                    stage.setTitle(stage.getTitle() + " " + originalFolder.getAbsolutePath());
                    originalFolderStatusLabel.setGraphic(
                            new ImageView(new Image(getClass().getResourceAsStream("/img/project/maven.png"))));
                } else {
                    originalFolderStatusLabel.setText("ordinary directory");
                    stage.setTitle(title);
                    originalFolderStatusLabel.setGraphic(null);
                }
                originalFolderSelected = true;
            } else {
                if (!originalFolderSelected) {
                    originalFolderSelected = false;
                }
            }
        });
        selectTemplateFileButton.setOnAction(event -> {
            dc.setTitle("Select project template file");
            templateFile = dc.showDialog(stage);
            if (templateFile != null) {
                templateFileSelected = true;
            } else {
                if (!templateFileSelected) {
                    templateFileSelected = false;
                }
            }
        });
        selectOutputFolderButton.setOnAction(event -> {
            dc.setTitle("Select output project root folder");
            outputFolder = dc.showDialog(stage);
            if (outputFolder != null) {
                outputFolderSelected = true;
            } else {
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
        StackPane logPane = new StackPane();
        FlowPane bottomPane = new FlowPane();

        topPane.setGridLinesVisible(true);
        topPane.add(selectOriginalFolderButton, 0, 0);
        topPane.add(selectTemplateFileButton, 0, 1);
        topPane.add(selectOutputFolderButton, 0, 2);
        topPane.add(new Label("status:"), 1, 0);
        topPane.add(new Label("status:"), 1, 1);
        topPane.add(new Label("status:"), 1, 2);
        topPane.add(originalFolderStatusLabel, 2, 0);
        topPane.add(templateFileStatusLabel, 2, 1);
        topPane.add(outputFolderStatusLabel, 2, 2);
        topPane.add(editTemplateButton, 3, 1);
        topPane.getChildren().forEach(element -> {
            GridPane.setMargin(element, new Insets(10));
            if (element instanceof Button) {
                ((Button) element).setMaxWidth(220);
            }
            if (element instanceof Label) {
                ((Label) element).setMaxWidth(400);
            }
        });

        logTextArea.setEditable(false);
        textAreaHandler.setTextArea(logTextArea);
        logPane.getChildren().add(logTextArea);

        checkOriginalProjectFilesButton.setDisable(true);
        proceedOriginalProjectFilesButton.setDisable(true);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.getChildren().add(checkOriginalProjectFilesButton);
        bottomPane.getChildren().add(proceedOriginalProjectFilesButton);
        bottomPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(inset)));

        root.setTop(topPane);
        root.setCenter(logPane);
        root.setBottom(bottomPane);

        log.addHandler(textAreaHandler);
        log.info("sanitizer started");

        int mainW = 800;
        int mainH = 600;
        Stage stage = new Stage();
        setMessages();
        setButtonsActions(stage);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));
        stage.setScene(new Scene(root, mainW, mainH));
        stage.setTitle(title);
        stage.show();
    }
}
