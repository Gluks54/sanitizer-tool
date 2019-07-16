package ua.com.foxminded.sanitizer.worker;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.concurrent.Task;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ua.com.foxminded.sanitizer.data.Config;
import ua.com.foxminded.sanitizer.data.Replacement;
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
    private FileWorker fileWorker;

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
                        // пропускаем
                    }
                } else {
                    Path modifiedOriginalProjectFile = outputFolder.toPath()
                            .resolve(originalFolder.toPath().getParent().relativize(path));

                    Files.copy(path, modifiedOriginalProjectFile, StandardCopyOption.REPLACE_EXISTING);
                    Path copyOriginalProjectFile = Paths.get(modifiedOriginalProjectFile.toString() + ".original");
                    // бэкапим оригинальный файл
                    Files.copy(modifiedOriginalProjectFile, copyOriginalProjectFile,
                            StandardCopyOption.REPLACE_EXISTING);
                    Path patchForOriginalProjectFile = Paths.get(modifiedOriginalProjectFile.toString() + ".patch.xml");

                    fileWorker = new FileWorker(copyOriginalProjectFile.toString(),
                            modifiedOriginalProjectFile.toString(), patchForOriginalProjectFile.toString());

                    // наш файл или нет
                    if (fileWorker.isMatchFilePatterns(modifiedOriginalProjectFile.toFile(), config)) {
                        // читаем в строку и фиксим табы
                        String originalCode = fileWorker
                                .fixTabsInCodeString(fileWorker.fileToCodeString(modifiedOriginalProjectFile));
                        String modifiedCode = originalCode;

                        // замены в файле в соотв с конфигом
                        if (config.getReplacementInFileContent() != null) {
                            for (Map.Entry<String, Replacement> entry : config.getReplacementInFileContent()
                                    .entrySet()) {

                                modifiedCode = fileWorker.replaceInCodeString(modifiedCode,
                                        entry.getValue().getSource(), entry.getValue().getTarget());

                                // перезаписываем исходный файл с изменениями
                                fileWorker.codeStringToFile(modifiedCode, modifiedOriginalProjectFile);
                                // записываем или перезаписываем патч
                                fileWorker.updatePatchData();
                            }
                        }
                    }
                }
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
