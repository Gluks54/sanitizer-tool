package ua.com.foxminded.sanitizer.ui.elements;

import java.util.Arrays;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import ua.com.foxminded.sanitizer.ui.ISanitizerWindow;

@Getter
@Setter
public class FilesSelectorHBox extends HBox {
    @NonNull
    private List<CheckBox> extensions = Arrays.asList(new CheckBox(".java"), new CheckBox(".xml"), new CheckBox(".ts"));
    @NonNull
    private CheckBox filePatternCheckBox = new CheckBox();
    @NonNull
    private TextField filePatternTextField = new TextField();

    public FilesSelectorHBox() {
        setAlignment(Pos.BASELINE_CENTER);
        getChildren().add(new Label("Files pattern:"));
        extensions.forEach(extension -> extension.setSelected(
                (extension.getText().equalsIgnoreCase(".java")) || (extension.getText().equalsIgnoreCase(".xml"))));
        getChildren().addAll(extensions);

        HBox filePatternHBox = new HBox();
        filePatternHBox.setAlignment(Pos.BASELINE_CENTER);
        filePatternTextField.setEditable(false);
        filePatternCheckBox.setOnAction(event -> {
            if (filePatternCheckBox.isSelected()) {
                filePatternTextField.setText("");
                filePatternTextField.setEditable(true);
            } else {
                filePatternTextField.setText("custom pattern");
                filePatternTextField.setEditable(false);
            }
        });
        filePatternHBox.getChildren().addAll(filePatternCheckBox, filePatternTextField);
        getChildren().add(filePatternHBox);
        getChildren().forEach(
                node -> HBox.setMargin(node, new Insets(0, ISanitizerWindow.INSET, 0, ISanitizerWindow.INSET)));
    }
}
