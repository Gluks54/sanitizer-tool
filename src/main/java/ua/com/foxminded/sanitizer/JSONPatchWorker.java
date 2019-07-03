package ua.com.foxminded.sanitizer;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JSONPatchWorker implements PatchWorker {
    private String configFileName = "/mnt/500GB/BACKUP/SANITIZER/patch.json";

    @Override
    public PatchData readPatchData(File file, Class<?> c) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PatchData.class, new PatchDataJsonDeserializer());
        mapper.registerModule(module);

        PatchData patch = new PatchData();
        try {
            patch = mapper.readValue(new File(configFileName), PatchData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return patch;
    }

    @Override
    public void writePatchData(File file, PatchData patchData) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(patchData));
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(configFileName), patchData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
