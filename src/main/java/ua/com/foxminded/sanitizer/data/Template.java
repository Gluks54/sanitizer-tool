package ua.com.foxminded.sanitizer.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "config")
public class Template {
    private boolean removeComments;
    private boolean backupOriginal;
    private List<String> extensions = new ArrayList<String>();
    private Map<String, String> replacement = new HashMap<String, String>();
}
