package ua.com.foxminded.sanitizer;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class XMLPatchWorker implements PatchWorker {

    @Override
    public PatchData readPatch(File file, Class<?> c) {
        PatchData patch = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            patch = (PatchData) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return patch;
    }

    @Override
    public void writePatch(File file, PatchData patchData) {
        try {
            JAXBContext context = JAXBContext.newInstance(patchData.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(patchData, System.out);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
