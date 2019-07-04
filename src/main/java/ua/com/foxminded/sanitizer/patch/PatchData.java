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
public class PatchData {
    private long original; // original file checksum
    private long modified; // modified file checksum
    private Map<Long, Delta> patches;
}
