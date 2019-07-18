package ua.com.foxminded.sanitizer.ui;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

public class UndoSelectWindow extends SharedTextAreaLog implements ISanitizerWindow {
    private String title;

    @Override
    public void setMessages() {
        title = "Select and apply undo stage";

    }

    @Override
    public void setButtonsActions(Stage stage) {
        // TODO Auto-generated method stub

    }

    @Override
    public void show() {
        setMessages();
        Stage stage = new Stage();
        stage.setOnCloseRequest(event -> getLog().info("undo bye!"));
        setButtonsActions(stage);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));

        stage.setScene(new Scene(getRoot(), ISanitizerWindow.UNDO_W, ISanitizerWindow.UNDO_H));
        stage.setTitle(title);
        stage.show();
    }

}
