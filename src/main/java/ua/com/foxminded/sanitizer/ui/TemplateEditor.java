package ua.com.foxminded.sanitizer.ui;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateEditor extends SharedTextAreaLog implements SanitizerWindow {
    private String title;

    @Override
    public void show() {
        setMessages();
        BorderPane root = new BorderPane();
        int mainW = 800;
        int mainH = 600;
        Stage stage = new Stage();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));
        stage.setScene(new Scene(root, mainW, mainH));
        stage.setTitle(title);
        stage.show();
    }

    @Override
    public void setMessages() {
        title = "New template";

    }

    @Override
    public void setButtonsActions(Stage stage) {
        // TODO Auto-generated method stub

    }

}
