package ua.com.foxminded.sanitizer.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Replacement {
    @NonNull
    private String source;
    @NonNull
    private String target;
}
