package ua.com.foxminded.sanitizer.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import ua.com.foxminded.sanitizer.data.Template;
import ua.com.foxminded.sanitizer.worker.TemplateWorker;

public class TemplateEditor extends SharedTextAreaLog implements SanitizerWindow {
    @Getter
    @Setter
    private Template template = new Template();
    private Button newTemplateButton = new Button();
    private Button saveTemplateButton = new Button();
    private Button cancelButton = new Button();
    private final List<CheckBox> extensions = Arrays.asList(new CheckBox("*.java"), new CheckBox("*.xml"),
            new CheckBox("*.sql"), new CheckBox("*.ts"));
    private final HBox filePatternHBox = new HBox();
    private final CheckBox filePatternCheckBox = new CheckBox();
    private final TextField filePatternTextField = new TextField("custom pattern");
    private File file;

    public TemplateEditor() {
    }

    public TemplateEditor(File file) {
        super();
        this.file = file;
        template = new TemplateWorker().readTemplateData(file, Template.class);
    }

    @Override
    public void show() {
        if (template == null) {
            Alert alert = new Alert(AlertType.WARNING, file.getName() + " not a template. Run new template?",
                    ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
        } else {
            // читаем данные и показываем окно
        }

        setMessages();
        BorderPane root = new BorderPane();
        FlowPane topPane = new FlowPane();
        FlowPane bottomPane = new FlowPane();

        topPane.getChildren().add(new Label("Files pattern:"));
        extensions.forEach(extension -> {
            if ((extension.getText().equalsIgnoreCase("*.java")) || (extension.getText().equalsIgnoreCase("*.xml"))) {
                extension.setSelected(true);
            } else {
                extension.setSelected(false);
            }
            extension.setOnAction(event -> System.out.println("check"));
            topPane.getChildren().add(extension);
        });
        filePatternHBox.setAlignment(Pos.BASELINE_CENTER);
        filePatternTextField.setEditable(false);
        filePatternHBox.getChildren().addAll(filePatternCheckBox, filePatternTextField);
        topPane.getChildren().add(filePatternHBox);
        topPane.setAlignment(Pos.CENTER);
        topPane.setId("topPane");
        topPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(10)));

        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setId("bottomPane");
        bottomPane.getChildren().addAll(newTemplateButton, saveTemplateButton, cancelButton);
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
        if (file != null) {
            stage.setTitle("Edit template " + file.getAbsolutePath());
            getLog().info("edit template " + file.getAbsolutePath());
        } else {
            stage.setTitle("New template file");
            getLog().info("new template file started");
        }
        stage.show();
    }

    @Override
    public void setMessages() {
        newTemplateButton.setText("New template");
        saveTemplateButton.setText("Save template");
        cancelButton.setText("Cancel");
    }

    @Override
    public void setButtonsActions(Stage stage) {
        filePatternCheckBox.setOnAction(event -> {
            if (filePatternCheckBox.isSelected()) {
                filePatternTextField.setText("");
                filePatternTextField.setEditable(true);
            } else {
                filePatternTextField.setText("custom pattern");
                filePatternTextField.setEditable(false);
            }
        });
        newTemplateButton.setOnAction(event -> {
            // все обнулить
            getLog().info("start new template");
        });
        saveTemplateButton.setOnAction(event -> {
            FileChooser fc = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
            fc.getExtensionFilters().add(extFilter);
            file = fc.showSaveDialog(stage);
            if (file != null) {
                getLog().info("save current template to " + file.getAbsolutePath());
                // все считать
                List<String> patterns = new ArrayList<String>();
                extensions.forEach(extension -> {
                    if (extension.isSelected()) {
                        patterns.add(extension.getText());
                    }
                });
                template.setPatterns(patterns);
                new TemplateWorker().writeTemplateData(file, template);
            } else {
                getLog().info("cancel template save");
            }
            stage.close();
        });
        cancelButton.setOnAction(event -> {
            getLog().info("cancel template");
            stage.close();
        });
    }
}
