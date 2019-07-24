package ua.com.foxminded.sanitizer.worker.commandshell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UnixCommandShell extends AbstractCommandShell {

    public UnixCommandShell() {
        setJavaExecutable("java");
    }

    public void runCommand(String command) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);

            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
