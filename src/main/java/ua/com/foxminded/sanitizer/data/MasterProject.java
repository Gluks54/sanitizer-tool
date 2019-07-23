package ua.com.foxminded.sanitizer.data;

import java.io.File;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "master")
@XmlAccessorType(XmlAccessType.FIELD)
public class MasterProject {
    private File originalProjectFolder;
    private File configFile;
    private File outputPreparedFolder;
}
