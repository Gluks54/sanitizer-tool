package ua.com.foxminded.sanitizer;

import java.io.IOException;

import com.github.difflib.algorithm.DiffException;

import ua.com.foxminded.sanitizer.patch.PatchData;
import ua.com.foxminded.sanitizer.worker.FileWorker;

public class Main {
    public static void main(String[] args) throws DiffException, IOException {
        String originalFileName = "/mnt/500GB/BACKUP/SANITIZER/ShapeGlobal.java";
        String processedFileName = "/mnt/500GB/BACKUP/SANITIZER/ShapeGlobal_comments2.java";
        String patchFileName = "/mnt/500GB/BACKUP/SANITIZER/patch.xml";

        FileWorker fw = new FileWorker(originalFileName, processedFileName, patchFileName);
        PatchData patchData;

        if ((patchData = fw.getPatchDataFromDiff()) != null) {
            // new XMLPatchWorker().writePatchData(new File(patchFileName), patchData);
            fw.updatePatchData();
        }
        // System.out.println(new XMLPatchWorker().readPatchData(configFile,
        // PatchData.class));
    }
}
