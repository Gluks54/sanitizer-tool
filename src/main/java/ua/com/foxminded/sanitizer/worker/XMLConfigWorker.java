package ua.com.foxminded.sanitizer.worker;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ua.com.foxminded.sanitizer.data.Config;
import ua.com.foxminded.sanitizer.ui.ISanitizerWindow;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

public class XMLConfigWorker extends SharedTextAreaLog implements IConfigWorker {
    @Override
    public Config readConfigData(File file, Class<?> c) {
        Config config = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            config = (Config) unmarshaller.unmarshal(file);
            getLog().info("read config " + file.getAbsolutePath() + " " + ISanitizerWindow.Status.OK.getStatus());
            return config;
        } catch (JAXBException e) {
            e.printStackTrace();
            getLog().severe("failure at JAXB in " + file.getAbsolutePath() + ", read config: "
                    + ISanitizerWindow.Status.FAIL.getStatus());
            getLog().info("--- " + file.getAbsolutePath() + " doesn't looks like config file");
            return null;
        }
    }

    @Override
    public boolean writeConfigData(File file, Config config) {
        try {
            JAXBContext context = JAXBContext.newInstance(config.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(config, System.out);
            marshaller.marshal(config, file);
            getLog().info("write config " + file.getAbsolutePath() + " " + ISanitizerWindow.Status.OK.getStatus());
            return true;
        } catch (JAXBException e) {
            e.printStackTrace();
            getLog().severe("failure at JAXB in " + file.getAbsolutePath() + ", read config: "
                    + ISanitizerWindow.Status.FAIL.getStatus());
            getLog().info("--- " + file.getAbsolutePath() + " doesn't looks like config file");
            return false;
        }
    }
}
