package ua.com.foxminded.sanitizer.ui.elements;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import lombok.Getter;
import ua.com.foxminded.sanitizer.data.Replacement;
import ua.com.foxminded.sanitizer.ui.ISanitizerWindow;

public class RefactorReplacePane extends TitledPane {
    // строки, добавляемые в панель
    public class RefactorReplaceItem extends GridPane {
        private HBox refactorReplaceHBox = new HBox();
        private FilesSelectorHBox filesSelectorBox = new FilesSelectorHBox();
        private Label descriptionLabel = new Label();
        private Label sourceLabel = new Label();
        private Label targetLabel = new Label();
        private TextField descriptionTextField = new TextField();
        private TextField sourceTextField = new TextField();
        private TextField targetTextField = new TextField();
        private Button deleteReplacementItemButton = new Button();
        private RefactorReplacePane replacementPane;

        public RefactorReplaceItem(String description, String source, String target,
                RefactorReplacePane replacementPane) {
            super();
            setMessages();
            setButtonActions();
            setId("topPane");

            this.replacementPane = replacementPane;
            descriptionTextField.setText(description);
            sourceTextField.setText(source);
            targetTextField.setText(target);

            refactorReplaceHBox.getChildren().addAll(descriptionLabel, descriptionTextField, sourceLabel,
                    sourceTextField, targetLabel, targetTextField, deleteReplacementItemButton);
            refactorReplaceHBox.setAlignment(Pos.BASELINE_CENTER);
            refactorReplaceHBox.getChildren().forEach(
                    node -> HBox.setMargin(node, new Insets(0, ISanitizerWindow.INSET, 0, ISanitizerWindow.INSET)));
            filesSelectorBox.setAlignment(Pos.BASELINE_LEFT);
            add(refactorReplaceHBox, 0, 0);
            add(filesSelectorBox, 0, 1);
            getChildren().forEach(node -> GridPane.setMargin(node,
                    new Insets(ISanitizerWindow.INSET / 2, 0, ISanitizerWindow.INSET / 2, 0)));
        }

        private void setMessages() {
            descriptionLabel.setText("Description: ");
            sourceLabel.setText("Source: ");
            targetLabel.setText("Target: ");
            deleteReplacementItemButton.setText("Delete");
        }

        private void setButtonActions() {
            deleteReplacementItemButton.setOnAction(event -> replacementPane.removeReplacementItem(this));
        }
    }

    @Getter
    private GridPane mainPane = new GridPane();

    public RefactorReplacePane() {
        ColumnConstraints mainColumn = new ColumnConstraints();
        mainColumn.setPercentWidth(100);
        mainPane.getColumnConstraints().add(mainColumn);

        ScrollPane scrollPane = new ScrollPane(mainPane);
        // scrollPane.setContent(mainPane);
        setContent(scrollPane);
    }

    public Map<String, Replacement> getReplacementsMap() {
        Map<String, Replacement> result = new HashMap<String, Replacement>();
        mainPane.getChildren().stream().forEach(node -> {
            RefactorReplaceItem item = (RefactorReplaceItem) node;
            Replacement replacement = new Replacement(item.sourceTextField.getText(), item.targetTextField.getText());
            result.put(item.descriptionTextField.getText(), replacement);
        });
        return result;
    }

    public void clear() {
        mainPane.getChildren().clear();
    }

    public boolean isWrongSourceInReplacementItems() {
        return mainPane.getChildren().stream()
                .anyMatch(node -> ((RefactorReplaceItem) node).sourceTextField.getText().equals("")
                        || ((RefactorReplaceItem) node).sourceTextField.getText().equals(null));
    }

    public boolean isWrongTargetInReplacementItems() {
        return mainPane.getChildren().stream()
                .anyMatch(node -> ((RefactorReplaceItem) node).targetTextField.getText().equals("")
                        || ((RefactorReplaceItem) node).targetTextField.getText().equals(null));
    }

    public boolean isWrongDescriptionInReplacementItems() {
        return mainPane.getChildren().stream()
                .anyMatch(node -> ((RefactorReplaceItem) node).descriptionTextField.getText().equals("")
                        || ((RefactorReplaceItem) node).descriptionTextField.getText().equals(null));
    }

    public boolean isDuplicateDescriptionsInReplacementItems() {
        return mainPane.getChildren().stream().map(node -> ((RefactorReplaceItem) node).descriptionTextField.getText())
                .collect(Collectors.toSet()).size() < mainPane.getChildren().size();
    }

    public void addReplacementItem(RefactorReplaceItem replacementItem) {
        mainPane.add((replacementItem == null) ? new RefactorReplaceItem("", "", "", this) : replacementItem, 0,
                mainPane.getChildren().size());
    }

    public void removeReplacementItem(RefactorReplaceItem replacementItem) {
        mainPane.getChildren().remove(replacementItem);
    }
}
