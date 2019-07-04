package ua.com.foxminded.sanitizer.patch;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Delta {
    private List<SanitizerFilePatch> delta = new ArrayList<SanitizerFilePatch>();
}
