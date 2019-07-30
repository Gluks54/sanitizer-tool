package ua.com.foxminded.sanitizer.worker;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.concurrent.Task;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ua.com.foxminded.sanitizer.ISanitizerEnvironment;
import ua.com.foxminded.sanitizer.data.Config;
import ua.com.foxminded.sanitizer.data.ProjectFileMask;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

@RequiredArgsConstructor
public class StripWorker extends Task<List<Path>> implements ISanitizerEnvironment {
    @NonNull
    private Path originalFolder;
    @NonNull
    private Path outputFolder;
    @NonNull
    private Config config;

    private class LogFeature extends SharedTextAreaLog {
    }

    private LogFeature logFeature = new LogFeature();

    @Override
    protected List<Path> call() throws Exception {
        try (Stream<Path> walk = Files.walk(originalFolder)) {
            List<Path> paths = walk.collect(Collectors.toList());
            int filesQuantity = paths.size();
            int filesCounter = 0;
            FileWorker fileWorker = new FileWorker();
            ProjectFileMask projectFileMask = new ProjectFileMask();

            for (Path fileInOriginalFolder : paths) {
                if (Files.isDirectory(fileInOriginalFolder, LinkOption.NOFOLLOW_LINKS)) {
                    try {
                        Files.createDirectory(outputFolder.resolve(originalFolder.relativize(fileInOriginalFolder)));
                    } catch (FileAlreadyExistsException e) {
                        // пропускаем
                    }
                } else { // пофайловый перебор
                    String originalCode = null;
                    String modifiedCode = null;

                    Path fileInStripFolder = null;
                    Path copyOfFileInStripFolder = null;
                    Path patchInOriginalFolder = null;
                    boolean isOverwrite = false;

                    projectFileMask = config.getRemoveComment().getFileMask(); // вначале - нужно ли в файле удалять коменты
                    if (config.getRemoveComment().isToRemove()
                            && projectFileMask.isMatchFilePatterns(fileInOriginalFolder.toFile())) {

                        fileInStripFolder = outputFolder.resolve(originalFolder.relativize(fileInOriginalFolder));
                        copyOfFileInStripFolder = Paths.get(fileInStripFolder.toString() + ORIGINAL_EXT);
                        patchInOriginalFolder = Paths.get(fileInStripFolder.toString() + PATCH_EXT);

                        Files.copy(fileInOriginalFolder, fileInStripFolder, StandardCopyOption.REPLACE_EXISTING);
                        Files.copy(fileInStripFolder, copyOfFileInStripFolder, StandardCopyOption.REPLACE_EXISTING);

                        fileWorker.setOriginalFile(copyOfFileInStripFolder);
                        fileWorker.setModifiedFile(fileInStripFolder);
                        fileWorker.setPatchFile(patchInOriginalFolder);

                        originalCode = fileWorker // исправляем табы
                                .fixTabsInCodeString(fileWorker.fileToCodeString(fileInStripFolder));
                        modifiedCode = originalCode;

                        // вырезание коментов
                        if (fileInStripFolder.toString().toLowerCase().endsWith(".java")) {
                            modifiedCode = fileWorker.removeCommentsFromJava(modifiedCode);
                        } else if (fileInStripFolder.toString().toLowerCase().endsWith(".xml")) {
                            modifiedCode = fileWorker.removeCommentsFromXml(modifiedCode);
                        }
                        isOverwrite = true;
                    }

                    if (isOverwrite) {
                        //logFeature.getLog().info("Process " + " " + fileInStripFolder);
                        System.out.println("Process " + " " + fileInStripFolder);
                        // перезаписываем исходный файл с изменениями
                        fileWorker.codeStringToFile(modifiedCode, fileInStripFolder);
                        // записываем или перезаписываем патч
                        fileWorker.updateTotalPatch("remove comments: " + fileWorker.getCurrentDateTimeString());
                        // удаляем оригинальный файл проекта, вместо него модиф и патч
                        Files.delete(copyOfFileInStripFolder);
                    }

                    // наш файл или нет
                    if (true) {
                        // if (fileWorker.isMatchFilePatterns(modifiedOriginalProjectFile.toFile(),
                        // config)) {
                        // бэкапим оригинальный файл

                        /*
                        Files.copy(modifiedOriginalProjectFile, copyOriginalProjectFile,
                                StandardCopyOption.REPLACE_EXISTING);
                        // читаем в строку и фиксим табы
                        String originalCode = fileWorker
                                .fixTabsInCodeString(fileWorker.fileToCodeString(modifiedOriginalProjectFile));
                        String modifiedCode = originalCode;
                        */

                        /*
                        // убираем коменты
                        if (config.getRemoveComment().isToRemove()) {
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
                            for (Map.Entry<String, RefactorReplacement> entry : config.getReplacementInFileContent()
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
                        */
                    }
                }
                filesCounter++;
                this.updateProgress(filesCounter, filesQuantity);
                this.updateMessage("strip: " + filesCounter + "/" + filesQuantity + " files");
            }
            return paths;
        } catch (

        IOException e) {
            e.printStackTrace();
            logFeature.getLog().severe("!!! error during file strip process");
            return null;
        }
    }
}
