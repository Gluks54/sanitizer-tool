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

public class ReplacementPane extends TitledPane {
    // строки, добавляемые в панель
    public class ReplacementItem extends HBox {
        private Label descriptionLabel = new Label();
        private Label sourceLabel = new Label();
        private Label targetLabel = new Label();
        private TextField descriptionTextField = new TextField();
        private TextField sourceTextField = new TextField();
        private TextField targetTextField = new TextField();
        private Button deleteReplacementItemButton = new Button();
        private ReplacementPane replacementPane;

        public ReplacementItem(String description, String source, String target, ReplacementPane replacementPane) {
            super();
            setMessages();
            setButtonActions();
            this.replacementPane = replacementPane;
            descriptionTextField.setText(description);
            sourceTextField.setText(source);
            targetTextField.setText(target);
            getChildren().addAll(descriptionLabel, descriptionTextField, sourceLabel, sourceTextField, targetLabel,
                    targetTextField, deleteReplacementItemButton);
            setAlignment(Pos.BASELINE_CENTER);
            getChildren().forEach(node -> setMargin(node, new Insets(ISanitizerWindow.INSET)));
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

    private ColumnConstraints mainColumn = new ColumnConstraints();
    @Getter
    private GridPane mainPane = new GridPane();
    private ScrollPane scrollPane = new ScrollPane();

    public ReplacementPane() {
        mainColumn.setPercentWidth(100);
        mainPane.getColumnConstraints().add(mainColumn);
        scrollPane.setContent(mainPane);
        setContent(scrollPane);
    }

    public Map<String, Replacement> getReplacementsMap() {
        Map<String, Replacement> result = new HashMap<String, Replacement>();
        mainPane.getChildren().stream().forEach(node -> {
            ReplacementItem item = (ReplacementItem) node;
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
                .anyMatch(node -> ((ReplacementItem) node).sourceTextField.getText().equals("")
                        || ((ReplacementItem) node).sourceTextField.getText().equals(null));
    }

    public boolean isWrongTargetInReplacementItems() {
        return mainPane.getChildren().stream()
                .anyMatch(node -> ((ReplacementItem) node).targetTextField.getText().equals("")
                        || ((ReplacementItem) node).targetTextField.getText().equals(null));
    }

    public boolean isWrongDescriptionInReplacementItems() {
        return mainPane.getChildren().stream()
                .anyMatch(node -> ((ReplacementItem) node).descriptionTextField.getText().equals("")
                        || ((ReplacementItem) node).descriptionTextField.getText().equals(null));
    }

    public boolean isDuplicateDescriptionsInReplacementItems() {
        return mainPane.getChildren().stream().map(node -> ((ReplacementItem) node).descriptionTextField.getText())
                .collect(Collectors.toSet()).size() < mainPane.getChildren().size();
    }

    public void addReplacementItem(ReplacementItem replacementItem) {
        mainPane.add((replacementItem == null) ? new ReplacementItem("", "", "", this) : replacementItem, 0,
                mainPane.getChildren().size());
    }

    public void removeReplacementItem(ReplacementItem replacementItem) {
        mainPane.getChildren().remove(replacementItem);
    }
}
