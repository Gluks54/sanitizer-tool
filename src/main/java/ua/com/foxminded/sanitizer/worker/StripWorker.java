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
import ua.com.foxminded.sanitizer.ui.ISanitizerWindow;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

@RequiredArgsConstructor
public class StripWorker extends Task<List<Path>> {
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
            int filesCounter = 0;

            for (Path path : result) {
                if (path.toFile().isDirectory()) {
                    try {
                        Path targetDir = outputFolder.toPath().resolve(originalFolder.toPath().relativize(path));
                        Files.createDirectory(targetDir);
                    } catch (FileAlreadyExistsException e) {
                        // пропускаем
                    }
                } else {
                    Path modifiedOriginalProjectFile = outputFolder.toPath()
                            .resolve(originalFolder.toPath().relativize(path));

                    Files.copy(path, modifiedOriginalProjectFile, StandardCopyOption.REPLACE_EXISTING);
                    Path copyOriginalProjectFile = Paths
                            .get(modifiedOriginalProjectFile.toString() + ISanitizerWindow.ORIGINAL_EXT);
                    Path patchForOriginalProjectFile = Paths
                            .get(modifiedOriginalProjectFile.toString().replaceAll(ISanitizerWindow.STRIP_SUFFIX,
                                    ISanitizerWindow.ORIG_SUFFIX) + ISanitizerWindow.PATCH_EXT);

                    fileWorker = new FileWorker(copyOriginalProjectFile.toString(),
                            modifiedOriginalProjectFile.toString(), patchForOriginalProjectFile.toString());
                    // наш файл или нет
                    if (fileWorker.isMatchFilePatterns(modifiedOriginalProjectFile.toFile(), config)) {
                        // бэкапим оригинальный файл
                        Files.copy(modifiedOriginalProjectFile, copyOriginalProjectFile,
                                StandardCopyOption.REPLACE_EXISTING);
                        // читаем в строку и фиксим табы
                        String originalCode = fileWorker
                                .fixTabsInCodeString(fileWorker.fileToCodeString(modifiedOriginalProjectFile));
                        String modifiedCode = originalCode;

                        // убираем коменты
                        if (config.isRemoveComments()) {
                            if (modifiedOriginalProjectFile.toString().endsWith(".java")) {
                                modifiedCode = fileWorker.removeCommentsFromJava(modifiedCode);
                            } else if (modifiedOriginalProjectFile.toString().endsWith(".xml")) {
                                modifiedCode = fileWorker.removeCommentsFromXml(modifiedCode);
                            }
                            // перезаписываем исходный файл с изменениями
                            fileWorker.codeStringToFile(modifiedCode, modifiedOriginalProjectFile);
                            // записываем или перезаписываем патч
                            fileWorker.updateTotalPatch("remove comments: " + fileWorker.getCurrentDateTimeString());
                        }

                        // замены в файле в соотв с конфигом
                        if (config.getReplacementInFileContent() != null) {
                            for (Map.Entry<String, Replacement> entry : config.getReplacementInFileContent()
                                    .entrySet()) {
                                modifiedCode = fileWorker.replaceInCodeString(modifiedCode,
                                        entry.getValue().getSource(), entry.getValue().getTarget());
                                // перезаписываем исходный файл с изменениями
                                fileWorker.codeStringToFile(modifiedCode, modifiedOriginalProjectFile);
                                // записываем или перезаписываем патч
                                fileWorker.updateTotalPatch(
                                        entry.getKey() + ": " + fileWorker.getCurrentDateTimeString());
                            }
                        }
                        logFeature.getLog().info("Process " + modifiedOriginalProjectFile + " "
                                + ISanitizerWindow.Status.OK.getStatus());
                        // удаляем оригинальный файл проекта, вместо него модиф и патч
                        Files.delete(copyOriginalProjectFile);
                    }
                }
                filesCounter++;
                this.updateProgress(filesCounter, filesQuantity);
                this.updateMessage("strip: " + filesCounter + "/" + filesQuantity + " files");
            }
            return result;
        } catch (

        IOException e) {
            e.printStackTrace();
            logFeature.getLog().severe("!!! error during file strip process");
            return null;
        }
    }
}
