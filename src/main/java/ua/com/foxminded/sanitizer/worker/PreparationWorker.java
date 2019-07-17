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
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

@RequiredArgsConstructor
public class PreparationWorker extends Task<List<Path>> {
    private class LogFeature extends SharedTextAreaLog {
    }

    @NonNull
    private File originalFolder;
    @NonNull
    private File outputFolder;
    @NonNull
    private LogFeature logFeature = new LogFeature();

    @Override
    protected List<Path> call() throws Exception {
        try (Stream<Path> walk = Files.walk(Paths.get(originalFolder.toURI()))) {
            List<Path> paths = walk.collect(Collectors.toList());
            int filesQuantity = paths.size();
            int filesCounter = 0;
            String snapshotTimeString = new FileWorker().getCurrentDateTimeString();
            logFeature.getLog().info("### current time snapshot: " + snapshotTimeString);

            for (Path path : paths) {
                String basePathString = outputFolder.toPath()
                        .resolve(originalFolder.getParentFile().toPath().relativize(path)).toString();
                String projectName = Paths.get(outputFolder.toString(), originalFolder.getName()).toString();
                String projectNameWithSnapshot = projectName + "-v" + snapshotTimeString + "-";

                Path origPath = Paths.get(basePathString.replaceFirst(projectName, projectNameWithSnapshot + "orig"));
                Path stripPath = Paths.get(basePathString.replaceFirst(projectName, projectNameWithSnapshot + "strip"));

                if (path.toFile().isDirectory()) {
                    try {
                        Files.createDirectory(origPath);
                        Files.createDirectory(stripPath);
                    } catch (FileAlreadyExistsException e) {
                        // пропускаем
                    }
                } else {
                    Files.copy(path, origPath, StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(path, stripPath, StandardCopyOption.REPLACE_EXISTING);
                }
                filesCounter++;
                this.updateProgress(filesCounter, filesQuantity);
                this.updateMessage("prepare: " + filesCounter + "/" + filesQuantity + " files");
            }
            return paths;
        } catch (IOException e) {
            e.printStackTrace();
            logFeature.getLog().severe("!!! error during file strip process");
            return null;
        }
    }
}
