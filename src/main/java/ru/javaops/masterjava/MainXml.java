package ru.javaops.masterjava;

import com.google.common.io.Resources;
import org.xml.sax.SAXException;
import ru.javaops.masterjava.xml.schemanew.*;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MainXml {
    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload_new.xsd"));
    }

    public static void main(String[] args) throws IOException, JAXBException, SAXException, XMLStreamException {

        String projectName = args[0];

//        List<User> users = usersByProjectJAXB(projectName);
//
//        System.out.println(users);

        usersByProjectStAX(projectName);


    }

    private static void usersByProjectStAX(String projectName) throws IOException, XMLStreamException {

        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(Resources.getResource("payload_new.xml").openStream())) {


//            XMLStreamReader reader = processor.getReader();
//            while (reader.hasNext()){
//                int event = reader.next();
//
//                System.out.println(event);
//
//                if (reader.hasName())
//                    System.out.println(reader.getName());
//                else
//                    System.out.println("---");
//
//                if (reader.hasText()){
//                    System.out.println(reader.getText());
//                }
//
//
//            }

            while (processor.doUntil(XMLEvent.START_ELEMENT, "Project")) {
                while (processor.doUntil(XMLEvent.START_ELEMENT, "Group")) {
                    if (processor.getReader().hasName())
                        System.out.println(processor.getReader().getName());
                        System.out.println(processor.getAttributeValue("name"));
                }
            }
        }

}

    private static List<User> usersByProjectJAXB(String projectName) throws IOException, JAXBException {

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
