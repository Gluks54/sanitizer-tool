package ua.com.foxminded.sanitizer.ui;

import java.io.File;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class ExploreProjectWindow extends SharedTextAreaLog implements SanitizerWindow {
    private File selectedDirectory;
    private String title;
    private Button okButton = new Button();

    public ExploreProjectWindow(File originalProject) {
        super();
        this.selectedDirectory = originalProject;
    }

    @Override
    public void setMessages() {
        title = "Explore " + selectedDirectory.getAbsolutePath();
        okButton.setText("OK");
    }

    @Override
    public void setButtonsActions(Stage stage) {
        okButton.setOnAction(event -> {
            stage.close();
            getLog().info("quit explore original project folder " + selectedDirectory.getAbsolutePath());
        });
    }

    @Override
    public void show() {
        getLog().info("explore original project folder " + selectedDirectory.getAbsolutePath());
        setMessages();
        BorderPane root = new BorderPane();
        FlowPane bottomPane = new FlowPane();
        SplitPane splitPane = new SplitPane();
        FileTreeItem fileItem = new FileTreeItem(selectedDirectory);
        TreeView<File> fileView = new TreeView<File>(fileItem);

        int mainW = 800;
        int mainH = 600;

        splitPane.getItems().addAll(fileView, fileItem.getTableView());
        root.setCenter(splitPane);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.getChildren().add(okButton);
        bottomPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(10)));
        root.setBottom(bottomPane);
        Stage stage = new Stage();
        stage.setOnCloseRequest(event -> {
            getLog().info("quit explore original project folder " + selectedDirectory.getAbsolutePath());
        });
        setButtonsActions(stage);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));
        stage.setScene(new Scene(root, mainW, mainH));
        stage.setTitle(title);
        stage.show();

    }

}
