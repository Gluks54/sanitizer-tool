package ua.com.foxminded.sanitizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

/**
 * Hello world!
 *
 */
public class Main {
    public static void main(String[] args) throws DiffException, IOException {
        String originalFileName = "/mnt/500GB/BACKUP/SANITIZER/ShapeGlobal.java";
        String processedFileName = "/mnt/500GB/BACKUP/SANITIZER/ShapeGlobal_comments2.java";
        String configFileName = "/mnt/500GB/BACKUP/SANITIZER/patch.json";

        File originalFile = new File(originalFileName);
        File processedFile = new File(processedFileName);
        File configFile = new File(configFileName);

        // build simple lists of the lines of the two testfiles
        List<String> original = Files.readAllLines(originalFile.toPath());
        List<String> revised = Files.readAllLines(processedFile.toPath());

        // compute the patch: this is the diffutils part
        Patch<String> diff = DiffUtils.diff(original, revised);
        // System.out.println(diff.toString());
        // simple output the computed patch to console

        // simple output the computed patch to console
        /*
         * for (AbstractDelta<String> delta : patch.getDeltas()) {
         * System.out.println(delta); }
         */

        List<AbstractDelta<String>> delta = diff.getDeltas();

        // delta.forEach(System.out::println);

        PatchData patchData = new PatchData();
        patchData.setOriginalFileName(originalFileName);
        patchData.setProcessedFileName(processedFileName);
        patchData.setOriginalFileHashCode(originalFile.hashCode());
        patchData.setProcessedFileHashCode(processedFile.hashCode());

        List<Map<Integer, Patch<String>>> patches = new ArrayList<Map<Integer, Patch<String>>>();
        Map<Integer, Patch<String>> patch = new HashMap<Integer, Patch<String>>();

        patch.put(diff.hashCode(), diff);
        patches.add(patch);
        patchData.setPatches(patches);

        new JSONPatchWorker().writePatch(configFile, patchData);
    }
}
