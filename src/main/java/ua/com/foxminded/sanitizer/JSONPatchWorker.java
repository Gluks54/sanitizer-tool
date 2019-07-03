package ua.com.foxminded.sanitizer;

import java.io.File;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONPatchWorker implements PatchWorker {

    @Override
    public PatchData readPatch(File file, Class<?> c) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void writePatch(File file, PatchData patch) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(patch));
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
