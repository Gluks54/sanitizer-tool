package ua.com.foxminded.sanitizer.worker;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.concurrent.Task;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ua.com.foxminded.sanitizer.data.Config;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

@RequiredArgsConstructor
public class ProcessWorker extends Task<List<Path>> {
    private class LogFeature extends SharedTextAreaLog {
    }

    @NonNull
    private File originalFolder;
    @NonNull
    private File outputFolder;
    @NonNull
    private Config config;
    private LogFeature logFeature = new LogFeature();
    private FileWorker fileWorker = new FileWorker();

    @Override
    protected List<Path> call() throws Exception {
        try (Stream<Path> walk = Files.walk(Paths.get(originalFolder.toURI()))) {
            List<Path> result = walk.collect(Collectors.toList());
            int filesQuantity = result.size();
            int i = 0;

            for (Path path : result) {
                if (path.toFile().isDirectory()) {
                    try {
                        Path targetDir = outputFolder.toPath()
                                .resolve(originalFolder.toPath().getParent().relativize(path));
                        Files.createDirectory(targetDir);
                    } catch (FileAlreadyExistsException e) {
                        // its ok, do nothing
                    }
                } else {
                    Path targetFile = outputFolder.toPath()
                            .resolve(originalFolder.toPath().getParent().relativize(path));
                    Files.copy(path, targetFile, StandardCopyOption.REPLACE_EXISTING);

                    if (fileWorker.isMatchFilePatterns(targetFile.toFile(), config)) {
                        if (fileWorker.hasTabs(targetFile.toFile())) {
                            System.out.println(fileWorker.hasTabs(targetFile.toFile()) + " " + targetFile.toFile());
                        }
                    }
                }
                // logFeature.getLog().info("process file " + path);
                i++;
                this.updateProgress(i, filesQuantity);
                this.updateMessage("process: " + i + "/" + filesQuantity + " files");
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            logFeature.getLog().severe("!!! error during file process");
            return null;
        }
    }
}
