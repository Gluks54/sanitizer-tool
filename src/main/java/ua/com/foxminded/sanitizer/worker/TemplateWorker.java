package ua.com.foxminded.sanitizer.worker;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ua.com.foxminded.sanitizer.data.Template;
import ua.com.foxminded.sanitizer.ui.SharedTextAreaLog;

public class TemplateWorker extends SharedTextAreaLog {
    public Template readTemplateData(File file, Class<?> c) {
        Template template = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            template = (Template) unmarshaller.unmarshal(file);
            getLog().info("ok read template " + file.getAbsolutePath());
            return template;
        } catch (JAXBException e) {
            getLog().severe("template read failure at JAXB in " + file.getAbsolutePath());
            e.printStackTrace();
            return null;
        }
    }

    public void writeTemplateData(File file, Template template) {
        try {
            JAXBContext context = JAXBContext.newInstance(template.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(template, System.out);
            // marshaller.marshal(template, file);
            getLog().info("ok write template " + file.getAbsolutePath());
        } catch (JAXBException e) {
            getLog().severe("template read failure at JAXB in " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }
}
