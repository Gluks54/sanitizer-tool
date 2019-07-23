package ua.com.foxminded.sanitizer.worker;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ua.com.foxminded.sanitizer.data.MasterProject;
import ua.com.foxminded.sanitizer.ui.ISanitizerWindow;
import ua.com.foxminded.sanitizer.ui.elements.SharedTextAreaLog;

public class MasterProjectWorker extends SharedTextAreaLog {
    public MasterProject readMasterProject(File file, Class<?> c) {
        MasterProject masterProject = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            masterProject = (MasterProject) unmarshaller.unmarshal(file);
            getLog().info(
                    "read master project " + file.getAbsolutePath() + " " + ISanitizerWindow.Status.OK.getStatus());
            return masterProject;
        } catch (JAXBException e) {
            e.printStackTrace();
            getLog().severe("failure at JAXB in " + file.getAbsolutePath() + ", read master project: "
                    + ISanitizerWindow.Status.FAIL.getStatus());
            getLog().info("--- " + file.getAbsolutePath() + " doesn't looks like master project meta-file");
            return null;
        }
    }

    public boolean writeMasterProject(File file, MasterProject masterProject) {
        try {
            JAXBContext context = JAXBContext.newInstance(masterProject.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(masterProject, System.out);
            marshaller.marshal(masterProject, file);
            getLog().info(
                    "write master project " + file.getAbsolutePath() + " " + ISanitizerWindow.Status.OK.getStatus());
            return true;
        } catch (JAXBException e) {
            e.printStackTrace();
            getLog().severe("failure at JAXB in " + file.getAbsolutePath() + ", read master project: "
                    + ISanitizerWindow.Status.FAIL.getStatus());
            getLog().info("--- " + file.getAbsolutePath() + " doesn't looks like master project meta-file");
            return false;
        }
    }
}
