package ua.com.foxminded.sanitizer.ui;

import java.util.logging.Logger;

import javafx.scene.control.TextArea;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SharedTextAreaLog {
    private Logger log = Logger.getLogger(SharedTextAreaLog.class.getName());
    private TextAreaHandler textAreaHandler = new TextAreaHandler();
    private TextArea logTextArea = new TextArea();

    public SharedTextAreaLog() {
        logTextArea.setEditable(false);
        textAreaHandler.setTextArea(logTextArea);
    }
}
