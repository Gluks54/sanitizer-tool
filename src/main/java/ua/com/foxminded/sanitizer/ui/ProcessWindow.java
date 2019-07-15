package ua.com.foxminded.sanitizer.ui;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

public class ProcessWindow extends SharedTextAreaLog implements SanitizerWindow {
    private String title;

    @Override
    public void setMessages() {
        title = "Processing files...";

    }

    @Override
    public void setButtonsActions(Stage stage) {
        // TODO Auto-generated method stub

    }

    @Override
    public void show() {
        BorderPane root = new BorderPane();

        setMessages();
        Stage stage = new Stage();
        stage.setOnCloseRequest(event -> getLog().info("stop project files process"));
        setButtonsActions(stage);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));

        stage.setScene(new Scene(root, SanitizerWindow.PROCESS_W, SanitizerWindow.PROCESS_H));
        stage.setTitle(title);
        stage.show();
    }

}
