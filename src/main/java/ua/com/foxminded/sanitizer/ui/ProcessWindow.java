package ua.com.foxminded.sanitizer.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;
import ua.com.foxminded.sanitizer.worker.ProcessWorker;

public class ProcessWindow extends SharedTextAreaLog implements SanitizerWindow {
    private String title;
    private ProcessWorker processWorker;
    private ProgressBar progressBar = new ProgressBar(0);
    private ProgressIndicator progressIndicator = new ProgressIndicator(0);
    private Button cancelProcessButton = new Button();

    @Override
    public void setMessages() {
        title = "Processing files...";
        cancelProcessButton.setText("Cancel");
    }

    @Override
    public void setButtonsActions(Stage stage) {
        cancelProcessButton.setOnAction(event -> {
            cancelProcessButton.setDisable(true);
            processWorker.cancel(true);
            progressBar.progressProperty().unbind();
            progressIndicator.progressProperty().unbind();
            //
            progressBar.setProgress(0);
            progressIndicator.setProgress(0);
        });
    }

    @Override
    public void show() {
        setMessages();
        BorderPane root = new BorderPane();
        FlowPane bottomPane = new FlowPane();

        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.getChildren().add(cancelProcessButton);
        bottomPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(SanitizerWindow.INSET)));
        root.setBottom(bottomPane);
        Stage stage = new Stage();
        setButtonsActions(stage);
        stage.setOnCloseRequest(event -> getLog().info("stop project files process"));
        setButtonsActions(stage);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));

        stage.setScene(new Scene(root, SanitizerWindow.PROCESS_W, SanitizerWindow.PROCESS_H));
        stage.setTitle(title);
        stage.show();
    }

}
