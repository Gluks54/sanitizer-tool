package ua.com.foxminded.sanitizer.data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {
    @XmlElement(name = "original-project")
    private File originalProject;
    @XmlElement(name = "output-project")
    private File outputProject;
    @XmlElement(name = "remove-comment")
    private RemoveComment removeComment = new RemoveComment();
    @XmlElementWrapper(name = "refactor-replace-code")
    private Map<String, Replacement> replacementInFileContent = new HashMap<String, Replacement>();
    @XmlElementWrapper(name = "refactor-replace-structure")
    private Map<String, Replacement> replacementInProjectStructure = new HashMap<String, Replacement>();
}
