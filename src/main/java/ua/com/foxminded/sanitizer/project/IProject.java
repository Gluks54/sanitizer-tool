package ua.com.foxminded.sanitizer.project;

import java.io.File;

import javafx.scene.control.Label;

public interface IProject {
    public boolean isProperProject(File dir);

    public Label getProjectLabel();
}
