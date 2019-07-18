package ua.com.foxminded.sanitizer.ui.elements;

import java.util.logging.Logger;

import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SharedTextAreaLog {
    private Logger log = Logger.getLogger(SharedTextAreaLog.class.getName());
    private TextAreaHandler textAreaHandler = new TextAreaHandler();
    private TextArea logTextArea = new TextArea();
    private BorderPane root = new BorderPane();

    public SharedTextAreaLog() {
        log.setUseParentHandlers(false);
        logTextArea.setEditable(false);
        textAreaHandler.setTextArea(logTextArea);
    }
}
