package ua.com.foxminded.sanitizer.worker.patch;

import java.io.File;

import ua.com.foxminded.sanitizer.patch.Template;

public interface IPatchWorker {
    public Template readPatchData(File file, Class<?> c);

    public void writePatchData(File file, Template patchData);
}
