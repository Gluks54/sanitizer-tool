package ua.com.foxminded.sanitizer.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoveComment {
    @XmlAttribute(name = "remove")
    private boolean isToRemove;
    private String contain;
    @XmlElement(name = "remove-comment-filename-filter-regexp")
    private String removeCommentFilenameFilterRegexp;
    @XmlElementWrapper(name = "remove-comment-filename-filters")
    @XmlElement(name = "remove-comment-filter")
    private List<String> removeCommentFilenameFilters = new ArrayList<String>();
}
