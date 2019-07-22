package ua.com.foxminded.sanitizer.project;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ua.com.foxminded.sanitizer.ui.ISanitizerWindow;

public class AngularProject extends AbstractProject {

    public AngularProject(File dir) {
        super(dir);
    }

    @Override
    public boolean isProperProject() {
        File mavenPomFile = new File(getDir().getAbsoluteFile() + "/pom.xml");
        boolean hasPomXml = mavenPomFile.exists();
        getLog().info(hasPomXml ? mavenPomFile + " " + ISanitizerWindow.Status.OK.getStatus()
                : mavenPomFile + " " + ISanitizerWindow.Status.FAIL.getStatus());

        File srcFolder = new File(getDir().getAbsoluteFile() + "/src");
        boolean hasSrcFolder = srcFolder.exists() && (!srcFolder.isFile());
        getLog().info(hasSrcFolder ? "src folder: " + srcFolder + " " + ISanitizerWindow.Status.OK.getStatus()
                : "src folder: " + ISanitizerWindow.Status.FAIL.getStatus());
        getLog().info("+++ maven project found at " + getDir());

        return getDir().isDirectory() && hasSrcFolder;
    }

    @Override
    public ImageView getProjectLabelIcon() {
        return new ImageView(new Image(getClass().getResourceAsStream("/img/project/angular.png")));
    }

}
