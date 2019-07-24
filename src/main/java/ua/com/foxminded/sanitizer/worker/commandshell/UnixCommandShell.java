package ua.com.foxminded.sanitizer.worker.commandshell;

public class UnixCommandShell extends AbstractCommandShell {

    public UnixCommandShell() {
        setJavaExecutable("java");
    }
}
