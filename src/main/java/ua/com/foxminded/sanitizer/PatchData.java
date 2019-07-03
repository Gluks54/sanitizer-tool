package ua.com.foxminded.sanitizer;

import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.github.difflib.patch.Patch;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@XmlRootElement(name = "diff-data")
public class PatchData {
    private String originalFileName;
    private String processedFileName;
    private int originalFileHashCode;
    private int processedFileHashCode;
    private Set<Map<Integer, Patch<String>>> patches;
}
