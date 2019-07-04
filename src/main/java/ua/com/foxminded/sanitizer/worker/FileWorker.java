package ua.com.foxminded.sanitizer.worker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ua.com.foxminded.sanitizer.patch.Delta;
import ua.com.foxminded.sanitizer.patch.PatchData;
import ua.com.foxminded.sanitizer.patch.SanitizerFilePatch;

@Getter
@Setter
@RequiredArgsConstructor
public class FileWorker {
    @NonNull
    private String originalFilename;
    @NonNull
    private String modifiedFilename;
    @NonNull
    private String patchFilename;

    private long getCheckSum(File file) {
        long result = 0;

        try (CheckedInputStream check = new CheckedInputStream(new FileInputStream(file), new CRC32());
                BufferedInputStream in = new BufferedInputStream(check)) {
            while (in.read() != -1) {
            }
            result = check.getChecksum().getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public PatchData getPatchDataFromDiff() { // текущие изменения по сравнению с оригиналом
        File originalFile = new File(originalFilename);
        File modifiedFile = new File(modifiedFilename);
        List<String> original;
        List<String> revised;
        Patch<String> diff;

        try {
            original = Files.readAllLines(originalFile.toPath());
            revised = Files.readAllLines(modifiedFile.toPath());
            diff = DiffUtils.diff(original, revised);
        } catch (IOException | DiffException e) {
            e.printStackTrace();
            return null;
        }
        PatchData patchData = new PatchData(); // весь патч со всеми изменениями за все время
        Map<Long, Delta> patches = new LinkedHashMap<Long, Delta>(); // мапа всех отдельных дельт (сеансов)
        Delta delta = new Delta(); // каждая дельта - список изменений в файле до сохранения

        diff.getDeltas().stream().forEach(d -> { // маппим дифф файла на свой класс
            SanitizerFilePatch sfp = new SanitizerFilePatch(); // для marshal-unmarshal
            sfp.setType(d.getType());
            sfp.getSource().setLines(d.getSource().getLines());
            sfp.getSource().setPosition(d.getSource().getPosition());
            sfp.getTarget().setLines(d.getTarget().getLines());
            sfp.getTarget().setPosition(d.getTarget().getPosition());
            delta.getDelta().add(sfp);
        });

        patches.put(getCheckSum(modifiedFile), delta); // в
        patchData.setOriginal(getCheckSum(originalFile));
        patchData.setModified(getCheckSum(modifiedFile));
        patchData.setPatches(patches);
        return patchData;
    }

    public void updatePatchData() { // берем предыдущие изменения и добавляем текущий snapshot
        File originalFile = new File(originalFilename);
        File modifiedFile = new File(modifiedFilename);

        PatchData currentPatchData = getPatchDataFromDiff();
        // если есть изменения
        if (currentPatchData != null && (currentPatchData.getModified() != getCheckSum(modifiedFile))) {
        }
    }
}
