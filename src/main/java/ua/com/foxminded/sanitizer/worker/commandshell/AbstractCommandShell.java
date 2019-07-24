package ua.com.foxminded.sanitizer.worker.commandshell;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.CodeSource;

import lombok.Setter;
import ua.com.foxminded.sanitizer.ISanitizerEnvironment;
import ua.com.foxminded.sanitizer.Main;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

public abstract class AbstractCommandShell extends SharedTextAreaLog implements ISanitizerEnvironment {
    private String javaHome;
    @Setter
    private String javaExecutable;
    private String fileDivider;
    private String runningJarExecutable;
    private String userHome;
    private Status operationStatus;

    public AbstractCommandShell() {
        super();
        javaHome = System.getProperty("java.home");
        fileDivider = System.getProperty("file.separator");
        userHome = System.getProperty("user.home");
    }

    private String getRunningJarExecutable() {
        runningJarExecutable = "";
        try {
            CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            runningJarExecutable = URLDecoder.decode(jarFile.getAbsolutePath(), "UTF-8");
        } catch (UnsupportedEncodingException | URISyntaxException e) {
        }
        return runningJarExecutable;
    }

    private String getJavaFullPathExecutable() {
        return javaHome + fileDivider + "bin" + fileDivider + javaExecutable;
    }

    public boolean isSystemEnvironmentOK() {
        File javaExecFile = new File(getJavaFullPathExecutable());
        File jarJavaFile = new File(getRunningJarExecutable());
        File userHomeDir = new File(userHome);

        getLog().info("*** check system environment...");
        boolean isJavaExecFileOK = javaExecFile.exists() && javaExecFile.isFile();
        operationStatus = isJavaExecFileOK ? Status.OK : Status.FAIL;
        getLog().info("check main JAVA executable... " + javaExecFile + " " + operationStatus);

        boolean isJarJavaFileOK = jarJavaFile.exists() && jarJavaFile.isFile();
        operationStatus = isJarJavaFileOK ? Status.OK : Status.FAIL;
        getLog().info("check JAR-file path... " + jarJavaFile + " " + operationStatus);

        boolean isUserHomeDirOK = userHomeDir.exists() && userHomeDir.isDirectory();
        operationStatus = isUserHomeDirOK ? Status.OK : Status.FAIL;
        getLog().info("check user home folder... " + userHomeDir + " " + operationStatus);

        return isJarJavaFileOK && isJavaExecFileOK && isUserHomeDirOK;
    }

}
