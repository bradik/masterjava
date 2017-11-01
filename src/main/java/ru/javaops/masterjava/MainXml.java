package ru.javaops.masterjava;

import com.google.common.io.Resources;
import org.xml.sax.SAXException;
import ru.javaops.masterjava.xml.schemanew.*;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainXml {
    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload_new.xsd"));
    }

    public static void main(String[] args) throws IOException, JAXBException, SAXException {

        String projectName = args[0];

        List<User> users = usersByProject(projectName);

        System.out.println(users);

    }

    private static List<User> usersByProject(String projectName) throws IOException, JAXBException {

        final JaxbParser parser = new JaxbParser(ObjectFactory.class);
        parser.setSchema(Schemas.ofClasspath("payload_new.xsd"));

        Payload payload = parser.unmarshal(
                Resources.getResource("payload_new.xml").openStream());

        final Project project = payload.getProjects().getProject().stream()
                .filter(project1 -> projectName.equals(project1.getName())).findFirst().get();

        final List<Group> groups = project.getGroup();

        List<User> users = payload.getUsers().getUser().stream()
                .filter(user -> !Collections.disjoint(groups, user.getGroupRefs())).collect(Collectors.toList());

        return users;

    }

}
