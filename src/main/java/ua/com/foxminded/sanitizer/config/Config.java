package ua.com.foxminded.sanitizer.config;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "config")
public class Config {
    private boolean removeComments;
    private boolean backupOriginal;
    private Map<String, String> replacement = new HashMap<String, String>();
}
