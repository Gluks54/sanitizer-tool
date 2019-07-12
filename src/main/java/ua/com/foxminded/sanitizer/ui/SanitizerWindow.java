package ua.com.foxminded.sanitizer.ui;

import javafx.stage.Stage;
import lombok.Getter;
import ua.com.foxminded.sanitizer.worker.OSWorker;

public interface SanitizerWindow {
    public enum Status {
        OK("ok"), FAIL("none");

        @Getter
        private final String status;

        private Status(String status) {
            this.status = status;
        }
    }

    public static final int INSET = 10;

    public static final OSWorker.OS ENV = new OSWorker().getOs();

    public void setMessages();

    public void setButtonsActions(Stage stage);

    public void show();
}
