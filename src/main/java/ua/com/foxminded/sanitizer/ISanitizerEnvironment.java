package ua.com.foxminded.sanitizer;

import lombok.Getter;
import ua.com.foxminded.sanitizer.worker.OSWorker;
import ua.com.foxminded.sanitizer.worker.OSWorker.OS;

public interface ISanitizerEnvironment {
    public enum Status {
        OK("ok"), FAIL("none");

        @Getter
        private final String status;

        private Status(String status) {
            this.status = status;
        }
    }

    public final String ORIG_SUFFIX = "orig";
    public final String STRIP_SUFFIX = "strip";
    public final String ORIGINAL_EXT = ".original";
    public final String PATCH_EXT = ".patch.xml";
    public final String MASTER_EXT = ".stz";
    public final String MASTER_PATTERN = "*.stz";
    public final String MASTER_DIALOG_NAME = "Sanitizer files (*.stz)";
    public final String XML_DIALOG_NAME = "XML files (*.xml)";
    public final String XML_PATTERN = "*.xml";
    public final OS ENV = new OSWorker().getOs();
}
