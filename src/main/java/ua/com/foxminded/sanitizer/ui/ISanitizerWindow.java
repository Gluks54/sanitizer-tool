package ua.com.foxminded.sanitizer.ui;

import javafx.stage.Stage;
import lombok.Getter;
import ua.com.foxminded.sanitizer.worker.OSWorker;

public interface ISanitizerWindow {
    public enum Status {
        OK("ok"), FAIL("none");

        @Getter
        private final String status;

        private Status(String status) {
            this.status = status;
        }
    }

    public static final int INSET = 10;
    public static final int MAIN_W = 800;
    public static final int MAIN_H = 600;
    public static final int EXPLORE_W = 800;
    public static final int EXPLORE_H = 600;
    public static final int VIEWER_W = 800;
    public static final int VIEWER_H = 600;
    public static final int CONFIGEDITOR_W = 950;
    public static final int CONFIGEDITOR_H = 600;
    public static final int PROCESS_W = 320;
    public static final int PROCESS_H = 100;
    public static final OSWorker.OS ENV = new OSWorker().getOs();

    public void setMessages();

    public void setButtonsActions(Stage stage);

    public void show();
}
