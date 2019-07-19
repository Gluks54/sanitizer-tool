package ua.com.foxminded.sanitizer.worker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ua.com.foxminded.sanitizer.data.Config;
import ua.com.foxminded.sanitizer.ui.ISanitizerWindow;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

@RequiredArgsConstructor
public class UndoWorker extends SharedTextAreaLog {
    @NonNull
    private File baseFolder;
    @NonNull
    private Config config;
    private FileWorker fileWorker = new FileWorker();

    public Path getPatchFile() {
        try (Stream<Path> walk = Files.walk(Paths.get(fileWorker.getProperOriginalFolderName(baseFolder)))) {
            return walk.filter(p -> p.toString().endsWith(ISanitizerWindow.PATCH_EXT)).findFirst().get();
        } catch (IOException e) {
            getLog().severe("error during process " + baseFolder);
            e.printStackTrace();
        }
        return null;
    }
}
