package ua.com.foxminded.sanitizer.project;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ua.com.foxminded.sanitizer.ui.ISanitizerWindow;

public class AngularProject extends AbstractProject {

    public AngularProject(File dir) {
        super(dir);
    }

    private boolean isValidJson(File jsonFile) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        JsonFactory factory = mapper.getFactory();
        JsonParser parser;
        try {
            parser = factory.createParser(jsonFile);
            JsonNode jsonObj = mapper.readTree(parser);
            return jsonObj != null;
        } catch (IOException e) {
        }
        return false;
    }

    @Override
    public boolean isProperProject() {
        File angularJsonFile = new File(getDir().getAbsoluteFile() + "/angular.json");
        boolean hasAngularJsonFile = angularJsonFile.exists();
        getLog().info(hasAngularJsonFile ? angularJsonFile + " " + ISanitizerWindow.Status.OK.getStatus()
                : angularJsonFile + " " + ISanitizerWindow.Status.FAIL.getStatus());
        boolean isProperAngularJson = isValidJson(angularJsonFile);

        File srcFolder = new File(getDir().getAbsoluteFile() + "/src");
        File e2eFolder = new File(getDir().getAbsoluteFile() + "/e2e");
        File node_modulesFolder = new File(getDir().getAbsoluteFile() + "/node_modules");
        boolean hasSrcFolder = srcFolder.exists() && (srcFolder.isDirectory());
        boolean hasE2eFolder = e2eFolder.exists() && (e2eFolder.isDirectory());
        boolean hasNode_modulesFolder = node_modulesFolder.exists() && (node_modulesFolder.isDirectory());

        boolean resultOK = getDir().isDirectory() && hasSrcFolder && hasE2eFolder && hasNode_modulesFolder
                && hasAngularJsonFile && isProperAngularJson;
        if (resultOK) {
            getLog().info(hasSrcFolder ? "src folder: " + srcFolder + " " + ISanitizerWindow.Status.OK.getStatus()
                    : "src folder: " + ISanitizerWindow.Status.FAIL.getStatus());
            getLog().info("+++ angular project found at " + getDir());
        }
        return resultOK;

    }

    @Override
    public ImageView getProjectLabelIcon() {
        return new ImageView(new Image(getClass().getResourceAsStream("/img/project/angular.png")));
    }

}
