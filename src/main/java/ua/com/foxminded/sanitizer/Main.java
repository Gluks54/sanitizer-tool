package ua.com.foxminded.sanitizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;

public class Main {
    public static void main(String[] args) throws DiffException, IOException {
        String originalFileName = "/mnt/500GB/BACKUP/SANITIZER/ShapeGlobal.java";
        String processedFileName = "/mnt/500GB/BACKUP/SANITIZER/ShapeGlobal_comments2.java";
        String configFileName = "/mnt/500GB/BACKUP/SANITIZER/patch.json";

        File originalFile = new File(originalFileName);
        File processedFile = new File(processedFileName);
        File configFile = new File(configFileName);

        List<String> original = Files.readAllLines(originalFile.toPath());
        List<String> revised = Files.readAllLines(processedFile.toPath());

        Patch<String> diff = DiffUtils.diff(original, revised);

        PatchData patchData = new PatchData();
        patchData.setOriginalFileName(originalFileName);
        patchData.setProcessedFileName(processedFileName);
        patchData.setOriginalFileHashCode(originalFile.hashCode());
        patchData.setProcessedFileHashCode(processedFile.hashCode());

        Map<Integer, Delta> patches = new LinkedHashMap<Integer, Delta>();

        patches.put(diff.hashCode(), new Delta(diff));
        patchData.setPatches(patches);
        System.out.println(patchData);

        new JSONPatchWorker().writePatchData(configFile, patchData);
        // System.out.println(new JSONPatchWorker().readPatchData(configFile,
        // PatchData.class));
    }
}
