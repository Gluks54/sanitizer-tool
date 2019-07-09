package ua.com.foxminded.sanitizer.ui;

import javafx.stage.Stage;

public interface SanitizerWindow {
    public static final int INSET = 10;

    public void setMessages();

    public void setButtonsActions(Stage stage);

    public void show();
}
