package ua.com.foxminded.sanitizer.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class Replacement {
    @NonNull
    private String source;
    @NonNull
    private String target;
}
