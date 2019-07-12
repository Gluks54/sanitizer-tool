package ua.com.foxminded.sanitizer.worker;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ua.com.foxminded.sanitizer.data.Config;
import ua.com.foxminded.sanitizer.ui.SanitizerWindow;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

public class ConfigWorker extends SharedTextAreaLog {
    public Config readConfigData(File file, Class<?> c) {
        Config config = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            config = (Config) unmarshaller.unmarshal(file);
            getLog().info("read template " + file.getAbsolutePath() + " " + SanitizerWindow.Status.OK.getStatus());
            return config;
        } catch (JAXBException e) {
            e.printStackTrace();
            getLog().severe("failure at JAXB in " + file.getAbsolutePath() + ", read template: "
                    + SanitizerWindow.Status.FAIL.getStatus());
            getLog().info("--- " + file.getAbsolutePath() + " doesn't looks like template file");
            return null;
        }
    }

    public boolean writeConfigData(File file, Config config) {
        try {
            JAXBContext context = JAXBContext.newInstance(config.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(config, System.out);
            marshaller.marshal(config, file);
            getLog().info("write config " + file.getAbsolutePath() + " " + SanitizerWindow.Status.OK.getStatus());
            return true;
        } catch (JAXBException e) {
            e.printStackTrace();
            getLog().severe("failure at JAXB in " + file.getAbsolutePath() + ", read config: "
                    + SanitizerWindow.Status.FAIL.getStatus());
            getLog().info("--- " + file.getAbsolutePath() + " doesn't looks like config file");
            return false;
        }
    }
}
