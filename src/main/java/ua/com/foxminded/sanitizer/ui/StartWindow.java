package ua.com.foxminded.sanitizer.ui;

import java.io.File;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ua.com.foxminded.sanitizer.worker.FileWorker;

public final class StartWindow {
    private FileWorker fw;
    private Button selectOriginalFolder = new Button();
    private Button selectTemplateFile = new Button();
    private Button selectOutputFolder = new Button();
    private Label originalFolderStatus = new Label();
    private Label templateFileStatus = new Label();
    private Label outputFolderStatus = new Label();
    private TextArea logTextArea = new TextArea();
    private File originalFolder;
    private File templateFile;
    private File outputFolder;
    private String title = "sanitizer";

    private void setMessages() {
        selectOriginalFolder.setText("Original project folder");
        selectTemplateFile.setText("Select Template File");
        selectOutputFolder.setText("Output project folder");
        originalFolderStatus.setText("not selected");
        templateFileStatus.setText("not selected");
        outputFolderStatus.setText("not selected");
    }

    private void setButtonsActions(Stage stage) {
        fw = new FileWorker();
        DirectoryChooser dc = new DirectoryChooser();
        selectOriginalFolder.setOnAction(event -> {
            dc.setTitle("Select original project root folder");
            originalFolder = dc.showDialog(stage);
            if (originalFolder != null) {
                if (fw.isMavenProject(originalFolder)) {
                    originalFolderStatus.setText("project ok at " + originalFolder.getName());
                    stage.setTitle(stage.getTitle() + " " + originalFolder.getAbsolutePath());
                    originalFolderStatus.setGraphic(
                            new ImageView(new Image(getClass().getResourceAsStream("/img/project/maven.png"))));
                } else {
                    originalFolderStatus.setText("ordinary directory");
                    stage.setTitle(title);
                    originalFolderStatus.setGraphic(null);
                }
            }
        });
        selectTemplateFile.setOnAction(event -> {
            dc.setTitle("Select project template file");
            templateFile = dc.showDialog(stage);
            if (templateFile != null) {

            }
        });
        selectOutputFolder.setOnAction(event -> {
            dc.setTitle("Select output project root folder");
            outputFolder = dc.showDialog(stage);
            if (outputFolder != null) {

            }
        });
    }

    public void show(Stage stage) {
        setMessages();
        setButtonsActions(stage);

        BorderPane root = new BorderPane();
        GridPane topPane = new GridPane();
        StackPane logPane = new StackPane();

        topPane.add(selectOriginalFolder, 0, 0);
        topPane.add(selectTemplateFile, 0, 1);
        topPane.add(selectOutputFolder, 0, 2);
        topPane.add(new Label("status:"), 1, 0);
        topPane.add(new Label("status:"), 1, 1);
        topPane.add(new Label("status:"), 1, 2);
        topPane.add(originalFolderStatus, 2, 0);
        topPane.add(templateFileStatus, 2, 1);
        topPane.add(outputFolderStatus, 2, 2);
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
        logPane.getChildren().add(logTextArea);

        root.setTop(topPane);
        root.setCenter(logPane);

        int mainW = 800;
        int mainH = 600;
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));
        stage.setScene(new Scene(root, mainW, mainH));
        stage.setTitle(title);
        stage.show();
    }
}
