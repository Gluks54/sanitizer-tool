package ua.com.foxminded.sanitizer;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@XmlRootElement(name = "diff-data")
public class PatchData {
    private String originalFileName;
    private String processedFileName;
    private int originalFileHashCode;
    private int processedFileHashCode;
    private Map<Integer, Delta> patches;
}
