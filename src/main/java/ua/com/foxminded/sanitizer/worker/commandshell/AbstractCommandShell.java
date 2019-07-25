package ua.com.foxminded.sanitizer.worker.commandshell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private String defaultEncoding;
    private Status operationStatus;

    public AbstractCommandShell() {
        super();
        javaHome = System.getProperty("java.home");
        fileDivider = System.getProperty("file.separator");
        userHome = System.getProperty("user.home");
        defaultEncoding = System.getProperty("file.encoding");
    }

    public abstract boolean isSTZFileAssociated();

    public abstract void associateSTZFileInOS();

    public abstract void deAssociateSTZFileInOS();

    protected String runCommand(String command) {
        Process process;
        String responce = "";
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp1251"));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(">> " + line);
                getLog().info(">> " + line);
                responce += line + System.lineSeparator();
            }
        } catch (InterruptedException | IOException e) {
        }
        return responce;
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

    public final boolean isSystemEnvironmentOK() {
        File javaExecFile = new File(getJavaFullPathExecutable());
        File jarJavaFile = new File(getRunningJarExecutable());
        File userHomeDir = new File(userHome);

        getLog().info("*** check system environment...");
        getLog().info("default system encoding... " + defaultEncoding);
        boolean isJavaExecFileOK = javaExecFile.exists() && javaExecFile.isFile();
        operationStatus = isJavaExecFileOK ? Status.OK : Status.FAIL;
        getLog().info("check main JAVA executable... " + javaExecFile + " " + operationStatus);

        boolean isJarJavaFileOK = jarJavaFile.exists() && jarJavaFile.isFile();
        operationStatus = isJarJavaFileOK ? Status.OK : Status.FAIL;
        getLog().info("check JAR-file path... " + jarJavaFile + " " + operationStatus);

        boolean isUserHomeDirOK = userHomeDir.exists() && userHomeDir.isDirectory();
        operationStatus = isUserHomeDirOK ? Status.OK : Status.FAIL;
        getLog().info("check user home folder... " + userHomeDir + " " + operationStatus);

        // return isJarJavaFileOK && isJavaExecFileOK && isUserHomeDirOK;
        return isJavaExecFileOK && isUserHomeDirOK;
    }

}
