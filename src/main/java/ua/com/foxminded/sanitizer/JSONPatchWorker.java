package ua.com.foxminded.sanitizer;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONPatchWorker implements PatchWorker {
    private String configFileName = "/mnt/500GB/BACKUP/SANITIZER/patch.json";

    @Override
    public PatchData readPatch(File file, Class<?> c) {
        ObjectMapper mapper = new ObjectMapper();
        PatchData patch = new PatchData();
        try {
            patch = mapper.readValue(new File(configFileName), PatchData.class);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return patch;
    }

    @Override
    public void writePatch(File file, PatchData patchData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(patchData));
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(configFileName), patchData);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
