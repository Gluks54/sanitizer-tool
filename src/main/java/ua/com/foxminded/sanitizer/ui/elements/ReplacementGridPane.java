package ua.com.foxminded.sanitizer.ui.elements;

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
import lombok.Setter;
import ua.com.foxminded.sanitizer.ui.SanitizerWindow;

public class ReplacementGridPane extends TitledPane {
    @Getter
    @Setter
    public class ReplacementItem extends HBox {
        private Label descriptionLabel = new Label();
        private Label sourceLabel = new Label();
        private Label targetLabel = new Label();
        private TextField descriptionTextField = new TextField();
        private TextField sourceTextField = new TextField();
        private TextField targetTextField = new TextField();
        private Button deleteReplacementItemButton = new Button();
        private ReplacementGridPane replacementGridPane;

        public ReplacementItem(String description, String source, String target,
                ReplacementGridPane replacementGridPane) {
            super();
            setMessages();
            setButtonActions();
            this.replacementGridPane = replacementGridPane;
            this.descriptionTextField.setText(description);
            this.sourceTextField.setText(source);
            this.targetTextField.setText(target);
            this.getChildren().addAll(descriptionLabel, descriptionTextField, sourceLabel, sourceTextField, targetLabel,
                    targetTextField, deleteReplacementItemButton);
            this.setAlignment(Pos.BASELINE_CENTER);
            this.getChildren().forEach(node -> setMargin(node, new Insets(SanitizerWindow.INSET)));
        }

        private void setMessages() {
            descriptionLabel.setText("Description: ");
            sourceLabel.setText("Source: ");
            targetLabel.setText("Target: ");
            deleteReplacementItemButton.setText("Delete");
        }

        private void setButtonActions() {
            deleteReplacementItemButton.setOnAction(event -> {
                replacementGridPane.removeReplacementItem(this);
            });
        }
    }

    private ColumnConstraints mainColumn = new ColumnConstraints();
    @Getter
    private GridPane mainPane = new GridPane();
    private ScrollPane scrollPane = new ScrollPane();

    public ReplacementGridPane() {
        mainColumn.setPercentWidth(100);
        mainPane.getColumnConstraints().add(mainColumn);
        scrollPane.setContent(mainPane);
        setContent(scrollPane);
    }

    public void clear() {
        mainPane.getChildren().clear();
    }

    public boolean isWrongDescriptionInReplacementItems() {
        return mainPane.getChildren().stream()
                .anyMatch(node -> ((ReplacementItem) node).getDescriptionTextField().getText().equals("")
                        || ((ReplacementItem) node).getDescriptionTextField().getText().equals(null));
    }

    public boolean isDuplicateDescriptionsInReplacementItems() {
        return mainPane.getChildren().stream().map(node -> ((ReplacementItem) node).getDescriptionTextField().getText())
                .collect(Collectors.toSet()).size() < mainPane.getChildren().size();
    }

    public void addReplacementItem() {
        ReplacementItem replacementItem = new ReplacementItem("", "", "", this);
        mainPane.add(replacementItem, 0, mainPane.getChildren().size());
    }

    public void removeReplacementItem(ReplacementItem replacementItem) {
        mainPane.getChildren().remove(replacementItem);
    }
}
