package ua.com.foxminded.sanitizer.project;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import javafx.scene.control.Label;
import ua.com.foxminded.sanitizer.ui.ISanitizerWindow;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

public class MavenProject implements IProject {
    private SharedTextAreaLog logFeature = new SharedTextAreaLog() {
    };

    private boolean validatePomXml(File mavenPomFile) {
        try {
            if (mavenPomFile != null) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory
                        .newSchema(new StreamSource(getClass().getResourceAsStream("/xsd/maven-4.0.0.xsd")));
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(mavenPomFile));
                logFeature.getLog().info("validate " + mavenPomFile + " " + ISanitizerWindow.Status.OK.getStatus());
                return true;
            }
        } catch (SAXException e) {
            logFeature.getLog().severe("SAX error in " + mavenPomFile);
            e.printStackTrace();
        } catch (IOException e) {
            logFeature.getLog().severe("IO error " + mavenPomFile);
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isProperProject(File dir) {
        File mavenPomFile = new File(dir.getAbsoluteFile() + "/pom.xml");
        boolean hasPomXml = mavenPomFile.exists();
        logFeature.getLog().info(hasPomXml ? mavenPomFile + " " + ISanitizerWindow.Status.OK.getStatus()
                : mavenPomFile + " " + ISanitizerWindow.Status.FAIL.getStatus());
        boolean isProperPomXml = validatePomXml(mavenPomFile);

        File srcFolder = new File(dir.getAbsoluteFile() + "/src");
        boolean hasSrcFolder = srcFolder.exists() && (!srcFolder.isFile());
        logFeature.getLog()
                .info(hasSrcFolder ? "src folder: " + srcFolder + " " + ISanitizerWindow.Status.OK.getStatus()
                        : "src folder: " + ISanitizerWindow.Status.FAIL.getStatus());

        return dir.isDirectory() && hasSrcFolder && hasPomXml && isProperPomXml;
    }

    @Override
    public Label getProjectLabel() {
        // TODO Auto-generated method stub
        return null;
    }

}
