package ua.com.foxminded.sanitizer;

import java.io.File;

public interface PatchWorker {
    public PatchData readPatchData(File file, Class<?> c);

    public void writePatchData(File file, PatchData patchData);
}
