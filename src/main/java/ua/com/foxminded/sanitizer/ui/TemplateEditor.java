package ua.com.foxminded.sanitizer.ui;

import java.util.Arrays;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateEditor extends SharedTextAreaLog implements SanitizerWindow {
    private String title;
    private Button saveTemplateButton = new Button();
    private Button cancelButton = new Button();
    private final List<CheckBox> extensions = Arrays.asList(new CheckBox("*.java"), new CheckBox("*.xml"),
            new CheckBox("*.sql"), new CheckBox("*.ts"));

    @Override
    public void show() {
        getLog().info("edit template");
        setMessages();
        BorderPane root = new BorderPane();
        FlowPane topPane = new FlowPane();
        FlowPane bottomPane = new FlowPane();

        topPane.getChildren().add(new Label("Files pattern:"));
        extensions.forEach(extension -> {
            if ((extension.getText().equalsIgnoreCase("*.java")) || (extension.getText().equalsIgnoreCase("*.xml"))) {
                extension.setSelected(true);
            }
            extension.setOnAction(event -> System.out.println("check"));
            topPane.getChildren().add(extension);
        });
        topPane.setAlignment(Pos.CENTER);
        topPane.setId("topPane");
        topPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(10)));

        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setId("bottomPane");
        bottomPane.getChildren().addAll(saveTemplateButton, cancelButton);
        bottomPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(10)));

        root.setTop(topPane);
        root.setBottom(bottomPane);
        Stage stage = new Stage();
        setButtonsActions(stage);
        stage.setOnCloseRequest(event -> {
            getLog().info("cancel template");
        });

        int mainW = 800;
        int mainH = 600;
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));
        stage.setScene(new Scene(root, mainW, mainH));
        stage.setTitle(title);
        stage.show();
    }

    @Override
    public void setMessages() {
        title = "New template";
        saveTemplateButton.setText("Save template");
        cancelButton.setText("Cancel");
    }

    @Override
    public void setButtonsActions(Stage stage) {
        saveTemplateButton.setOnAction(event -> {
            getLog().info("save current template");
            stage.close();
        });
        cancelButton.setOnAction(event -> {
            getLog().info("cancel template");
            stage.close();
        });
    }

}
