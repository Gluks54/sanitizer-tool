package ua.com.foxminded.sanitizer.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ua.com.foxminded.sanitizer.data.Template;
import ua.com.foxminded.sanitizer.ui.elements.ReplacementPane;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;
import ua.com.foxminded.sanitizer.worker.TemplateWorker;

@RequiredArgsConstructor
@NoArgsConstructor
public class TemplateEditor extends SharedTextAreaLog implements SanitizerWindow {
    @Getter
    @NonNull
    private Template template;
    @NonNull
    private File file;
    private SanitizerWindow.Status operationStatus;
    private Button newTemplateButton = new Button();
    private Button saveTemplateButton = new Button();
    private Button cancelButton = new Button();
    private Button addContentReplacementButton = new Button();
    private Button addFileSystemReplacementButton = new Button();
    private ReplacementPane contentReplacementPane = new ReplacementPane();
    private ReplacementPane filesystemReplacementPane = new ReplacementPane();
    private CheckBox removeCommentsCheckBox = new CheckBox();
    private final List<CheckBox> extensions = Arrays.asList(new CheckBox(".java"), new CheckBox(".xml"),
            new CheckBox(".sql"), new CheckBox(".ts"));
    private final HBox filePatternHBox = new HBox();
    private final CheckBox filePatternCheckBox = new CheckBox();
    private final TextField filePatternTextField = new TextField();
    @Setter
    private MainAppWindow startWindow;

