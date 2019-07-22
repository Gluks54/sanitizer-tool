package ua.com.foxminded.sanitizer.project;

import java.io.File;

import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

@RequiredArgsConstructor
public abstract class AbstractProject extends SharedTextAreaLog {
    @NonNull
    @Getter
    private File dir;

    public abstract boolean isProperProject();

    public abstract ImageView getProjectLabelIcon();
}
