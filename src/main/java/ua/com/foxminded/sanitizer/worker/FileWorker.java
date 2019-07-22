package ua.com.foxminded.sanitizer.worker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.tika.Tika;

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
import ua.com.foxminded.sanitizer.ui.ISanitizerWindow;
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
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
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

    public String getProperOriginalFolderName(File dir) {
        return Arrays.stream(dir.listFiles()).filter(d -> d.getName().endsWith(ISanitizerWindow.ORIG_SUFFIX))
                .findFirst().get().getAbsolutePath();
    }

    public boolean isContainProperOriginalFolder(File dir) {
        return Arrays.stream(dir.listFiles())
                .anyMatch(d -> (d.isDirectory() && d.getName().endsWith(ISanitizerWindow.ORIG_SUFFIX)));
    }

    public boolean isContainProperStripFolder(File dir) {
        return Arrays.stream(dir.listFiles())
                .anyMatch(d -> (d.isDirectory() && d.getName().endsWith(ISanitizerWindow.STRIP_SUFFIX)));
    }

    public boolean isMatchFilePatterns(File file, Config config) {
        // boolean isMatchPattern = true;
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

    public String removeCommentsFromProperties(String code) {
        return code.replaceAll("([\\t]*#.*)|(=.*)", "$1 ");
    }

    public String removeCommentsFromCss(String code) {
        return code.replaceAll(
                "(/\\*([^*]|[\\r\\n]|(\\*+([^*/]|[\\r\\n])))*\\*+/)|\"(\\\\.|[^\\\\\"])*\"|'(\\\\[\\s\\S]|[^'])*'",
                "$1 ");
    }

    public String removeCommentsFromTs(String code) {
        return code.replaceAll("<!--(?!\\\\s*(?:\\\\[if [^\\\\]]+]|<!|>))(?:(?!-->)(.|\\\\n))*-->", "$1 ");
    }

    public String removeCommentsFromXml(String code) {
        return code.replaceAll("<!--(?!\\\\s*(?:\\\\[if [^\\\\]]+]|<!|>))(?:(?!-->)(.|\\\\n))*-->", "$1 ");
    }

    public String removeCommentsFromJava(String code) {
        return code.replaceAll("//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/", "$1 ");
    }

    public Template getTotalPatchFromDiff(String currentPatchDescription) { // текущие изменения по сравнению с
        // оригиналом
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
        Template totalFilePatch = new Template(); // весь патч со всеми изменениями за все время
        Map<Long, Delta> patches = new LinkedHashMap<Long, Delta>(); // мапа всех отдельных дельт (сеансов)
        Delta delta = new Delta(); // каждая дельта - список изменений в файле до сохранения

        diff.getDeltas().forEach(d -> { // маппим дифф файла на свой класс
            SanitizerFilePatch sfp = new SanitizerFilePatch(); // для marshal-unmarshal
            sfp.setType(d.getType());
            sfp.getSource().setLines(d.getSource().getLines());
            sfp.getSource().setPosition(d.getSource().getPosition());
            sfp.getTarget().setLines(d.getTarget().getLines());
            sfp.getTarget().setPosition(d.getTarget().getPosition());
            delta.getDeltas().add(sfp);
        });
        delta.setDescription(currentPatchDescription);
        totalFilePatch.setOriginalCRC32(getCheckSum(originalFile));
        totalFilePatch.setModifiedCRC32(getCheckSum(modifiedFile));
        patches.put(getCheckSum(modifiedFile), delta);
        totalFilePatch.setPatches(patches);
        return totalFilePatch;
    }

    public void updateTotalPatch(String currentPatchDescription) { // берем предыдущие изменения и добавляем текущий
                                                                   // snapshot
        File modifiedFile = new File(modifiedFilename);
        File patchFile = new File(patchFilename);
        Template totalFilePatch;
        long modifiedFileCRC32;
        Map<Long, Delta> newPatches;

        if (patchFile.exists()) { // берем предыдущий патч целиком
            totalFilePatch = new XMLPatchWorker().readPatchData(patchFile, Template.class);
            modifiedFileCRC32 = getCheckSum(modifiedFile);

            if ((totalFilePatch != null) && (totalFilePatch.getModifiedCRC32() != modifiedFileCRC32)) {
                Template previousPatchData = totalFilePatch;
                totalFilePatch = getTotalPatchFromDiff(currentPatchDescription);
                newPatches = totalFilePatch.getPatches();
                newPatches.putAll(previousPatchData.getPatches()); // объединяем предыдущие патчи с текущим
                totalFilePatch.setPatches(newPatches);
            }
        } else {
            totalFilePatch = getTotalPatchFromDiff(currentPatchDescription);
            newPatches = totalFilePatch.getPatches();
            totalFilePatch.setPatches(newPatches);
        }
        new XMLPatchWorker().writePatchData(patchFile, totalFilePatch);
    }
}