    @Override
    public void show() {
        setMessages();
        FlowPane extensionsPane = new FlowPane();
        extensionsPane.getChildren().add(new Label("Files pattern:"));
        extensions.forEach(extension -> extension.setSelected(
                (extension.getText().equalsIgnoreCase(".java")) || (extension.getText().equalsIgnoreCase(".xml"))));
        extensionsPane.getChildren().addAll(extensions);

        filePatternTextField.setEditable(false);
        filePatternHBox.setAlignment(Pos.BASELINE_CENTER);
        filePatternHBox.getChildren().addAll(filePatternCheckBox, filePatternTextField);
        extensionsPane.getChildren().add(filePatternHBox);
        extensionsPane.setAlignment(Pos.CENTER);
        extensionsPane.setId("topPane");
        extensionsPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(SanitizerWindow.INSET)));

        BorderPane centerPane = new BorderPane();
        FlowPane centerTopButtonsPane = new FlowPane();
        centerTopButtonsPane.getChildren().addAll(removeCommentsCheckBox, addContentReplacementButton,
                addFileSystemReplacementButton);
        centerTopButtonsPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(SanitizerWindow.INSET)));
        centerPane.setTop(centerTopButtonsPane);
        SplitPane splitCenterPane = new SplitPane();
        splitCenterPane.setOrientation(Orientation.VERTICAL);
        splitCenterPane.getItems().addAll(contentReplacementPane, filesystemReplacementPane);

        centerPane.setCenter(splitCenterPane);

        FlowPane bottomButtonsPane = new FlowPane();
        bottomButtonsPane.setAlignment(Pos.CENTER);
        bottomButtonsPane.setId("bottomPane");
        bottomButtonsPane.getChildren().addAll(newTemplateButton, saveTemplateButton, cancelButton);
        bottomButtonsPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(SanitizerWindow.INSET)));

        BorderPane root = new BorderPane();
        root.setTop(extensionsPane);
        root.setCenter(centerPane);
        root.setBottom(bottomButtonsPane);
        Stage stage = new Stage();
        setButtonsActions(stage);
        stage.setOnCloseRequest(event -> {
            getLog().info("cancel template");
        });

        int mainW = 950;
        int mainH = 600;
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));
        stage.setScene(new Scene(root, mainW, mainH));

        // если проинжекчен template, инитим данные в окне
        if (template != null && file != null) {
            loadTemplateData();
            stage.setTitle("Edit template " + file.getAbsolutePath());
            getLog().info("edit template " + file.getAbsolutePath());
        } else {
            stage.setTitle("New template file");
            getLog().info("new template file started");
        }
        stage.show();
    }

    public void loadTemplateData() {
        if (template.getPatterns() != null) {
            extensions.stream().forEach(extension -> {
                extension.setSelected(template.getPatterns().stream()
                        .anyMatch(template -> template.equalsIgnoreCase(extension.getText())) ? true : false);
            });
            operationStatus = SanitizerWindow.Status.OK;
        } else {
            operationStatus = SanitizerWindow.Status.FAIL;
        }
        getLog().info("...load file extensions: " + operationStatus.getStatus());

        if (template.getCustomPattern() != null) {
            filePatternCheckBox.setSelected(true);
            filePatternTextField.setEditable(true);
            filePatternTextField.setText(template.getCustomPattern());
            operationStatus = SanitizerWindow.Status.OK;
        } else {
            operationStatus = SanitizerWindow.Status.FAIL;
        }
        getLog().info("...load custom file pattern: " + operationStatus.getStatus());

        removeCommentsCheckBox.setSelected(template.isRemoveComments());
        getLog().info("...load remove comments feature: " + SanitizerWindow.Status.OK.getStatus());

        if (template.getReplacementInFileContent() != null
                && template.getReplacementInFileContent().entrySet().size() > 0) {
            template.getReplacementInFileContent().entrySet().stream()
                    .forEach(entry -> contentReplacementPane.addReplacementItem(
                            contentReplacementPane.new ReplacementItem(entry.getKey(), entry.getValue().getSource(),
                                    entry.getValue().getTarget(), contentReplacementPane)));
            operationStatus = SanitizerWindow.Status.OK;
        } else {
            operationStatus = SanitizerWindow.Status.FAIL;
        }
        getLog().info("...load per-file replacements: " + operationStatus.getStatus());

        if (template.getReplacementInProjectStructure() != null
                && template.getReplacementInProjectStructure().entrySet().size() > 0) {
            template.getReplacementInProjectStructure().entrySet().stream()
                    .forEach(entry -> filesystemReplacementPane.addReplacementItem(
                            filesystemReplacementPane.new ReplacementItem(entry.getKey(), entry.getValue().getSource(),
                                    entry.getValue().getTarget(), filesystemReplacementPane)));
            operationStatus = SanitizerWindow.Status.OK;
        } else {
            operationStatus = SanitizerWindow.Status.FAIL;
        }
        getLog().info("...load project structure replacements: " + operationStatus.getStatus());
    }

    public void clearTemplate() {
        removeCommentsCheckBox.setSelected(false);
        contentReplacementPane.clear();
        filesystemReplacementPane.clear();
        extensions.stream().forEach(extension -> extension.setSelected(
                (extension.getText().equalsIgnoreCase(".java")) || (extension.getText().equalsIgnoreCase(".xml"))));
        filePatternCheckBox.setSelected(false);
        filePatternTextField.setText("custom pattern");
    }

    @Override
    public void setMessages() {
        newTemplateButton.setText("New template");
        saveTemplateButton.setText("Save template");
        cancelButton.setText("Cancel");
        addContentReplacementButton.setText("Add per-file replacement");
        addFileSystemReplacementButton.setText("Add project structure replacement");
        removeCommentsCheckBox.setText("Remove comments");
        contentReplacementPane.setText("Per-file replacements");
        filesystemReplacementPane.setText("Project structure replacements");
        filePatternTextField.setText("custom pattern");
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
            getLog().info("start new template");
            clearTemplate();
        });
        saveTemplateButton.setOnAction(event -> {
            Alert alert = new Alert(AlertType.ERROR);
            if (contentReplacementPane.isWrongDescriptionInReplacementItems()
                    || filesystemReplacementPane.isWrongDescriptionInReplacementItems()) {
                alert.setTitle("Description error");
                alert.setContentText("Empty descriptions are prohibited");
                alert.showAndWait();
            } else if (contentReplacementPane.isWrongSourceInReplacementItems()
                    || filesystemReplacementPane.isWrongSourceInReplacementItems()) {
                alert.setTitle("Source error");
                alert.setContentText("Empty sources are prohibited");
                alert.showAndWait();
            } else if (contentReplacementPane.isWrongTargetInReplacementItems()
                    || filesystemReplacementPane.isWrongTargetInReplacementItems()) {
                alert.setTitle("Target error");
                alert.setContentText("Empty targets are prohibited");
                alert.showAndWait();
            } else if (contentReplacementPane.isDuplicateDescriptionsInReplacementItems()
                    || filesystemReplacementPane.isDuplicateDescriptionsInReplacementItems()) {
                alert.setTitle("Description error");
                alert.setContentText("Similar descriptions are prohibited");
                alert.showAndWait();
            } else {
                FileChooser fc = new FileChooser();
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml"));
                file = fc.showSaveDialog(stage);
                if (file != null) {
                    getLog().info("save current template to " + file.getAbsolutePath());
                    // считать все расширения файлов
                    List<String> patterns = new ArrayList<String>();
                    extensions.forEach(extension -> {
                        if (extension.isSelected()) {
                            patterns.add(extension.getText());
                        }
                    });
                    // читаем показатели из полей
                    // если template не проинжекчен конструктором,
                    // создаем новый
                    if (template == null) {
                        template = new Template();
                    }
                    template.setPatterns(patterns);
                    getLog().info("...save file extensions: " + SanitizerWindow.Status.OK.getStatus());

                    if (contentReplacementPane.getReplacementsMap() != null
                            && contentReplacementPane.getReplacementsMap().size() > 0) {
                        template.setReplacementInFileContent(contentReplacementPane.getReplacementsMap());
                        operationStatus = SanitizerWindow.Status.OK;
                    } else {
                        operationStatus = SanitizerWindow.Status.FAIL;
                    }
                    getLog().info("...save per-file replacements: " + operationStatus.getStatus());

                    template.setRemoveComments(removeCommentsCheckBox.isSelected());
                    getLog().info("...save remove comments feature: " + SanitizerWindow.Status.OK.getStatus());

                    // добавляем regexp из поля
                    if (filePatternCheckBox.isSelected() && (!filePatternTextField.getText().equals(""))
                            && (!filePatternTextField.getText().equals(null))) {
                        template.setCustomPattern(filePatternTextField.getText());
                        operationStatus = SanitizerWindow.Status.OK;
                    } else {
                        operationStatus = SanitizerWindow.Status.FAIL;
                    }
                    getLog().info("...save custom file regexp: " + operationStatus.getStatus());

                    if (filesystemReplacementPane.getReplacementsMap() != null
                            && filesystemReplacementPane.getReplacementsMap().size() > 0) {
                        template.setReplacementInProjectStructure(filesystemReplacementPane.getReplacementsMap());
                        operationStatus = SanitizerWindow.Status.OK;
                    } else {
                        operationStatus = SanitizerWindow.Status.FAIL;
                    }
                    getLog().info("...save project structure replacements: " + operationStatus.getStatus());

                    if (new TemplateWorker().writeTemplateData(file, template)) {
                        // записали, обновили статус и проверили кнопки снизу
                        startWindow.setTemplateFile(file);
                        startWindow.setProperTemplateFileSelected(true);
                        startWindow.getTemplateFileStatusLabel().setText(file.getAbsolutePath());
                        startWindow.getTemplateFileStatusLabel().setGraphic(
                                new ImageView(new Image(getClass().getResourceAsStream("/img/sign/ok.png"))));
                        startWindow.toggleBottomButtons();
                    } else {
                        startWindow.getTemplateFileStatusLabel().setText("cancel select");
                        startWindow.getTemplateFileStatusLabel().setGraphic(
                                new ImageView(new Image(getClass().getResourceAsStream("/img/sign/disable.png"))));
                    }
                } else {
                    getLog().info("cancel template save");
                }
                stage.close();
            }
        });
        cancelButton.setOnAction(event -> {
            getLog().info("cancel template save");
            stage.close();
        });
        addContentReplacementButton.setOnAction(event -> {
            contentReplacementPane.addReplacementItem(null);
        });
        addFileSystemReplacementButton.setOnAction(event -> {
            filesystemReplacementPane.addReplacementItem(null);
        });
    }
}
