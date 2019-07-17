package ua.com.foxminded.sanitizer.worker;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ua.com.foxminded.sanitizer.patch.Template;

public class XMLPatchWorker implements PatchWorker {

    @Override
    public Template readPatchData(File file, Class<?> c) {
        Template patchData = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            patchData = (Template) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return patchData;
    }

    @Override
    public void writePatchData(File file, Template patchData) {
        try {
            JAXBContext context = JAXBContext.newInstance(patchData.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(patchData, file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
