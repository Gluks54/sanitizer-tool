package ua.com.foxminded.sanitizer.ui;

import java.io.File;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ua.com.foxminded.sanitizer.ISanitizerEnvironment;
import ua.com.foxminded.sanitizer.data.Config;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;
import ua.com.foxminded.sanitizer.worker.UndoWorker;

@RequiredArgsConstructor
public class UndoSelectWindow extends SharedTextAreaLog implements ISanitizerWindow, ISanitizerEnvironment {
    @NonNull
    private File baseFolder;
    @NonNull
    private Config config;
    private String title;
    private Button closeButton = new Button();
    private UndoWorker undoWorker;
    private Status operationStatus;

    public HBox getExtensionsHBox() {
        HBox extensionsBox = new HBox();
        extensionsBox.getChildren().add(new Label("files pattern: "));

        if (config.getPatterns() != null) {
            for (String pattern : config.getPatterns()) {
                CheckBox extensionCheckBox = new CheckBox(pattern);
                extensionCheckBox.setSelected(true);
                extensionCheckBox.setDisable(true);
                extensionsBox.getChildren().add(extensionCheckBox);
            }
        }
        operationStatus = config.getPatterns() != null ? Status.OK : Status.FAIL;
        getLog().info("...load file extensions: " + operationStatus.getStatus());
        if (config.getCustomPattern() != null) {
            CheckBox customPatternCheckBox = new CheckBox(config.getCustomPattern());
            customPatternCheckBox.setSelected(true);
            customPatternCheckBox.setDisable(true);
            extensionsBox.getChildren().add(customPatternCheckBox);
        }
        operationStatus = config.getCustomPattern() != null && (!config.getCustomPattern().equalsIgnoreCase(""))
                ? Status.OK
                : Status.FAIL;
        getLog().info("...load custom file pattern: " + operationStatus.getStatus());
        extensionsBox.getChildren().forEach(node -> HBox.setMargin(node, new Insets(ISanitizerWindow.INSET)));
        return extensionsBox;
    }

    public HBox getCommentsRemoverHBox() {
        HBox commentsRemoverBox = new HBox();
        commentsRemoverBox.getChildren().add(new Label("remove comments: "));
        CheckBox commentsRemoverCheckBox = new CheckBox();
        commentsRemoverCheckBox.setSelected(config.isRemoveComments());
        commentsRemoverCheckBox.setDisable(true);
        commentsRemoverBox.getChildren().add(commentsRemoverCheckBox);
        commentsRemoverBox.getChildren().forEach(node -> HBox.setMargin(node, new Insets(ISanitizerWindow.INSET)));
        operationStatus = config.isRemoveComments() ? Status.OK : Status.FAIL;
        getLog().info("...load remove comments feature: " + operationStatus.getStatus());
        return commentsRemoverBox;
    }

    public List<HBox> getReplacementInFileContentBoxes() {
        return null;
    }

    /*
     * public void loadConfigData() { if (config.getPatterns() != null) {
     * extensions.stream().forEach(extension -> { extension.setSelected(
     * config.getPatterns().stream().anyMatch(config ->
     * config.equalsIgnoreCase(extension.getText())) ? true : false); });
     * operationStatus = ISanitizerWindow.Status.OK; } else { operationStatus =
     * ISanitizerWindow.Status.FAIL; } getLog().info("...load file extensions: " +
     * operationStatus.getStatus());
     * 
     * if (config.getCustomPattern() != null) {
     * filePatternCheckBox.setSelected(true);
     * filePatternTextField.setEditable(true);
     * filePatternTextField.setText(config.getCustomPattern()); operationStatus =
     * ISanitizerWindow.Status.OK; } else { operationStatus =
     * ISanitizerWindow.Status.FAIL; } getLog().info("...load custom file pattern: "
     * + operationStatus.getStatus());
     * 
     * removeCommentsCheckBox.setSelected(config.isRemoveComments());
     * getLog().info("...load remove comments feature: " +
     * ISanitizerWindow.Status.OK.getStatus());
     * 
     * if (config.getReplacementInFileContent() != null &&
     * config.getReplacementInFileContent().entrySet().size() > 0) {
     * config.getReplacementInFileContent().entrySet().stream() .forEach(entry ->
     * contentReplacementPane.addReplacementItem( contentReplacementPane.new
     * ReplacementItem(entry.getKey(), entry.getValue().getSource(),
     * entry.getValue().getTarget(), contentReplacementPane))); operationStatus =
     * ISanitizerWindow.Status.OK; } else { operationStatus =
     * ISanitizerWindow.Status.FAIL; }
     * getLog().info("...load per-file replacements: " +
     * operationStatus.getStatus());
     * 
     * if (config.getReplacementInProjectStructure() != null &&
     * config.getReplacementInProjectStructure().entrySet().size() > 0) {
     * config.getReplacementInProjectStructure().entrySet().stream() .forEach(entry
     * -> filesystemReplacementPane.addReplacementItem(
     * filesystemReplacementPane.new ReplacementItem(entry.getKey(),
     * entry.getValue().getSource(), entry.getValue().getTarget(),
     * filesystemReplacementPane))); operationStatus = ISanitizerWindow.Status.OK; }
     * else { operationStatus = ISanitizerWindow.Status.FAIL; }
     * getLog().info("...load project structure replacements: " +
     * operationStatus.getStatus()); }
     */
    @Override
    public void setMessages() {
        title = "Select and apply undo for whole project";
        closeButton.setText("Close");
    }

    @Override
    public void setButtonsActions(Stage stage) {
        closeButton.setOnAction(event -> {
            stage.close();
            getLog().info("quit undo");
        });
    }

    @Override
    public void show() {
        setMessages();
        undoWorker = new UndoWorker(baseFolder, config);
        ColumnConstraints mainColumn = new ColumnConstraints();
        GridPane mainPane = new GridPane();
        ScrollPane scrollPane = new ScrollPane(mainPane);
        FlowPane bottomPane = new FlowPane();

        mainColumn.setPercentWidth(100);
        mainPane.getColumnConstraints().add(mainColumn);
        mainPane.add(getExtensionsHBox(), 0, 0);
        mainPane.add(getCommentsRemoverHBox(), 0, 1);

        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.getChildren().add(closeButton);
        bottomPane.getChildren().forEach(node -> FlowPane.setMargin(node, new Insets(INSET)));
        getRoot().setCenter(scrollPane);
        getRoot().setBottom(bottomPane);
        Stage stage = new Stage();
        stage.setOnCloseRequest(event -> getLog().info("undo stopped"));
        setButtonsActions(stage);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/code.png")));

        stage.setScene(new Scene(getRoot(), UNDO_W, UNDO_H));
        stage.setTitle(title);
        stage.show();
    }
}
