package ua.com.foxminded.sanitizer.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Replacement {
    @NonNull
    private String source;
    @NonNull
    private String target;
    @XmlElementWrapper(name = "refactor-replace-filename-filters")
    @XmlElement(name = "refactor-replace-filter")
    private List<String> refactorReplaceFilenameFilters = new ArrayList<String>();
    @XmlElement(name = "refactor-replace-filename-filter-regexp")
    private String refactorReplaceFilenameFilterRegexp;
}
