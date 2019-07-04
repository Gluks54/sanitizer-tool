package ua.com.foxminded.sanitizer.worker;

import java.io.File;

import ua.com.foxminded.sanitizer.patch.PatchData;

public interface PatchWorker {
    public PatchData readPatchData(File file, Class<?> c);

    public void writePatchData(File file, PatchData patchData);
}
