package ua.com.foxminded.sanitizer.worker;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ua.com.foxminded.sanitizer.patch.Template;

public class TemplateWorker {
    public Template readTemplateData(File file, Class<?> c) {
        Template template = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            template = (Template) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return template;
    }

    public void writeTemplateData(File file, Template template) {
        try {
            JAXBContext context = JAXBContext.newInstance(template.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(template, System.out);
            // marshaller.marshal(template, file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
