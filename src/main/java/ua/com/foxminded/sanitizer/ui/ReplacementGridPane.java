package ua.com.foxminded.sanitizer.ui;

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

public class ReplacementGridPane extends TitledPane {
    @Getter
    @Setter
    public class ReplacementItem extends HBox {
        private Label sourceLabel = new Label();
        private Label targetLabel = new Label();
        private TextField sourceTextField = new TextField();
        private TextField targetTextField = new TextField();
        private Button deleteReplacementItemButton = new Button();
        private ReplacementGridPane replacementGridPane;

        public ReplacementItem(String sourceText, String targetText, ReplacementGridPane replacementGridPane) {
            super();
            setMessages();
            setButtonActions();
            this.replacementGridPane = replacementGridPane;
            this.sourceTextField.setText(sourceText);
            this.targetTextField.setText(targetText);
            this.getChildren().addAll(sourceLabel, sourceTextField, targetLabel, targetTextField,
                    deleteReplacementItemButton);
            this.setAlignment(Pos.BASELINE_CENTER);
            this.getChildren().forEach(node -> setMargin(node, new Insets(SanitizerWindow.INSET)));
        }

        private void setMessages() {
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
    private GridPane mainPane = new GridPane();
    private ScrollPane scrollPane = new ScrollPane();

    public ReplacementGridPane() {
        mainColumn.setPercentWidth(100);
        mainPane.getColumnConstraints().add(mainColumn);
        scrollPane.setContent(mainPane);
        setContent(scrollPane);
    }

    public void addReplacementItem() {
        ReplacementItem replacementItem = new ReplacementItem("", "", this);
        mainPane.add(replacementItem, 0, mainPane.getChildren().size());
    }

    public void removeReplacementItem(ReplacementItem replacementItem) {
        mainPane.getChildren().remove(replacementItem);
    }
}
