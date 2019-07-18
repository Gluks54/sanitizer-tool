package ua.com.foxminded.sanitizer.patch;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@XmlRootElement(name = "diff-data")
public class Template {
    private long originalCRC32; // original file checksum
    private long modifiedCRC32; // modified file checksum
    private Map<Long, Delta> patches;
}
