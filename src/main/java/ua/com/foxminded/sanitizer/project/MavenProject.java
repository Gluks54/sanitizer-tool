package ua.com.foxminded.sanitizer.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ua.com.foxminded.sanitizer.ui.ISanitizerWindow;

public class MavenProject extends AbstractProject {
    public MavenProject(File dir) {
        super(dir);
    }

    private boolean isValidPomXml(File mavenPomFile) {
        try {
            if (mavenPomFile != null) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory
                        .newSchema(new StreamSource(getClass().getResourceAsStream("/schema/maven-4.0.0.xsd")));
                // проверяем через JAXB
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(mavenPomFile));
                getLog().info("validate " + mavenPomFile + " " + ISanitizerWindow.Status.OK.getStatus());
                return true;
            }
        } catch (SAXException e) {
            getLog().severe("SAX error in " + mavenPomFile);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            getLog().severe("IO error " + mavenPomFile);
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isProperProject() {
        File mavenPomFile = new File(getDir().getAbsoluteFile() + "/pom.xml");
        boolean hasPomXml = mavenPomFile.exists();
        getLog().info(hasPomXml ? mavenPomFile + " " + ISanitizerWindow.Status.OK.getStatus()
                : mavenPomFile + " " + ISanitizerWindow.Status.FAIL.getStatus());
        boolean isProperPomXml = isValidPomXml(mavenPomFile);
        File srcFolder = new File(getDir().getAbsoluteFile() + "/src");
        boolean hasSrcFolder = srcFolder.exists() && (srcFolder.isDirectory());

        boolean resultOK = getDir().isDirectory() && hasSrcFolder && hasPomXml && isProperPomXml;
        if (resultOK) {
            getLog().info(hasSrcFolder ? "src folder: " + srcFolder + " " + ISanitizerWindow.Status.OK.getStatus()
                    : "src folder: " + ISanitizerWindow.Status.FAIL.getStatus());
            getLog().info("+++ maven project found at " + getDir());
        }
        return resultOK;
    }

    @Override
    public ImageView getProjectLabelIcon() {
        return new ImageView(new Image(getClass().getResourceAsStream("/img/project/maven.png")));
    }

}
