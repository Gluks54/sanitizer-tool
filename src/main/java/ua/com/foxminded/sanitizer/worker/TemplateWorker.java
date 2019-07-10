package ua.com.foxminded.sanitizer.worker;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ua.com.foxminded.sanitizer.data.Template;
import ua.com.foxminded.sanitizer.ui.SanitizerWindow;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

public class TemplateWorker extends SharedTextAreaLog {
    public Template readTemplateData(File file, Class<?> c) {
        Template template = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            template = (Template) unmarshaller.unmarshal(file);
            getLog().info("read template " + file.getAbsolutePath() + " " + SanitizerWindow.Status.OK.getStatus());
            return template;
        } catch (JAXBException e) {
            e.printStackTrace();
            getLog().severe("failure at JAXB in " + file.getAbsolutePath() + ", read template: "
                    + SanitizerWindow.Status.FAIL.getStatus());
            getLog().info("--- " + file.getAbsolutePath() + " doesn't looks like template file");
            return null;
        }
    }

    public boolean writeTemplateData(File file, Template template) {
        try {
            JAXBContext context = JAXBContext.newInstance(template.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(template, System.out);
            marshaller.marshal(template, file);
            getLog().info("write template " + file.getAbsolutePath() + " " + SanitizerWindow.Status.OK.getStatus());
            return true;
        } catch (JAXBException e) {
            e.printStackTrace();
            getLog().severe("failure at JAXB in " + file.getAbsolutePath() + ", read template: "
                    + SanitizerWindow.Status.FAIL.getStatus());
            getLog().info("--- " + file.getAbsolutePath() + " doesn't looks like template file");
            return false;
        }
    }
}
