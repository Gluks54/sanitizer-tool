package ua.com.foxminded.sanitizer.worker;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
import ua.com.foxminded.sanitizer.ISanitizerEnvironment;
import ua.com.foxminded.sanitizer.data.Config;
import ua.com.foxminded.sanitizer.data.ProjectFileMask;
import ua.com.foxminded.sanitizer.data.RefactorReplacement;
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
                    Path fileInStripFolder = outputFolder.resolve(originalFolder.relativize(fileInOriginalFolder));
                    Path copyOfFileInStripFolder = Paths.get(fileInStripFolder.toString() + ORIGINAL_EXT);
                    Path patchInOriginalFolder = Paths.get(fileInOriginalFolder.toString() + PATCH_EXT);
                    boolean isOverwrite = false;
                    String originalCode = null;
                    String modifiedCode = null;
                    Files.copy(fileInOriginalFolder, fileInStripFolder, StandardCopyOption.REPLACE_EXISTING);

                    projectFileMask = config.getRemoveComment().getFileMask(); // вначале - нужно ли в файле удалять коменты
                    if (config.getRemoveComment().isToRemove()
                            && projectFileMask.isMatchFilePatterns(fileInOriginalFolder.toFile())) {

                        Files.copy(fileInStripFolder, copyOfFileInStripFolder, StandardCopyOption.REPLACE_EXISTING);
                        fileWorker.setOriginalFile(copyOfFileInStripFolder); // копия оригинала
                        fileWorker.setModifiedFile(fileInStripFolder); // обработанный файл
                        fileWorker.setPatchFile(patchInOriginalFolder); // патч в оригинальной папке

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

                    // проверяем на замены внутри кода по файловым маскам
                    if (config.getReplacementInFileContent() != null
                            && config.getReplacementInFileContent().size() > 0) {
                        for (Map.Entry<String, RefactorReplacement> entry : config.getReplacementInFileContent()
                                .entrySet()) {
                            projectFileMask = entry.getValue().getFileMask();
                            if (projectFileMask.isMatchFilePatterns(fileInOriginalFolder.toFile())) {

                                System.out.println(fileInStripFolder + " " + entry.getKey() + " " + entry.getValue()
                                        + " " + projectFileMask);

                            }
                        }
                    }

                    if (isOverwrite) {
                        //logFeature.getLog().info("Process " + " " + fileInStripFolder);
                        // System.out.println("Process " + " " + fileInStripFolder);
                        // перезаписываем исходный файл с изменениями
                        fileWorker.codeStringToFile(modifiedCode, fileInStripFolder);
                        // записываем или перезаписываем патч
                        fileWorker.updateTotalPatch("remove comments: " + fileWorker.getCurrentDateTimeString(),
                                originalCode, modifiedCode);
                        // удаляем оригинальный файл проекта, вместо него модиф и патч
                        Files.delete(copyOfFileInStripFolder);
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
