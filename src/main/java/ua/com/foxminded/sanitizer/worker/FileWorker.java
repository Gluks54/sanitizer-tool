package ua.com.foxminded.sanitizer.worker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.tika.Tika;
import org.xml.sax.SAXException;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ua.com.foxminded.sanitizer.data.Config;
import ua.com.foxminded.sanitizer.patch.Delta;
import ua.com.foxminded.sanitizer.patch.SanitizerFilePatch;
import ua.com.foxminded.sanitizer.patch.Template;
import ua.com.foxminded.sanitizer.ui.SanitizerWindow;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

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

    public String getCurrentDateTimeString() {
        return new SimpleDateFormat("vyyyyMMddHHmmss-").format(new Date(System.currentTimeMillis()));
    }

    public String getFileContentType(File file) throws IOException {
        return new Tika().detect(file);
    }

    public String getFileTime(File file) throws IOException {
        FileTime time = Files.getLastModifiedTime(Paths.get(file.getAbsolutePath()), LinkOption.NOFOLLOW_LINKS);
        return DateTimeFormatter.ofPattern("dd/MM/yyyy kk:mm:ss").format(time.toInstant().atZone(ZoneId.of("UTC")));
    }

    public String getPermissions(Set<PosixFilePermission> perm) {
        String s = "-";

        if (perm.contains(PosixFilePermission.OWNER_READ)) {
            s += "r";
        } else {
            s += "-";
        }
        if (perm.contains(PosixFilePermission.OWNER_WRITE)) {
            s += "w";
        } else {
            s += "-";
        }
        if (perm.contains(PosixFilePermission.OWNER_EXECUTE)) {
            s += "x";
        } else {
            s += "-";
        }
        s += "/";
        if (perm.contains(PosixFilePermission.GROUP_READ)) {
            s += "r";
        } else {
            s += "-";
        }
        if (perm.contains(PosixFilePermission.GROUP_WRITE)) {
            s += "w";
        } else {
            s += "-";
        }
        if (perm.contains(PosixFilePermission.GROUP_EXECUTE)) {
            s += "x";
        } else {
            s += "-";
        }
        s += "/";

        if (perm.contains(PosixFilePermission.OTHERS_READ)) {
            s += "r";
        } else {
            s += "-";
        }
        if (perm.contains(PosixFilePermission.OTHERS_WRITE)) {
            s += "w";
        } else {
            s += "-";
        }
        if (perm.contains(PosixFilePermission.OTHERS_EXECUTE)) {
            s += "x";
        } else {
            s += "-";
        }
        return s;
    }

    public boolean isMatchFilePatterns(File file, Config config) {
        boolean isMatchPattern = true;
        boolean isMatchFileExtension = true;

        isMatchFileExtension = config.getPatterns().stream().anyMatch(e -> file.getAbsolutePath().endsWith(e));
        // System.out.println(file.getName());

        // PathMatcher matcher =
        // FileSystems.getDefault().getPathMatcher(config.getCustomPattern());
        // isMatchPattern = matcher.matches(file.toPath());
        // Pattern pattern = Pattern.compile(config.getCustomPattern(),
        // Pattern.CASE_INSENSITIVE);
        // isMatchPattern = Pattern.matches(config.getCustomPattern(), file.getName());
        return isMatchFileExtension;
    }

    public String turnFileSizeToString(final long value) {
        final int BYTES = 1024;
        long[] dividers = new long[] { (long) Math.pow(BYTES, 3), (long) Math.pow(BYTES, 2), (long) Math.pow(BYTES, 1),
                (long) Math.pow(BYTES, 0) };
        String[] units = new String[] { "Gb", "Mb", "Kb", "b" };
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

    private boolean validatePomXml(String xmlFile) {
        try {
            if (xmlFile != null) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory
                        .newSchema(new StreamSource(getClass().getResourceAsStream("/xsd/maven-4.0.0.xsd")));
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(new File(xmlFile)));
                getLog().info("validate " + xmlFile + " " + SanitizerWindow.Status.OK.getStatus());
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
        // boolean isProperPomXml = hasPomXml && validatePomXml(pomFileName);
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
        return true;
    }

    public String fileToCodeString(Path path) { // fast file reader
        String code = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toString()))) {
            String line = "";
            while ((line = reader.readLine()) != null) {
                code += line + System.lineSeparator();
            }
        } catch (IOException e) {
            getLog().severe("!!! file read error at " + path.toString());
        }
        return code;
    }

    public void codeStringToFile(String code, Path path) { // fast file writer
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toString(), false))) {
            writer.write(code);
        } catch (IOException e) {
            getLog().severe("!!! file write error at " + path.toString());
        }
    }

    public String fixTabsInCodeString(String code) {
        return code.replaceAll(String.valueOf(tab), tabReplacer);
    }

    public String replaceInCodeString(String code, String original, String target) {
        return code.replaceAll(original, target);
    }

    private long getCheckSum(File file) {
        long result = 0;

        try (CheckedInputStream check = new CheckedInputStream(new FileInputStream(file), new CRC32());
                BufferedInputStream in = new BufferedInputStream(check)) {
            while (in.read() != -1) {
            }
            result = check.getChecksum().getValue();
        } catch (IOException e) {
            getLog().severe("error in get checksum method in " + file.getAbsolutePath());
            e.printStackTrace();
        }
        return result;
    }

    public String removeComments(File file) {
        String code = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = "";
            while ((line = reader.readLine()) != null) {
                code = code + line + System.lineSeparator();
            }
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        StringBuilder newCode = new StringBuilder();
        try (StringReader sr = new StringReader(code)) {
            boolean inBlockComment = false;
            boolean inLineComment = false;
            boolean out = true;

            int prev = sr.read();
            int cur;
            for (cur = sr.read(); cur != -1; cur = sr.read()) {
                if (inBlockComment) {
                    if ((prev == '*' && cur == '/') || (prev == '*' && cur == '*')) {
                        inBlockComment = false;
                        out = false;
                    }
                } else if (inLineComment) {
                    if (cur == '\r') { // start untested block
                        sr.mark(1);
                        int next = sr.read();
                        if (next != '\n') {
                            sr.reset();
                        }
                        inLineComment = false;
                        out = false; // end untested block
                    } else if (cur == '\n') {
                        inLineComment = false;
                        out = false;
                    }
                } else {
                    if (prev == '/' && cur == '*') {
                        sr.mark(1); // start untested block
                        int next = sr.read();
                        if (next != '*') {
                            inBlockComment = true; // tested line (without rest of block)
                        }
                        sr.reset(); // end untested block
                    } else if (prev == '/' && cur == '/') {
                        inLineComment = true;
                    } else if (out) {
                        newCode.append((char) prev);
                    } else {
                        out = true;
                    }
                }
                prev = cur;
            }
            if (prev != -1 && out && !inLineComment) {
                newCode.append((char) prev);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newCode.toString();
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
        patches.put(getCheckSum(modifiedFile), delta);
        patchData.setPatches(patches);
        return patchData;
    }

    public void updatePatchData() { // берем предыдущие изменения и добавляем текущий snapshot
        File modifiedFile = new File(modifiedFilename);
        File patchFile = new File(patchFilename);
        Template currentPatchData;
        long modifiedFileCRC32;
        Map<Long, Delta> patches;

        if (patchFile.exists()) { // берем предыдущий патч целиком
            currentPatchData = new XMLPatchWorker().readPatchData(patchFile, Template.class);
            modifiedFileCRC32 = getCheckSum(modifiedFile);

            if ((currentPatchData != null) && (currentPatchData.getModified() != modifiedFileCRC32)) {
                Template previousPatchData = currentPatchData;
                currentPatchData = getPatchDataFromDiff();
                patches = currentPatchData.getPatches();
                patches.putAll(previousPatchData.getPatches()); // объединяем предыдущие патчи с текущим
                currentPatchData.setPatches(patches);
            }
        } else {
            currentPatchData = getPatchDataFromDiff();
            patches = currentPatchData.getPatches();
            currentPatchData.setPatches(patches);
        }
        new XMLPatchWorker().writePatchData(patchFile, currentPatchData);
    }
}
