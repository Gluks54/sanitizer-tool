package ua.com.foxminded.sanitizer.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "template")
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {
    private File originalProject;
    private File outputProject;
    private boolean removeComments;
    private List<String> patterns = new ArrayList<String>();
    private String customPattern;
    private Map<String, Replacement> replacementInFileContent = new HashMap<String, Replacement>();
    private Map<String, Replacement> replacementInProjectStructure = new HashMap<String, Replacement>();
}
