package ua.com.foxminded.sanitizer.ui;

import java.io.File;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ua.com.foxminded.sanitizer.data.Config;
import ua.com.foxminded.sanitizer.ui.elements.FileTreeItem;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

@RequiredArgsConstructor
public class ExploreProjectWindow extends SharedTextAreaLog implements ISanitizerWindow {
    @NonNull
    private File selectedDirectory;
    @NonNull
    private Config config;
    private String title;
    private Button okButton = new Button();

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
        FlowPane bottomPane = new FlowPane();
        SplitPane splitPane = new SplitPane();
        FileTreeItem fileItem = new FileTreeItem(selectedDirectory, config);
        TreeView<File> fileView = new TreeView<File>(fileItem);

        fileView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {
            @Override
            public TreeCell<File> call(TreeView<File> param) {
                return (new FileTreeItem()).new CustomFileTreeCell();
            }
        });
        fileView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> fileItem.processDirectory(newValue.getValue().toPath()));
        fileItem.setExpanded(true);

        splitPane.getItems().addAll(fileView, fileItem.getTableView());
        splitPane.setDividerPositions(0.3);
        getRoot().setCenter(splitPane);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.getChildren().add(okButton);
        bottomPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(ISanitizerWindow.INSET)));
        getRoot().setBottom(bottomPane);
        Stage stage = new Stage();
        stage.setOnCloseRequest(
                event -> getLog().info("quit explore original project folder " + selectedDirectory.getAbsolutePath()));
        setButtonsActions(stage);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));
        stage.setScene(new Scene(getRoot(), ISanitizerWindow.EXPLORE_W, ISanitizerWindow.EXPLORE_H));
        stage.setTitle(title);
        stage.show();

    }

}
