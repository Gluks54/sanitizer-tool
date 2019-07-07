package ua.com.foxminded.sanitizer.ui;

import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javafx.scene.control.TextArea;

public class TextAreaHandler extends StreamHandler {
    TextArea textArea = null;

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        flush();

        if (textArea != null) {
            textArea.appendText(getFormatter().format(record));
        }
    }
}
