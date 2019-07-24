package ua.com.foxminded.sanitizer.worker;

import java.io.File;

import javafx.application.Application.Parameters;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ua.com.foxminded.sanitizer.ISanitizerEnvironment;

@RequiredArgsConstructor
public class CommandLineWorker implements ISanitizerEnvironment {
    @NonNull
    private Parameters parameters;

    public File getMasterProjectFile() {
        return new File(parameters.getRaw().stream().filter(p -> p.endsWith(MASTER_EXT)).findFirst().get());
    }
}
