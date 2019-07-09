package ua.com.foxminded.sanitizer.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "template")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class Template {
    private boolean removeComments;
    private List<String> patterns = new ArrayList<String>();
    @XmlElementWrapper
    @XmlElement
    private Map<String, Replacement> replacementInFileContent = new HashMap<String, Replacement>();
    private Map<String, Replacement> replacementInProjectStructure = new HashMap<String, Replacement>();
}
