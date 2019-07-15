package ua.com.foxminded.sanitizer.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;

public class ProcessWorker extends Task<List<File>> {

    @Override
    protected List<File> call() throws Exception {
        File dir = new File("/usr/bin");
        File[] files = dir.listFiles();
        int count = files.length;

        List<File> copied = new ArrayList<File>();
        int i = 0;
        for (File file : files) {
            if (file.isFile()) {
                this.copy(file);
                copied.add(file);
            }
            i++;
            this.updateProgress(i, count);
        }
        return copied;
    }

    private void copy(File file) throws Exception {
        this.updateMessage("Copying: " + file.getAbsolutePath());
        Thread.sleep(500);
    }

}
