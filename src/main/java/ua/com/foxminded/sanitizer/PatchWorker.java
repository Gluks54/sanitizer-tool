package ua.com.foxminded.sanitizer;

import java.io.File;

public interface PatchWorker {
    public PatchData readPatch(File file, Class<?> c);

    public void writePatch(File file, PatchData patch);
}
