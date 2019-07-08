package ua.com.foxminded.sanitizer.worker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;
import ua.com.foxminded.sanitizer.patch.Delta;
import ua.com.foxminded.sanitizer.patch.Template;
import ua.com.foxminded.sanitizer.patch.SanitizerFilePatch;
import ua.com.foxminded.sanitizer.ui.SharedTextAreaLog;

@Log
@NoArgsConstructor
@RequiredArgsConstructor
public class FileWorker extends SharedTextAreaLog {
    private final String tabReplacer = "    ";
    private final char tab = '\u0009';
    @NonNull
    @Getter
    @Setter
    private String originalFilename;
    @NonNull
    @Getter
    @Setter
    private String modifiedFilename;
    @NonNull
    @Getter
    @Setter
    private String patchFilename;

    public String convertToStringRepresentation(final long value) {
        long[] dividers = new long[] { 1_000_000_000, 1_000_000, 1_000, 1 };
        String[] units = new String[] { "Gb", "Mb", "Kb", "B" };
        String result = "";
        for (int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if (value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    private String format(long value, long divider, String unit) {
        double result = divider > 1 ? (double) value / (double) divider : (double) value;
        return new DecimalFormat("#,##0.#").format(result) + " " + unit;
    }

    private boolean validate(String xmlFile) {
        try {
            if (xmlFile != null) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                URL schemaFile = new URL("https://maven.apache.org/xsd/maven-4.0.0.xsd");
                getLog().info("validating " + xmlFile + " with " + schemaFile);
                Schema schema;
                schema = schemaFactory.newSchema(schemaFile);
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(new File(xmlFile)));
                getLog().info("ok");
                return true;
            }
        } catch (SAXException e) {
            getLog().severe("SAX error");
            e.printStackTrace();
        } catch (IOException e) {
            getLog().severe("IO error");
            e.printStackTrace();
        }
        return false;
    }

    public boolean isMavenProject(File file) {
        String pomFileName = "pom.xml";
        boolean hasPomXml = (new File(file.getAbsoluteFile() + "/" + pomFileName)).exists();
        if (hasPomXml) {
            getLog().info("found " + pomFileName);
        } else {
            getLog().info("no " + pomFileName);
        }
        // boolean isProperPomXml = hasPomXml && validate(pomFileName);
        boolean isProperPomXml = hasPomXml;

        File srcFolder = new File(file.getAbsoluteFile() + "/src");
        boolean hasSrcFolder = srcFolder.exists() && (!srcFolder.isFile());
        if (hasSrcFolder) {
            getLog().info("found src folder " + srcFolder);
        } else {
            getLog().info("no src folder");
        }

        return file.isDirectory() && hasPomXml && isProperPomXml && hasSrcFolder;
    }

    public boolean isAngularProject(File file) {
        return file.isDirectory();
    }

    public boolean hasTabs(File file) {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            int r;
            while ((r = buffer.read()) != -1) {
                if ((char) r == '\t') {
                    buffer.close();
                    return true;
                }
            }
        } catch (IOException e) {
            log.severe("error");
            e.printStackTrace();
        }
        return false;
    }

    public void fixTabsInFile(Path file) throws IOException {
        Path oldFile = Files.move(file, Paths.get(file.toString() + ".old"), StandardCopyOption.REPLACE_EXISTING);
        try (BufferedReader readBuffer = new BufferedReader(
                new InputStreamReader(new FileInputStream(oldFile.toFile())));
                BufferedWriter writeBuffer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(file.toFile())))) {

            int r;
            while ((r = readBuffer.read()) != -1) {
                if ((char) r == tab) {
                    writeBuffer.write(tabReplacer);
                } else {
                    writeBuffer.write(r);
                }
            }
        }
    }

    public void replaceStringInFile(File file, String oldString, String newString, boolean backup) throws IOException {
        if (backup) {
            String backupFileName = file.getName() + ".old";
            Files.copy(file.toPath(), Paths.get(backupFileName), StandardCopyOption.REPLACE_EXISTING);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
                FileWriter writer = new FileWriter(file)) {
            String line = "";
            String oldContent = "";
            while ((line = reader.readLine()) != null) {
                oldContent = oldContent + line + System.lineSeparator();
            }
            String newContent = oldContent.replaceAll(oldString, newString);
            writer.write(newContent);
        }
    }

    private long getCheckSum(File file) {
        long result = 0;

        try (CheckedInputStream check = new CheckedInputStream(new FileInputStream(file), new CRC32());
                BufferedInputStream in = new BufferedInputStream(check)) {
            while (in.read() != -1) {
            }
            result = check.getChecksum().getValue();
        } catch (IOException e) {
            log.severe("Error in get checksum method");
            e.printStackTrace();
        }
        return result;
    }

    public Template getPatchDataFromDiff() { // текущие изменения по сравнению с оригиналом
        File originalFile = new File(originalFilename);
        File modifiedFile = new File(modifiedFilename);
        List<String> original;
        List<String> revised;
        Patch<String> diff;

        try {
            original = Files.readAllLines(originalFile.toPath());
            revised = Files.readAllLines(modifiedFile.toPath());
            diff = DiffUtils.diff(original, revised);
        } catch (IOException | DiffException e) {
            e.printStackTrace();
            return null;
        }
        Template patchData = new Template(); // весь патч со всеми изменениями за все время
        Map<Long, Delta> patches = new LinkedHashMap<Long, Delta>(); // мапа всех отдельных дельт (сеансов)
        Delta delta = new Delta(); // каждая дельта - список изменений в файле до сохранения

        diff.getDeltas().stream().forEach(d -> { // маппим дифф файла на свой класс
            SanitizerFilePatch sfp = new SanitizerFilePatch(); // для marshal-unmarshal
            sfp.setType(d.getType());
            sfp.getSource().setLines(d.getSource().getLines());
            sfp.getSource().setPosition(d.getSource().getPosition());
            sfp.getTarget().setLines(d.getTarget().getLines());
            sfp.getTarget().setPosition(d.getTarget().getPosition());
            delta.getDelta().add(sfp);
        });
        patchData.setOriginal(getCheckSum(originalFile));
        patchData.setModified(getCheckSum(modifiedFile));
        patches.put(getCheckSum(modifiedFile), delta); // в
        patchData.setPatches(patches);
        return patchData;
    }

    public void updatePatchData() { // берем предыдущие изменения и добавляем текущий snapshot
        File modifiedFile = new File(modifiedFilename);
        File patchFile = new File(patchFilename);

        if (patchFile.exists()) { // берем предыдущий патч целиком
            Template currentPatchData = new XMLPatchWorker().readPatchData(patchFile, Template.class);
            long modifiedFileCRC32 = getCheckSum(modifiedFile);

            if (currentPatchData == null) {
                log.severe("null currentpatchdata");
            } else if (currentPatchData.getModified() == modifiedFileCRC32) { // если изменений нет
                log.severe("checksum equals, skipping patchFile update");
            } else if (currentPatchData.getModified() != modifiedFileCRC32) { // если изменения есть
                log.severe("checksum not equals, updating patchFile");
                Template previousPatchData = currentPatchData;
                currentPatchData = getPatchDataFromDiff();

                Map<Long, Delta> patches = currentPatchData.getPatches();
                patches.putAll(previousPatchData.getPatches()); // объединяем предыдущие патчи с текущим
                currentPatchData.setPatches(patches);
                new XMLPatchWorker().writePatchData(patchFile, currentPatchData);
            }

        } else {
            log.severe("no patchFile to update");
        }
    }
}
