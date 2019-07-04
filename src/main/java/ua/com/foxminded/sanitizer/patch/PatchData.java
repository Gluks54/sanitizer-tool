package ua.com.foxminded.sanitizer.patch;

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
    private Map<Integer, Delta> patches;
}
