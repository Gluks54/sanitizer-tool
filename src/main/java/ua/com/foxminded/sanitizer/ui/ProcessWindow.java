package ua.com.foxminded.sanitizer.ui;

import java.io.File;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ua.com.foxminded.sanitizer.data.Config;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;
import ua.com.foxminded.sanitizer.worker.ProcessWorker;

@RequiredArgsConstructor
public class ProcessWindow extends SharedTextAreaLog implements SanitizerWindow {
    @NonNull
    private File originalFolder;
    @NonNull
    private File outputFolder;
    @NonNull
    private Config config;
    private String title;
    private ProcessWorker processWorker;
    private ProgressBar progressBar = new ProgressBar(0);
    private ProgressIndicator progressIndicator = new ProgressIndicator(0);
    private Button cancelProcessButton = new Button();
    private Button startProcessButton = new Button();
    private Button closeProcessButton = new Button();

    @Override
    public void setMessages() {
        title = "Processing files...";
        cancelProcessButton.setText("Cancel");
        startProcessButton.setText("Start");
        closeProcessButton.setText("Close");
    }

    @Override
    public void setButtonsActions(Stage stage) {
        closeProcessButton.setOnAction(event -> {
            stage.close();
        });
        startProcessButton.setOnAction(event -> {
            startProcessButton.setDisable(true);
            closeProcessButton.setDisable(true);
            cancelProcessButton.setDisable(false);
            progressBar.setProgress(0);
            progressIndicator.setProgress(0);

            processWorker = new ProcessWorker(originalFolder, outputFolder, config);
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(processWorker.progressProperty());
            progressIndicator.progressProperty().unbind();
            progressIndicator.progressProperty().bind(processWorker.progressProperty());

            processWorker.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                    new EventHandler<WorkerStateEvent>() {

                        @Override
                        public void handle(WorkerStateEvent t) {
                            closeProcessButton.setDisable(false);
                            cancelProcessButton.setDisable(true);
                            // List<Path> proceedFiles = processWorker.getValue();
                            getLog().info("*** complete file process");
                        }
                    });
            getLog().info("*** start file process");
            new Thread(processWorker).start();
        });
        cancelProcessButton.setOnAction(event -> {
            startProcessButton.setDisable(false);
            closeProcessButton.setDisable(false);
            cancelProcessButton.setDisable(true);
            processWorker.cancel(true);
            getLog().info("!!! cancel file process");
            progressIndicator.progressProperty().unbind();
            progressBar.progressProperty().unbind();
            progressIndicator.setProgress(0);
        });
    }

    @Override
    public void show() {
        setMessages();
        BorderPane root = new BorderPane();
        FlowPane topPane = new FlowPane();
        FlowPane bottomPane = new FlowPane();
        FlowPane centerPane = new FlowPane();

        topPane.getChildren().add(progressIndicator);
        topPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(SanitizerWindow.INSET)));
        topPane.setAlignment(Pos.CENTER);
        root.setTop(topPane);

        progressBar.setMinWidth(0.85 * SanitizerWindow.PROCESS_W);
        centerPane.setAlignment(Pos.BASELINE_CENTER);
        centerPane.getChildren().add(progressBar);
        centerPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(SanitizerWindow.INSET)));
        root.setCenter(centerPane);

        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.getChildren().addAll(startProcessButton, cancelProcessButton, closeProcessButton);
        bottomPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(SanitizerWindow.INSET)));
        root.setBottom(bottomPane);

        Stage stage = new Stage();
        setButtonsActions(stage);
        stage.setOnCloseRequest(event -> {
            getLog().info("stop project files process");
            if (processWorker.isRunning()) {
                processWorker.cancel(true);
            }
            stage.close();
        });
        setButtonsActions(stage);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));

        stage.setScene(new Scene(root, SanitizerWindow.PROCESS_W, SanitizerWindow.PROCESS_H));
        stage.setTitle(title);
        stage.show();
    }

}
