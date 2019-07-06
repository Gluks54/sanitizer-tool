package ua.com.foxminded.sanitizer.ui;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class StartWindow {
    public void show(Stage stage) {
        BorderPane root = new BorderPane();
        int mainW = 1024;
        int mainH = 600;
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));
        stage.setScene(new Scene(root, mainW, mainH));
        stage.setTitle("title");
        stage.show();
    }
}
