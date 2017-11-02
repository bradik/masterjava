package ru.javaops.masterjava;

import com.google.common.base.Splitter;
import com.google.common.io.Resources;
import org.xml.sax.SAXException;
import ru.javaops.masterjava.xml.schemanew.*;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;

public class MainXml {
    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload_new.xsd"));
    }

    public static void main(String[] args) throws IOException, JAXBException, SAXException, XMLStreamException {

        String projectName = args[0];

        List<User> users;

//        users = usersByProjectJAXB(projectName);
//
//        System.out.println(users);

        users = usersByProjectStAX(projectName);

        System.out.println(users);

    }

    private static List<User> usersByProjectStAX(String projectName) throws IOException, XMLStreamException {

        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(Resources.getResource("payload_new.xml").openStream())) {

            Predicate<StaxStreamProcessor> isProject = p -> {
                final int event = p.getReader().getEventType();
                try {
                    if ((event == XMLEvent.START_ELEMENT) && "Project".equals(p.getValue())) {
                        return true;
                    }
                } catch (XMLStreamException e) {
                    //not catch
                }
                return false;
            };

            Predicate<StaxStreamProcessor> isGroup = p -> {
                final int event = p.getReader().getEventType();
                try {
                    if ((event == XMLEvent.START_ELEMENT) && "Group".equals(p.getValue())) {
                        return true;
                    }
                    if ((event == XMLEvent.END_ELEMENT) && "Project".equals(p.getValue())) {
                        return true;
                    }

                } catch (XMLStreamException e) {
                    //not catch
                }
                return false;
            };

            final Set<String> groupNames = new HashSet<>();

            while (processor.doUntil(isProject)) {
                if (projectName.equals(processor.getAttributeValue("name"))) {
                    while (processor.doUntil(isGroup)) {
                        if ("Project".equals(processor.getValue()))
                            break;

                        groupNames.add(processor.getAttributeValue("name"));
                    }
                }
            }


            List<User> users = new ArrayList<>();

            while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
                String groupRefs = processor.getAttributeValue("groupRefs");
                if (!Collections.disjoint(groupNames, Splitter.on(' ').splitToList(nullToEmpty(groupRefs)))) {
                    User user = new User();
                    user.setEmail(processor.getAttributeValue("email"));
                    user.setContent(processor.getText());
                    users.add(user);
                }
            }

            return users;

//            PrintStream out = System.out;
//
//            XMLStreamReader r = processor.getReader();
//
//            int event = r.getEventType();
//            while (true) {
//                switch (event) {
//                    case XMLEvent.START_DOCUMENT:
//                        out.println("Start Document.");
//                        break;
//                    case XMLEvent.START_ELEMENT:
//                        out.println("Start Element: " + r.getName());
//                        for (int i = 0, n = r.getAttributeCount(); i < n; ++i)
//                            out.println("Attribute: " + r.getAttributeName(i)
//                                    + "=" + r.getAttributeValue(i));
//
//                        break;
//                    case XMLEvent.CHARACTERS:
//                        if (r.isWhiteSpace())
//                            break;
//
//                        out.println("Text: " + r.getText());
//                        break;
//                    case XMLEvent.END_ELEMENT:
//                        out.println("End Element:" + r.getName());
//                        break;
//                    case XMLEvent.END_DOCUMENT:
//                        out.println("End Document.");
//                        break;
//                }
//
//                if (!r.hasNext())
//                    break;
//
//                event = r.next();
//            }


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
