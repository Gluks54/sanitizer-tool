package ua.com.foxminded.sanitizer.data;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor
@RequiredArgsConstructor
@XmlRootElement(name = "repl")
public class Replacement {
    @NonNull
    private String source;
    @NonNull
    private String target;
}
