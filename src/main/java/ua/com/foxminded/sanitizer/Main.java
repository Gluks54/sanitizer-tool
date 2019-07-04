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

import ua.com.foxminded.sanitizer.patch.Delta;
import ua.com.foxminded.sanitizer.patch.PatchData;
import ua.com.foxminded.sanitizer.patch.SanitizerFilePatch;
import ua.com.foxminded.sanitizer.patchworker.XMLPatchWorker;

public class Main {
    public static void main(String[] args) throws DiffException, IOException {
        String originalFileName = "/mnt/500GB/BACKUP/SANITIZER/ShapeGlobal.java";
        String processedFileName = "/mnt/500GB/BACKUP/SANITIZER/ShapeGlobal_comments2.java";
        String configFileName = "/mnt/500GB/BACKUP/SANITIZER/patch.xml";

        File originalFile = new File(originalFileName);
        File processedFile = new File(processedFileName);
        File configFile = new File(configFileName);

        List<String> original = Files.readAllLines(originalFile.toPath());
        List<String> revised = Files.readAllLines(processedFile.toPath());
        Patch<String> diff = DiffUtils.diff(original, revised);

        Delta delta = new Delta();
        Map<Integer, Delta> patches = new LinkedHashMap<Integer, Delta>();
        PatchData patchData = new PatchData();

        diff.getDeltas().stream().forEach(d -> { // маппим дифф файла на свой класс
            SanitizerFilePatch sfp = new SanitizerFilePatch();
            sfp.setType(d.getType());
            sfp.getSource().setLines(d.getSource().getLines());
            sfp.getSource().setPosition(d.getSource().getPosition());
            sfp.getTarget().setLines(d.getTarget().getLines());
            sfp.getTarget().setPosition(d.getTarget().getPosition());
            delta.getDelta().add(sfp);
        });

        patches.put(diff.hashCode(), delta);
        patchData.setPatches(patches);
        System.out.println(patchData);

        new XMLPatchWorker().writePatchData(configFile, patchData);
        // System.out.println(new XMLPatchWorker().readPatchData(configFile,
        // PatchData.class));
    }
}
