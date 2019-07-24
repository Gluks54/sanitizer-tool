package ua.com.foxminded.sanitizer.worker.commandshell;

public class WindowsCommandShell extends AbstractCommandShell {

    public WindowsCommandShell() {
        super();
        setJavaExecutable("java.exe");
    }
}
