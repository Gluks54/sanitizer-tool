package ua.com.foxminded.sanitizer.ui.elements;

import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javafx.scene.control.TextArea;

public class TextAreaHandler extends StreamHandler {
    TextArea textArea = null;

    public TextAreaHandler() {
        this.setFormatter(new SimpleFormatter() {
            private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

            @Override
            public synchronized String format(LogRecord logRecord) {
                return String.format(format, new Date(logRecord.getMillis()), logRecord.getLevel().getLocalizedName(),
                        logRecord.getMessage());
            }
        });
    }

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
